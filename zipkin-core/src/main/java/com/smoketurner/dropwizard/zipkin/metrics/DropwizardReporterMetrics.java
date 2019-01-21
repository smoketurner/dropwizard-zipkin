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
package com.smoketurner.dropwizard.zipkin.metrics;

import com.codahale.metrics.MetricRegistry;
import java.util.Objects;
import zipkin2.reporter.ReporterMetrics;

public class DropwizardReporterMetrics implements ReporterMetrics {

  private final MetricRegistry registry;

  /**
   * Constructor
   *
   * @param registry Metric Registry
   */
  public DropwizardReporterMetrics(final MetricRegistry registry) {
    this.registry = Objects.requireNonNull(registry);
  }

  @Override
  public void incrementMessages() {
    registry.meter("tracing.reporter.message.accepted").mark();
  }

  @Override
  public void incrementMessagesDropped(Throwable cause) {
    registry.meter("tracing.reporter.message.dropped").mark();
  }

  @Override
  public void incrementSpans(int quantity) {
    registry.meter("tracing.reporter.span.accepted").mark(quantity);
  }

  @Override
  public void incrementSpanBytes(int quantity) {
    registry.histogram("tracing.reporter.span.bytes").update(quantity);
  }

  @Override
  public void incrementMessageBytes(int quantity) {
    registry.histogram("tracing.reporter.message.bytes").update(quantity);
  }

  @Override
  public void incrementSpansDropped(int quantity) {
    registry.meter("tracing.reporter.span.dropped").mark(quantity);
  }

  @Override
  public void updateQueuedSpans(int update) {
    registry.meter("tracing.reporter.queued.span").mark();
  }

  @Override
  public void updateQueuedBytes(int update) {
    registry.histogram("tracing.reporter.queued.bytes").update(update);
  }
}
