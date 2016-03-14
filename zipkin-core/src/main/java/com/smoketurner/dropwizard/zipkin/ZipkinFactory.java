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
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.EmptySpanCollector;
import com.github.kristofa.brave.LoggingSpanCollector;
import com.github.kristofa.brave.Sampler;
import com.github.kristofa.brave.SpanCollector;
import com.github.kristofa.brave.SpanCollectorMetricsHandler;
import com.github.kristofa.brave.http.DefaultSpanNameProvider;
import com.github.kristofa.brave.http.HttpSpanCollector;
import com.github.kristofa.brave.jaxrs2.BraveContainerRequestFilter;
import com.github.kristofa.brave.jaxrs2.BraveContainerResponseFilter;
import com.github.kristofa.brave.scribe.ScribeSpanCollector;
import com.github.kristofa.brave.scribe.ScribeSpanCollectorParams;
import com.google.common.net.HostAndPort;
import com.google.common.net.InetAddresses;
import com.smoketurner.dropwizard.zipkin.metrics.DropwizardSpanCollectorMetricsHandler;
import io.dropwizard.setup.Environment;
import io.dropwizard.validation.OneOf;
import io.dropwizard.validation.PortRange;

public class ZipkinFactory {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ZipkinFactory.class);
    private static final String DEFAULT_ZIPKIN_SCRIBE = "127.0.0.1:9140";

    @NotNull
    private HostAndPort endpoint = HostAndPort
            .fromString(DEFAULT_ZIPKIN_SCRIBE);

    private String serviceName;

    @NotEmpty
    private String serviceHost;

    @PortRange
    private Integer servicePort;

    @OneOf(value = { "http", "logging", "scribe", "empty" })
    @NotEmpty
    private String collector = "logging";

    @Min(0)
    @Max(1)
    @NotNull
    private Float sampleRate = 1.0f;

    @JsonProperty
    public HostAndPort getEndpoint() {
        return endpoint;
    }

    @JsonProperty
    public void setEndpoint(HostAndPort endpoint) {
        this.endpoint = endpoint;
    }

    @JsonProperty
    public String getServiceName() {
        return serviceName;
    }

    @JsonProperty
    public void setSeviceName(String serviceName) {
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
    public Integer getServicePort() {
        return servicePort;
    }

    @JsonProperty
    public void setServicePort(Integer servicePort) {
        this.servicePort = servicePort;
    }

    @JsonProperty
    public String getCollector() {
        return collector;
    }

    @JsonProperty
    public void setCollector(String collector) {
        this.collector = collector;
    }

    @JsonProperty
    public Float getSampleRate() {
        return sampleRate;
    }

    @JsonProperty
    public void setSampleRate(float sampleRate) {
        this.sampleRate = sampleRate;
    }

    /**
     * Build a new {@link Brave} instance for interfacing with Zipkin
     *
     * @param environment
     *            Environment
     * @return Brave instance
     */
    public Brave build(@Nonnull final Environment environment) {
        final SpanCollectorMetricsHandler metricsHandler = new DropwizardSpanCollectorMetricsHandler(
                environment.metrics());
        final SpanCollector spanCollector;
        switch (collector) {
        case "scribe":
            final ScribeSpanCollectorParams params = new ScribeSpanCollectorParams();
            params.setMetricsHandler(metricsHandler);
            spanCollector = new ScribeSpanCollector(endpoint.getHostText(),
                    endpoint.getPort(), params);
            LOGGER.info("Connecting to Zipkin scribe span collector at <{}:{}>",
                    endpoint.getHostText(), endpoint.getPort());
            break;
        case "http":
            spanCollector = HttpSpanCollector.create(
                    String.format("http://%s:%d", endpoint.getHostText(),
                            endpoint.getPort()),
                    metricsHandler);
            break;
        case "empty":
            spanCollector = new EmptySpanCollector();
            break;
        case "logging":
        default:
            spanCollector = new LoggingSpanCollector();
            break;
        }

        LOGGER.info("Using Zipkin service ({}) at <{}:{}>", serviceName,
                serviceHost, servicePort);

        final Brave.Builder builder = new Brave.Builder(toInt(serviceHost),
                servicePort, serviceName);
        builder.spanCollector(spanCollector);
        builder.traceSampler(Sampler.create(sampleRate));

        final Brave brave = builder.build();

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

    private static int toInt(final String ip) {
        return InetAddresses.coerceToInteger(InetAddresses.forString(ip));
    }
}
