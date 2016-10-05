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
package com.smoketurner.dropwizard.zipkin.managed;

import java.util.Objects;
import javax.annotation.Nonnull;
import io.dropwizard.lifecycle.Managed;
import zipkin.Component.CheckResult;
import zipkin.reporter.Sender;

public class SenderManager implements Managed {

    private final Sender sender;

    /**
     * Constructor
     *
     * @param sender
     *            Sender to manage
     */
    public SenderManager(@Nonnull final Sender sender) {
        this.sender = Objects.requireNonNull(sender);
    }

    @Override
    public void start() throws Exception {
        final CheckResult result = sender.check();
        if (!result.ok) {
            throw new Exception("Unable to connect to Zipkin destination",
                    result.exception);
        }
    }

    @Override
    public void stop() throws Exception {
        sender.close();
    }
}
