/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.grpc;

import io.grpc.collector.CollectorGrpc;
import io.grpc.collector.DataPoint;
import io.grpc.collector.DataRequest;
import io.grpc.collector.DataResponse;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple gRPC client that wraps a gRPC channel  .
 */
public class DataClient {

    private static final Logger logger = Logger.getLogger(DataClient.class.getName());

    private static int requestID = 0;
    private final ManagedChannel channel;
    private final CollectorGrpc.CollectorBlockingStub blockingStub;

    /**
     * Creates a new gRPC channel & stub for a @{@link CollectorGrpc}.
     *
     * @param host ip of server
     * @param port port of server
     */
    public DataClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS).
                // For the example we disable TLS to avoid needing certificates.
                .usePlaintext()
                .build();
        blockingStub = CollectorGrpc.newBlockingStub(channel);
    }

    /**
     * Shuts down the channel of the data client.
     *
     * @throws InterruptedException
     */
    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Makes gRPC call to datasource to fetch new data.
     */
    public List<DataPoint> fetchData() {
        int localRequestID = requestID++;
        DataRequest request = DataRequest.newBuilder().setRequestID(localRequestID).build();
        DataResponse response;
        try {
            response = blockingStub.fetchData(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {" + localRequestID + "}", e.getStatus());
            return new LinkedList<>();
        }
        return response.getDataList();
    }

}
