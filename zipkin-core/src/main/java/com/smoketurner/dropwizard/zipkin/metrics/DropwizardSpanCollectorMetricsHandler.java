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
package com.smoketurner.dropwizard.zipkin.metrics;

import java.util.Objects;
import javax.annotation.Nonnull;
import com.codahale.metrics.MetricRegistry;
import com.github.kristofa.brave.SpanCollectorMetricsHandler;

public class DropwizardSpanCollectorMetricsHandler
        implements SpanCollectorMetricsHandler {

    private static final String ACCEPTED_METER = "tracing.collector.scribe.span.accepted";
    private static final String DROPPED_METER = "tracing.collector.scribe.span.dropped";
    private final MetricRegistry registry;

    /**
     * Constructor
     *
     * @param registry
     *            Metric Registry
     */
    public DropwizardSpanCollectorMetricsHandler(
            @Nonnull final MetricRegistry registry) {
        this.registry = Objects.requireNonNull(registry);
    }

    @Override
    public void incrementAcceptedSpans(final int quantity) {
        registry.meter(ACCEPTED_METER).mark(quantity);
    }

    @Override
    public void incrementDroppedSpans(final int quantity) {
        registry.meter(DROPPED_METER).mark(quantity);
    }
}
