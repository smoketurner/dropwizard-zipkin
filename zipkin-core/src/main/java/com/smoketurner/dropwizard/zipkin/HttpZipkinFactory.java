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

import brave.http.HttpTracing;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.smoketurner.dropwizard.zipkin.managed.ReporterManager;
import com.smoketurner.dropwizard.zipkin.metrics.DropwizardReporterMetrics;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.MinDuration;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.ReporterMetrics;
import zipkin2.reporter.urlconnection.URLConnectionSender;

@JsonTypeName("http")
public class HttpZipkinFactory extends AbstractZipkinFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpZipkinFactory.class);

  @NotEmpty private String baseUrl = "http://127.0.0.1:9411/";

  @NotNull
  @MinDuration(value = 0, unit = TimeUnit.MILLISECONDS)
  private Duration connectTimeout = Duration.seconds(10);

  @NotNull
  @MinDuration(value = 0, unit = TimeUnit.MILLISECONDS)
  private Duration readTimeout = Duration.seconds(60);

  @JsonProperty
  public String getBaseUrl() {
    return baseUrl;
  }

  @JsonProperty
  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  @JsonProperty
  public void setConnectTimeout(Duration connectTimeout) {
    this.connectTimeout = connectTimeout;
  }

  @JsonProperty
  public void setReadTimeout(Duration readTimeout) {
    this.readTimeout = readTimeout;
  }

  /**
   * Build a new {@link HttpTracing} instance for interfacing with Zipkin
   *
   * @param environment Environment
   * @return HttpTracing instance
   */
  @Override
  public Optional<HttpTracing> build(final Environment environment) {
    if (!isEnabled()) {
      LOGGER.warn("Zipkin tracing is disabled");
      return Optional.empty();
    }

    final ReporterMetrics metricsHandler = new DropwizardReporterMetrics(environment.metrics());

    final String endpoint = URI.create(baseUrl).resolve("api/v2/spans").toString();

    final URLConnectionSender sender =
        URLConnectionSender.newBuilder()
            .endpoint(endpoint)
            .readTimeout(Math.toIntExact(readTimeout.toMilliseconds()))
            .connectTimeout(Math.toIntExact(connectTimeout.toMilliseconds()))
            .build();

    final AsyncReporter<Span> reporter =
        AsyncReporter.builder(sender).metrics(metricsHandler).build();

    environment.lifecycle().manage(new ReporterManager(reporter, sender));

    LOGGER.info("Sending spans to HTTP collector at: {}", baseUrl);

    return buildTracing(environment, reporter);
  }
}
