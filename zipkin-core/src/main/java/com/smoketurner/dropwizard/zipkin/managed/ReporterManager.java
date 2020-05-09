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
package com.smoketurner.dropwizard.zipkin.managed;

import io.dropwizard.lifecycle.Managed;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zipkin2.CheckResult;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Sender;

public class ReporterManager implements Managed {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReporterManager.class);

  private final AsyncReporter<?> reporter;
  private final Sender sender;

  /**
   * Constructor
   *
   * @param reporter Reporter to manage
   * @param sender Sender to manage
   */
  public ReporterManager(final AsyncReporter<?> reporter, final Sender sender) {
    this.reporter = Objects.requireNonNull(reporter);
    this.sender = Objects.requireNonNull(sender);
  }

  @Override
  public void start() {
    final CheckResult result = reporter.check();
    if (!result.ok()) {
      LOGGER.error("Unable to connect to Zipkin destination", result.error());
    } else {
      LOGGER.info("Successfully connected to Zipkin");
    }
  }

  @Override
  public void stop() throws Exception {
    // the reporter needs to be closed first so that it can report on
    // any dropped spans before closing the sender connection.
    reporter.close();
    sender.close();
  }
}
