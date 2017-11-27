/**
 * Copyright 2017 Smoke Turner, LLC.
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

import java.util.Optional;
import javax.annotation.Nonnull;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.smoketurner.dropwizard.zipkin.managed.ReporterManager;
import com.smoketurner.dropwizard.zipkin.metrics.DropwizardReporterMetrics;
import brave.http.HttpTracing;
import io.dropwizard.setup.Environment;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.ReporterMetrics;
import zipkin2.reporter.kafka11.KafkaSender;

@JsonTypeName("kafka")
public class KafkaZipkinFactory extends AbstractZipkinFactory {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(KafkaZipkinFactory.class);

    @NotEmpty
    private String bootstrapServers = "";

    @NotEmpty
    private String topic = "zipkin";

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

    /**
     * Build a new {@link HttpTracing} instance for interfacing with Zipkin
     *
     * @param environment
     *            Environment
     * @return Brave instance
     */
    @Override
    public Optional<HttpTracing> build(@Nonnull final Environment environment) {
        if (!isEnabled()) {
            LOGGER.warn("Zipkin tracing is disabled");
            return Optional.empty();
        }

        final ReporterMetrics metricsHandler = new DropwizardReporterMetrics(
                environment.metrics());

        final KafkaSender sender = KafkaSender.newBuilder()
                .bootstrapServers(bootstrapServers).topic(topic).build();

        final AsyncReporter<Span> reporter = AsyncReporter.builder(sender)
                .metrics(metricsHandler).build();

        environment.lifecycle().manage(new ReporterManager(reporter, sender));

        LOGGER.info("Sending spans to Kafka topic '{}' at: {}", topic,
                bootstrapServers);

        return buildTracing(environment, reporter);
    }
}
