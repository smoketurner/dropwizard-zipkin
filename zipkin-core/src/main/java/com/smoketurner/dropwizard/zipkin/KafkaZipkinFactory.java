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

import brave.http.HttpTracing;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.setup.Environment;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javax.validation.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zipkin2.reporter.kafka.KafkaSender;

@JsonTypeName("kafka")
public class KafkaZipkinFactory extends ReportingZipkinFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaZipkinFactory.class);

  @NotEmpty private String bootstrapServers = "";

  @NotEmpty private String topic = "zipkin";

  private Map<String, String> overrides = new LinkedHashMap<>();

  @JsonProperty
  public String getBootstrapServers() {
    return bootstrapServers;
  }

  @JsonProperty
  public void setBootstrapServers(String bootstrapServers) {
    this.bootstrapServers = bootstrapServers;
  }

  @JsonProperty
  public String getTopic() {
    return topic;
  }

  @JsonProperty
  public void setTopic(String topic) {
    this.topic = topic;
  }

  @JsonProperty
  public Map<String, String> getOverrides() {
    return overrides;
  }

  @JsonProperty
  public void setOverrides(Map<String, String> overrides) {
    this.overrides = overrides;
  }

  /**
   * Build a new {@link HttpTracing} instance for interfacing with Zipkin
   *
   * @param environment Environment
   * @return Brave instance
   */
  @Override
  public Optional<HttpTracing> build(final Environment environment) {
    if (!isEnabled()) {
      LOGGER.warn("Zipkin tracing is disabled");
      return Optional.empty();
    }

    final KafkaSender sender =
        KafkaSender.newBuilder()
            .bootstrapServers(bootstrapServers)
            .topic(topic)
            .overrides(overrides)
            .build();

    LOGGER.info("Sending spans to Kafka topic \"{}\" at: {}", topic, bootstrapServers);

    return buildTracing(environment, sender);
  }
}
