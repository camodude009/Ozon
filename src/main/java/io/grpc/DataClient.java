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
 * A simple RestletClient that requests data from the {@link DataServer}.
 */
public class DataClient {

    private static final Logger logger = Logger.getLogger(DataClient.class.getName());
    private static int requestID = 0;

    private final ManagedChannel channel;
    private final CollectorGrpc.CollectorBlockingStub blockingStub;

    /**
     * Construct RestletClient connecting to data RestletServer at {@code host:port}.
     */
    public DataClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS).
                // For the example we disable TLS to avoid needing certificates.
                .usePlaintext()
                .build());
    }

    /**
     * Construct RestletClient for accessing data RestletServer using the existing channel.
     */
    DataClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = CollectorGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Request data from RestletServer.
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
