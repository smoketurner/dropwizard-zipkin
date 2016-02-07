package com.example.helloworld.resources;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import com.codahale.metrics.annotation.Timed;

@Path("/")
public class HelloWorldResource {
    private final Client client;

    public HelloWorldResource(Client client) {
        this.client = Objects.requireNonNull(client);
    }

    @GET
    @Timed
    @Path("/fetch")
    public Response fetch() throws InterruptedException {
        final Random random = new Random();
        TimeUnit.MILLISECONDS.sleep(random.nextInt(1000));
        return client.target("http://localhost:8888/result").request().get();
    }

    @GET
    @Timed
    @Path("/result")
    public Response result() throws InterruptedException {
        final Random random = new Random();
        TimeUnit.MILLISECONDS.sleep(random.nextInt(1000));
        return Response.noContent().build();
    }
}
