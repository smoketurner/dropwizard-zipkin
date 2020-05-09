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
import com.smoketurner.dropwizard.zipkin.managed.ReporterManager;
import com.smoketurner.dropwizard.zipkin.metrics.DropwizardReporterMetrics;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.MinDuration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Sender;
import zipkin2.reporter.brave.ZipkinSpanHandler;

public abstract class ReportingZipkinFactory extends AbstractZipkinFactory {
  @NotNull
  @MinDuration(value = 0, unit = TimeUnit.MILLISECONDS)
  private Duration reportTimeout = Duration.seconds(1);

  @JsonProperty
  public void setReportTimeout(Duration reportTimeout) {
    this.reportTimeout = reportTimeout;
  }

  @JsonProperty
  public Duration getReportTimeout() {
    return reportTimeout;
  }

  protected Optional<HttpTracing> buildTracing(final Environment environment, Sender sender) {
    final AsyncReporter<Span> reporter =
        AsyncReporter.builder(sender)
            .metrics(new DropwizardReporterMetrics(environment.metrics()))
            .messageTimeout(reportTimeout.toNanoseconds(), TimeUnit.NANOSECONDS)
            .build();

    environment.lifecycle().manage(new ReporterManager(reporter, sender));

    return buildTracing(environment, ZipkinSpanHandler.create(reporter));
  }
}
