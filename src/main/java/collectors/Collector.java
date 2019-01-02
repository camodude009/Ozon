package collectors;

import io.grpc.collector.CollectorGrpc;
import io.grpc.collector.DataPoint;
import io.grpc.collector.DataRequest;
import io.grpc.collector.DataResponse;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public abstract class Collector extends CollectorGrpc.CollectorImplBase implements Runnable {

    private static final Logger logger = Logger.getLogger(BinanceCollector.class.getName());
    private List<DataPoint> data;

    protected Collector() {
        data = new ArrayList<>(1000);
    }

    @Override
    public final void fetchData(DataRequest req, StreamObserver<DataResponse> responseObserver) {
        int requestID = req.getRequestID();
        logger.info("Received request #" + requestID);
        DataResponse response = DataResponse.newBuilder()
                .addAllData(getAndResetData())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private final synchronized List<DataPoint> getAndResetData() {
        List<DataPoint> oldData = data;
        data = new ArrayList<>(1000);
        return oldData;
    }

    protected synchronized void addData(DataPoint... dataPoints) {
        for (DataPoint dp : dataPoints) {
            data.add(dp);
        }
    }
}


