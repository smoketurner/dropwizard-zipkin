/*
 * Copyright © 2018 Smoke Turner, LLC (contact@smoketurner.com)
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
package com.smoketurner.dropwizard.zipkin.client;

import brave.http.HttpTracing;
import brave.jaxrs2.TracingClientFilter;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;
import java.util.Objects;
import javax.ws.rs.client.Client;
import org.jetbrains.annotations.NotNull;

public class ZipkinClientBuilder {
  private final Environment environment;
  private final HttpTracing tracing;

  /**
   * Constructor
   *
   * @param environment Environment
   * @param tracing HttpTracing instance
   */
  public ZipkinClientBuilder(
      @NotNull final Environment environment, @NotNull final HttpTracing tracing) {
    this.environment = Objects.requireNonNull(environment);
    this.tracing = Objects.requireNonNull(tracing);
  }

  /**
   * Build a new Jersey Client that is instrumented for Zipkin
   *
   * @param configuration Configuration to use for the client
   * @return new Jersey Client
   */
  public Client build(@NotNull final ZipkinClientConfiguration configuration) {
    final Client client =
        new JerseyClientBuilder(environment)
            .using(configuration)
            .build(configuration.getServiceName());
    return build(client);
  }

  /**
   * Instrument an existing Jersey client
   *
   * @param client Jersey client
   * @return an instrumented Jersey client
   */
  public Client build(@NotNull final Client client) {
    client.register(TracingClientFilter.create(tracing));
    return client;
  }
}
