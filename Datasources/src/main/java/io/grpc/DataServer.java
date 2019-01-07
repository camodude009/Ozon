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

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Wrapper for implementation of a @{@link Server} using a
 * {@link io.grpc.collector.CollectorGrpc.CollectorImplBase}.
 */
public class DataServer {
    private static final Logger logger = Logger.getLogger(DataServer.class.getName());

    /**
     * gRCP {@link Server}.
     */
    private Server server;
    /**
     * Implementation of {@link io.grpc.collector.CollectorGrpc.CollectorImplBase} gRPC service.
     */
    private CollectorGrpc.CollectorImplBase service;
    /**
     * Port the gRPC {@link Server} runs on.
     */
    private int port;

    /**
     * Constructs a {@link DataServer}, setting its service and port.
     *
     * @param service service to be run by this {@link DataServer}
     * @param port    port this {@link DataServer} should run on
     */
    public DataServer(CollectorGrpc.CollectorImplBase service, int port) {
        this.service = service;
        this.port = port;
    }

    /**
     * Starts up the gRPC {@link Server}.
     *
     * @throws IOException
     */
    public void start() throws IOException {
        // building and starting gRPC Server
        server = ServerBuilder.forPort(port)
                .addService(service)
                .build()
                .start();
        logger.info("Server started, listening on port: " + port);
        // adding shutdown hook to cleanly shut down server when the JVM gets shut down
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println("Shutting down gRPC server since JVM is shutting down");
            DataServer.this.stop();
            System.err.println("Server shut down");
        }));
    }

    /**
     * Shuts down the gRPC {@link Server}.
     */
    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the gRPC library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
