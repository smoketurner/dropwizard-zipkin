package com.example.helloworld;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smoketurner.dropwizard.zipkin.LoggingZipkinFactory;
import com.smoketurner.dropwizard.zipkin.ZipkinFactory;
import com.smoketurner.dropwizard.zipkin.client.ZipkinClientConfiguration;
import io.dropwizard.Configuration;

public class HelloWorldConfiguration extends Configuration {

    @Valid
    @NotNull
    public final ZipkinFactory zipkin = new LoggingZipkinFactory();

    @Valid
    @NotNull
    private final ZipkinClientConfiguration zipkinClient = new ZipkinClientConfiguration();

    @JsonProperty
    public ZipkinFactory getZipkinFactory() {
        return zipkin;
    }

    @JsonProperty
    public ZipkinClientConfiguration getZipkinClient() {
        return zipkinClient;
    }
}
