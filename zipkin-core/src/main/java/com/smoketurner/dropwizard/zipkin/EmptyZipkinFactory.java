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

import brave.handler.SpanHandler;
import brave.http.HttpTracing;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.setup.Environment;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonTypeName("empty")
public class EmptyZipkinFactory extends AbstractZipkinFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(EmptyZipkinFactory.class);

  /**
   * Build a new {@link HttpTracing} instance for interfacing with Zipkin
   *
   * @param environment Environment
   * @return HttpTracing instance
   */
  @Override
  public Optional<HttpTracing> build(final Environment environment) {
    if (!isEnabled()) {
      LOGGER.warn("Zipkin tracing is disabled");
      return Optional.empty();
    }

    LOGGER.info("Dropping all collected spans");
    return buildTracing(environment, SpanHandler.NOOP);
  }
}
