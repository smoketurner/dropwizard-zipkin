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
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.github.kristofa.brave.Brave;
import com.smoketurner.dropwizard.zipkin.managed.SenderManager;
import com.smoketurner.dropwizard.zipkin.metrics.DropwizardReporterMetrics;
import io.dropwizard.setup.Environment;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.ReporterMetrics;
import zipkin.reporter.urlconnection.URLConnectionSender;

@JsonTypeName("http")
public class HttpZipkinFactory extends AbstractZipkinFactory {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(HttpZipkinFactory.class);

    @NotEmpty
    private String baseUrl = "http://127.0.0.1:9411/";

    @JsonProperty
    public String getBaseUrl() {
        return baseUrl;
    }

    @JsonProperty
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Build a new {@link Brave} instance for interfacing with Zipkin
     *
     * @param environment
     *            Environment
     * @return Brave instance
     */
    @Override
    public Brave build(@Nonnull final Environment environment) {
        final ReporterMetrics metricsHandler = new DropwizardReporterMetrics(
                environment.metrics());

        final URLConnectionSender sender = URLConnectionSender
                .create(baseUrl + "api/v1/spans");

        environment.lifecycle().manage(new SenderManager(sender));

        final AsyncReporter<Span> reporter = AsyncReporter.builder(sender)
                .metrics(metricsHandler).build();

        LOGGER.info("Sending spans to HTTP collector at: {}", baseUrl);

        return buildBrave(environment, reporter);
    }
}
