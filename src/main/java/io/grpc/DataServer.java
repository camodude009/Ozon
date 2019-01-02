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

import collectors.Collector;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Server that manages startup/shutdown of a {@code data} RestletServer.
 */
public class DataServer {
    private static final Logger logger = Logger.getLogger(DataServer.class.getName());

    private Server server;
    private Collector collector;

    public DataServer(Collector collector) {
        this.collector = collector;
        new Thread(collector).start();
    }

    public void start() throws IOException {
        server = ServerBuilder.forPort(50051)
                .addService(collector)
                .build()
                .start();
        logger.info("Server started, listening on 50051");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("Shutting down gRPC RestletServer since JVM is shutting down");
                DataServer.this.stop();
                System.err.println("Server shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
