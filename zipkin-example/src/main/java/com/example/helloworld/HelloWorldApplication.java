package com.example.helloworld;

import javax.ws.rs.client.Client;
import com.example.helloworld.resources.HelloWorldResource;
import com.github.kristofa.brave.Brave;
import com.smoketurner.dropwizard.zipkin.ZipkinBundle;
import com.smoketurner.dropwizard.zipkin.ZipkinFactory;
import com.smoketurner.dropwizard.zipkin.client.ZipkinClientBuilder;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class HelloWorldApplication
        extends Application<HelloWorldConfiguration> {

    public static void main(String[] args) throws Exception {
        new HelloWorldApplication().run(args);
    }

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
        bootstrap.addBundle(
                new ZipkinBundle<HelloWorldConfiguration>(getName()) {
                    @Override
                    public ZipkinFactory getZipkinFactory(
                            HelloWorldConfiguration configuration) {
                        return configuration.getZipkinFactory();
                    }
                });
    }

    @Override
    public void run(HelloWorldConfiguration configuration,
            Environment environment) throws Exception {

        final Brave brave = configuration.getZipkinFactory().build(environment);

        final Client client = new ZipkinClientBuilder(environment, brave)
                .build(configuration.getZipkinClient());

        // Register resources
        final HelloWorldResource resource = new HelloWorldResource(client);
        environment.jersey().register(resource);
    }
}
