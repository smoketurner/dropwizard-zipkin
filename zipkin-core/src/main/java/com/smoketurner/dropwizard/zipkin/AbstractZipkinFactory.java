/*
 * Copyright © 2018 Smoke Turner, LLC (github@smoketurner.com)
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
import brave.http.HttpClientParser;
import brave.http.HttpSampler;
import brave.http.HttpServerParser;
import brave.http.HttpTracing;
import brave.jersey.server.TracingApplicationEventListener;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.sampler.Sampler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import io.dropwizard.validation.PortRange;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zipkin2.Span;
import zipkin2.reporter.Reporter;

/**
 * @see ConsoleZipkinFactory
 * @see EmptyZipkinFactory
 * @see HttpZipkinFactory
 * @see KafkaZipkinFactory
 */
public abstract class AbstractZipkinFactory implements ZipkinFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractZipkinFactory.class);
  private static final int DEFAULT_DW_PORT = 8080;

  private boolean enabled = true;

  @Nullable private String serviceName;

  @NotEmpty private String serviceHost = "127.0.0.1";

  @PortRange private int servicePort = DEFAULT_DW_PORT;

  @Min(0)
  @Max(1)
  private float sampleRate = 1.0f;

  @Nullable private Sampler sampler = null;

  private HttpClientParser clientParser = new HttpClientParser();
  private HttpSampler clientSampler = HttpSampler.TRACE_ID;
  private HttpServerParser serverParser = new HttpServerParser();
  private HttpSampler serverSampler = HttpSampler.TRACE_ID;

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
  public boolean getTraceId128Bit() {
    return traceId128Bit;
  }

  @JsonProperty
  public void setTraceId128Bit(boolean traceId128Bit) {
    this.traceId128Bit = traceId128Bit;
  }

  @JsonIgnore
  public HttpClientParser getClientParser() {
    return clientParser;
  }

  @JsonIgnore
  public void setClientParser(HttpClientParser parser) {
    this.clientParser = parser;
  }

  @JsonIgnore
  public HttpSampler getClientSampler() {
    return clientSampler;
  }

  @JsonIgnore
  public void setClientSampler(HttpSampler sampler) {
    this.clientSampler = sampler;
  }

  @JsonIgnore
  public HttpServerParser getServerParser() {
    return serverParser;
  }

  @JsonIgnore
  public void setServerParser(HttpServerParser parser) {
    this.serverParser = parser;
  }

  @JsonIgnore
  public HttpSampler getServerSampler() {
    return serverSampler;
  }

  @JsonIgnore
  public void setServerSampler(HttpSampler sampler) {
    this.serverSampler = sampler;
  }

  /**
   * Build a new {@link HttpTracing} instance for interfacing with Zipkin
   *
   * @param environment Environment
   * @param reporter reporter
   * @return HttpTracing instance
   */
  protected Optional<HttpTracing> buildTracing(
      final Environment environment, final Reporter<Span> reporter) {

    LOGGER.info(
        "Registering Zipkin service ({}) at <{}:{}>", serviceName, serviceHost, servicePort);

    final Tracing tracing =
        Tracing.newBuilder()
            .currentTraceContext(
                ThreadLocalCurrentTraceContext.newBuilder()
                    .addScopeDecorator(MDCScopeDecorator.create())
                    .build())
            .localIp(serviceHost)
            .localPort(servicePort)
            .spanReporter(reporter)
            .localServiceName(serviceName)
            .sampler(getSampler())
            .traceId128Bit(traceId128Bit)
            .build();

    final HttpTracing httpTracing =
        HttpTracing.newBuilder(tracing)
            .clientParser(clientParser)
            .clientSampler(clientSampler)
            .serverParser(serverParser)
            .serverSampler(serverSampler)
            .build();

    // Register the tracing feature for client and server requests
    environment.jersey().register(TracingApplicationEventListener.create(httpTracing));
    environment
        .lifecycle()
        .manage(
            new Managed() {
              @Override
              public void start() throws Exception {
                // nothing to start
              }

              @Override
              public void stop() throws Exception {
                tracing.close();
              }
            });

    return Optional.of(httpTracing);
  }
}
