/**
 * Copyright 2016 Smoke Turner, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.smoketurner.dropwizard.zipkin;

import javax.annotation.Nonnull;

import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.Sampler;
import com.github.kristofa.brave.http.DefaultSpanNameProvider;
import com.github.kristofa.brave.jaxrs2.BraveContainerRequestFilter;
import com.github.kristofa.brave.jaxrs2.BraveContainerResponseFilter;
import com.google.common.net.InetAddresses;

import io.dropwizard.setup.Environment;
import io.dropwizard.validation.PortRange;
import zipkin.Span;
import zipkin.reporter.Reporter;

/**
 * @see ConsoleZipkinFactory
 * @see EmptyZipkinFactory
 * @see HttpZipkinFactory
 * @see KafkaZipkinFactory
 * @see LoggingZipkinFactory
 * @see ScribeZipkinFactory
 */
public abstract class AbstractZipkinFactory implements ZipkinFactory {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AbstractZipkinFactory.class);
    private static final int DEFAULT_DW_PORT = 8080;

    private String serviceName;

    @NotEmpty
    private String serviceHost = "127.0.0.1";

    @PortRange
    private int servicePort = DEFAULT_DW_PORT;

    private Sampler sampler;

    private boolean traceId128Bit = false;

    @JsonProperty
    public String getServiceName() {
        return serviceName;
    }

    @Override
    @JsonProperty
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @JsonProperty
    public String getServiceHost() {
        return serviceHost;
    }

    @JsonProperty
    public void setServiceHost(String serviceHost) {
        this.serviceHost = serviceHost;
    }

    @JsonProperty
    public int getServicePort() {
        return servicePort;
    }

    @JsonProperty
    public void setServicePort(int servicePort) {
        this.servicePort = servicePort;
    }

    @JsonProperty
    public Sampler getSampler() {
        return sampler;
    }

    @JsonProperty
    public void setSampler(Sampler sampler) {
        this.sampler = sampler;
    }

    @JsonProperty
    public boolean getTraceId128Bit() {
        return traceId128Bit;
    }

    @JsonProperty
    public void setTraceId128Bit(boolean traceId128Bit) {
        this.traceId128Bit = traceId128Bit;
    }

    private static int toInt(final String ip) {
        return InetAddresses.coerceToInteger(InetAddresses.forString(ip));
    }

    /**
     * Build a new {@link Brave} instance for interfacing with Zipkin
     *
     * @param environment
     *            Environment
     * @param reporter
     *            reporter
     * @return Brave instance
     */
    protected Brave buildBrave(@Nonnull final Environment environment,
            @Nonnull final Reporter<Span> reporter) {

        LOGGER.info("Registering Zipkin service ({}) at <{}:{}>", serviceName,
                serviceHost, servicePort);

        final Brave brave = new Brave.Builder(toInt(serviceHost), servicePort,
                serviceName).reporter(reporter)
                        .traceSampler(sampler)
                        .traceId128Bit(traceId128Bit).build();

        // Register the request filter for incoming server requests
        environment.jersey()
                .register(new BraveContainerRequestFilter(
                        brave.serverRequestInterceptor(),
                        new DefaultSpanNameProvider()));

        // Register the response filter for outgoing server requests
        environment.jersey().register(new BraveContainerResponseFilter(
                brave.serverResponseInterceptor()));

        return brave;
    }
}
