package de.uniba.rz.backend;

import de.uniba.rz.backend.configs.Configuration;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Properties;

public class RestRemoteAccess extends RemoteAccess{

    private static Properties properties = Configuration.loadProperties();

    @Override
    void prepareStartup(TicketStore ticketStore) {

    }

    @Override
    void shutdown() {

    }

    @Override
    public void run() {
        String serverUri = properties.getProperty("serverUri");

        URI baseUri = UriBuilder.fromUri(serverUri).build();
        ResourceConfig config = ResourceConfig.forApplicationClass(RestApi.class);
        JdkHttpServerFactory.createHttpServer(baseUri, config);
        System.out.println("Server ready to serve your JAX-RS requests...");
    }
}
