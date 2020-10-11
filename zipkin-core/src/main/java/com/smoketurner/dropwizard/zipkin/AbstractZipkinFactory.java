/*
 * Copyright © 2019 Smoke Turner, LLC (github@smoketurner.com)
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
package com.smoketurner.dropwizard.zipkin;

import brave.Tracing;
import brave.context.slf4j.MDCScopeDecorator;
import brave.handler.SpanHandler;
import brave.http.HttpClientParser;
import brave.http.HttpRequest;
import brave.http.HttpRequestParser;
import brave.http.HttpResponseParser;
import brave.http.HttpServerParser;
import brave.http.HttpTracing;
import brave.jersey.server.TracingApplicationEventListener;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.sampler.Sampler;
import brave.sampler.SamplerFunction;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Strings;
import io.dropwizard.validation.PortRange;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see ConsoleZipkinFactory
 * @see EmptyZipkinFactory
 * @see HttpZipkinFactory
 * @see KafkaZipkinFactory
 */
public abstract class AbstractZipkinFactory implements ZipkinFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractZipkinFactory.class);

  private boolean enabled = true;

  @Nullable private String serviceName;

  // TODO: This must be an IP address. consider renaming
  @Nullable private String serviceHost;

  @Nullable private Integer servicePort;

  // TODO: This is not a rate, rather a probability. Rate is N traces/second via RateLimitingSampler
  @Min(0)
  @Max(1)
  private float sampleRate = 1.0f;

  @Nullable private Sampler sampler = null;

  @Deprecated @Nullable private HttpClientParser clientParser;
  @Nullable private HttpRequestParser clientRequestParser;
  @Nullable private HttpResponseParser clientResponseParser;
  @Nullable private SamplerFunction<HttpRequest> clientSampler;
  @Deprecated @Nullable private HttpServerParser serverParser;
  @Nullable private HttpRequestParser serverRequestParser;
  @Nullable private HttpResponseParser serverResponseParser;
  @Nullable private SamplerFunction<HttpRequest> serverSampler;

  private boolean supportsJoin = true;
  private boolean traceId128Bit = false;

  @JsonProperty
  public boolean isEnabled() {
    return enabled;
  }

  @JsonProperty
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  @Nullable
  @JsonProperty
  public String getServiceName() {
    return serviceName;
  }

  @Override
  @JsonProperty
  public void setServiceName(@Nullable String serviceName) {
    this.serviceName = serviceName;
  }

  @JsonProperty
  public String getServiceHost() {
    return serviceHost;
  }

  @JsonProperty
  public void setServiceHost(@Nullable String serviceHost) {
    this.serviceHost = serviceHost;
  }

  @JsonProperty
  public Integer getServicePort() {
    return servicePort;
  }

  @JsonProperty
  public void setServicePort(@Nullable Integer servicePort) {
    this.servicePort = servicePort;
  }

  @JsonProperty
  public float getSampleRate() {
    return sampleRate;
  }

  @JsonProperty
  public void setSampleRate(float sampleRate) {
    this.sampleRate = sampleRate;
  }

  @JsonIgnore
  public Sampler getSampler() {
    if (sampler == null) {
      return Sampler.create(sampleRate);
    }
    return sampler;
  }

  @JsonIgnore
  public void setSampler(@Nullable Sampler sampler) {
    this.sampler = sampler;
  }

  @JsonProperty
  public boolean getSupportsJoin() {
    return supportsJoin;
  }

  @JsonProperty
  public void setSupportsJoin(boolean supportsJoin) {
    this.supportsJoin = supportsJoin;
  }

  @JsonProperty
  public boolean getTraceId128Bit() {
    return traceId128Bit;
  }

  @JsonProperty
  public void setTraceId128Bit(boolean traceId128Bit) {
    this.traceId128Bit = traceId128Bit;
  }

  @Deprecated
  @JsonIgnore
  public HttpClientParser getClientParser() {
    return clientParser;
  }

  @Deprecated
  @JsonIgnore
  public void setClientParser(HttpClientParser parser) {
    this.clientParser = parser;
  }

  @JsonIgnore
  public HttpRequestParser getClientRequestParser() {
    return clientRequestParser;
  }

  @JsonIgnore
  public void setClientRequestParser(HttpRequestParser requestParser) {
    this.clientRequestParser = requestParser;
  }

  @JsonIgnore
  public HttpResponseParser getClientResponseParser() {
    return clientResponseParser;
  }

  @JsonIgnore
  public void setClientResponseParser(HttpResponseParser responseParser) {
    this.clientResponseParser = responseParser;
  }

  @JsonIgnore
  public SamplerFunction<HttpRequest> getClientSampler() {
    return clientSampler;
  }

  @JsonIgnore
  public void setClientSampler(SamplerFunction<HttpRequest> sampler) {
    this.clientSampler = sampler;
  }

  @Deprecated
  @JsonIgnore
  public HttpServerParser getServerParser() {
    return serverParser;
  }

  @Deprecated
  @JsonIgnore
  public void setServerParser(HttpServerParser parser) {
    this.serverParser = parser;
  }

  @JsonIgnore
  public HttpRequestParser getServerRequestParser() {
    return serverRequestParser;
  }

  @JsonIgnore
  public void setServerRequestParser(HttpRequestParser requestParser) {
    this.serverRequestParser = requestParser;
  }

  @JsonIgnore
  public HttpResponseParser getServerResponseParser() {
    return serverResponseParser;
  }

  @JsonIgnore
  public void setServerResponseParser(HttpResponseParser responseParser) {
    this.serverResponseParser = responseParser;
  }

  @JsonIgnore
  public SamplerFunction<HttpRequest> getServerSampler() {
    return serverSampler;
  }

  @JsonIgnore
  public void setServerSampler(SamplerFunction<HttpRequest> sampler) {
    this.serverSampler = sampler;
  }

  /**
   * Build a new {@link HttpTracing} instance for interfacing with Zipkin
   *
   * @param environment Environment
   * @param zipkinSpanHandler how to send spans to Zipkin
   * @return HttpTracing instance
   */
  protected Optional<HttpTracing> buildTracing(
      final Environment environment, final SpanHandler zipkinSpanHandler) {

    final Tracing.Builder tracingBuilder =
        Tracing.newBuilder()
            .sampler(getSampler())
            .supportsJoin(supportsJoin)
            .traceId128Bit(traceId128Bit)
            .currentTraceContext(
                ThreadLocalCurrentTraceContext.newBuilder()
                    .addScopeDecorator(MDCScopeDecorator.get())
                    .build())
            .addSpanHandler(zipkinSpanHandler);

    if (!Strings.isNullOrEmpty(serviceName)) {
      tracingBuilder.localServiceName(serviceName);
    } else {
      tracingBuilder.localServiceName(environment.getName());
    }

    // TODO: see if we can read this from the environment
    if (!Strings.isNullOrEmpty(serviceHost)) tracingBuilder.localIp(serviceHost);
    if (servicePort != null) tracingBuilder.localPort(servicePort);

    final Tracing tracing = tracingBuilder.build();

    LOGGER.info("Registering Zipkin {}", tracing);

    final HttpTracing.Builder httpTracingBuilder = HttpTracing.newBuilder(tracing);
    if (clientParser != null) httpTracingBuilder.clientParser(clientParser);
    if (clientRequestParser != null) httpTracingBuilder.clientRequestParser(clientRequestParser);
    if (clientResponseParser != null) httpTracingBuilder.clientResponseParser(clientResponseParser);
    if (serverRequestParser != null) httpTracingBuilder.serverRequestParser(serverRequestParser);
    if (serverResponseParser != null) httpTracingBuilder.serverResponseParser(serverResponseParser);
    if (serverParser != null) httpTracingBuilder.serverParser(serverParser);
    if (clientSampler != null) httpTracingBuilder.clientSampler(clientSampler);
    if (serverSampler != null) httpTracingBuilder.serverSampler(serverSampler);

    final HttpTracing httpTracing = httpTracingBuilder.build();

    // Register the tracing feature for client and server requests
    environment.jersey().register(TracingApplicationEventListener.create(httpTracing));
    environment
        .lifecycle()
        .manage(
            new Managed() {
              @Override
              public void start() {
                // nothing to start
              }

              @Override
              public void stop() {
                tracing.close();
              }
            });

    return Optional.of(httpTracing);
  }
}
