package application;

import io.grpc.collector.CollectorGrpc;
import io.grpc.collector.DataPoint;
import io.grpc.collector.DataRequest;
import io.grpc.collector.DataResponse;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * An implementation of the gRPC service {@link io.grpc.collector.CollectorGrpc.CollectorImplBase}.
 * To be extended by super classes, that collect data in their run() method.
 */
public abstract class Datasource extends CollectorGrpc.CollectorImplBase implements Runnable {

    private static final Logger logger = Logger.getLogger(Datasource.class.getName());

    /**
     * List of {@link DataPoint}s that accumulate and are transferred to the collector service on request.
     */
    private List<DataPoint> data;

    /**
     * Creates a new Datasource.
     */
    protected Datasource() {
        data = new ArrayList<>(1000);
    }

    /**
     * Server side implementation of the gRPC call that fetches data from a datasource.
     *
     * @param req              object supplied by the gRPC call
     * @param responseObserver object through which to reply to the call
     */
    @Override
    public final void fetchData(DataRequest req, StreamObserver<DataResponse> responseObserver) {
        // get request id from request object
        int requestID = req.getRequestID();
        logger.info("Received request #" + requestID);
        // build response object
        DataResponse response = DataResponse.newBuilder()
                .addAllData(getAndResetData())
                .build();
        // set response
        responseObserver.onNext(response);
        // tell gRPC that we are finished processing the request
        responseObserver.onCompleted();
    }

    /**
     * Gets current data and resets internal data store.
     * Synchronized to avoid dropped {@link DataPoint}s.
     *
     * @return
     */
    private final synchronized List<DataPoint> getAndResetData() {
        List<DataPoint> oldData = data;
        data = new ArrayList<>(1000);
        return oldData;
    }

    /**
     * Adds a {@link DataPoint} to internal data store.
     * Synchronized to avoid dropped {@link DataPoint}s.
     *
     * @param dp {@link DataPoint} to store
     */
    protected synchronized void addData(DataPoint dp) {
        data.add(dp);
    }
}


