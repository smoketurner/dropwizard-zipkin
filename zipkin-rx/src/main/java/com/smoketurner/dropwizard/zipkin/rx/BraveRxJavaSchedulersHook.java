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
package com.smoketurner.dropwizard.zipkin.rx;

import java.util.Objects;
import brave.Tracing;
import brave.propagation.CurrentTraceContext;
import rx.functions.Action0;
import rx.plugins.RxJavaSchedulersHook;

public final class BraveRxJavaSchedulersHook extends RxJavaSchedulersHook {

    private final Tracing tracing;

    /**
     * Constructor
     *
     * @param tracing
     *            Tracing instance
     */
    public BraveRxJavaSchedulersHook(final Tracing tracing) {
        this.tracing = Objects.requireNonNull(tracing);
    }

    @Override
    public Action0 onSchedule(final Action0 action) {
        final ServerSpanThreadBinder binder = tracing.serverSpanThreadBinder();
        final ServerSpan span = binder.getCurrentServerSpan();
        return new Action0() {
            @Override
            public void call() {
                binder.setCurrentSpan(span);
                action.call();
            }
        };
    }
}
