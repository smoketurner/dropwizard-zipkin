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
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.ReporterMetrics;
import zipkin2.reporter.amqp.RabbitMQSender;

@JsonTypeName("amqp")
public class RabbitMQZipkinFactory extends AbstractZipkinFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQZipkinFactory.class);

  @NotEmpty private String addresses = "";

  @NotEmpty private String queue = "zipkin";

  @NotEmpty private String username = "guest";

  @NotEmpty private String password = "guest";

  @NotEmpty private String virtualHost = "/";

  @NotNull
  @MinDuration(value = 1, unit = TimeUnit.MILLISECONDS)
  private Duration connectionTimeout = Duration.seconds(60);

  @JsonProperty
  public String getAddresses() {
    return addresses;
  }

  @JsonProperty
  public void setAddresses(String addresses) {
    this.addresses = addresses;
  }

  @JsonProperty
  public String getQueue() {
    return queue;
  }

  @JsonProperty
  public void setQueue(String queue) {
    this.queue = queue;
  }

  @JsonProperty
  public String getUsername() {
    return username;
  }

  @JsonProperty
  public void setUsername(String username) {
    this.username = username;
  }

  @JsonProperty
  public String getPassword() {
    return password;
  }

  @JsonProperty
  public void setPassword(String password) {
    this.password = password;
  }

  @JsonProperty
  public String getVirtualHost() {
    return virtualHost;
  }

  @JsonProperty
  public void setVirtualHost(String virtualHost) {
    this.virtualHost = virtualHost;
  }

  @JsonProperty
  public Duration getConnectionTimeout() {
    return connectionTimeout;
  }

  @JsonProperty
  public void setConnectionTimeout(Duration timeout) {
    this.connectionTimeout = timeout;
  }

  /**
   * Build a new {@link HttpTracing} instance for interfacing with Zipkin
   *
   * @param environment Environment
   * @return Brave instance
   */
  @Override
  public Optional<HttpTracing> build(@NotNull final Environment environment) {
    if (!isEnabled()) {
      LOGGER.warn("Zipkin tracing is disabled");
      return Optional.empty();
    }

    final ReporterMetrics metricsHandler = new DropwizardReporterMetrics(environment.metrics());

    final RabbitMQSender sender =
        RabbitMQSender.newBuilder()
            .addresses(addresses)
            .queue(queue)
            .connectionTimeout((int) connectionTimeout.toMilliseconds())
            .username(username)
            .password(password)
            .virtualHost(virtualHost)
            .build();

    final AsyncReporter<Span> reporter =
        AsyncReporter.builder(sender).metrics(metricsHandler).build();

    environment.lifecycle().manage(new ReporterManager(reporter, sender));

    LOGGER.info("Sending spans to RabbitMQ queue \"{}\" at: {}", queue, addresses);

    return buildTracing(environment, reporter);
  }
}
