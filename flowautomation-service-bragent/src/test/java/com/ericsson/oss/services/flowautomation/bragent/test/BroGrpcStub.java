/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.services.flowautomation.bragent.test;

import com.ericsson.oss.services.flowautomation.bragent.BackupAndRestoreAgentFA;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class BroGrpcStub {

    private static final Logger log = LoggerFactory.getLogger(BroGrpcStub.class);

    protected Server server;
    private static final String SERVER_CERTIFICATE_PATH = "src/test/resources/ExampleCertificates/server1.pem";
    private static final String SERVER_PRIVATE_KEY_PATH = "src/test/resources/ExampleCertificates/server1.key";
    private static final String SERVER_CERTIFICATE_AUTHORITY_PATH = "src/test/resources/ExampleCertificates/broca.pem";

    @Before
    public void baseGrpcSetup() throws Exception {
        startGrpcServer();
    }

    @After
    public void baseGrpcTeardown() {
        try {
            log.info("Shutting down GRPC server after test");
            stopGrpcServer();
            if(BackupAndRestoreAgentFA.getExecutorService() != null) {
                BackupAndRestoreAgentFA.killAgent();
            }
            Awaitility.await().atMost(1, TimeUnit.SECONDS).until(() -> BackupAndRestoreAgentFA.getExecutorService() == null);
        } catch (final Exception e) {}
    }

    protected void startGrpcServer() throws Exception {
        final NettyServerBuilder serverBuilder = NettyServerBuilder.forPort(3000).sslContext(getSslContextBuilder().build());;
        getServices().forEach(serverBuilder::addService);
        server = serverBuilder.build().start();
    }

    private SslContextBuilder getSslContextBuilder() {
        SslContextBuilder sslClientContextBuilder = null;
        sslClientContextBuilder = SslContextBuilder.forServer(new File(SERVER_CERTIFICATE_PATH),
            new File(SERVER_PRIVATE_KEY_PATH)).trustManager(new File(SERVER_CERTIFICATE_AUTHORITY_PATH)).clientAuth(ClientAuth.OPTIONAL);
        return GrpcSslContexts.configure(sslClientContextBuilder);
    }

    protected void stopGrpcServer() throws Exception {
        server.shutdownNow();
        server.awaitTermination();
        Awaitility.await().atMost(1, TimeUnit.SECONDS).until(server::isTerminated);
    }

    protected void restartGrpcServer() throws Exception {
        stopGrpcServer();
        startGrpcServer();
    }

    protected abstract List<BindableService> getServices();

}
