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
package com.smoketurner.dropwizard.zipkin.client;

import brave.http.HttpTracing;
import brave.httpclient.TracingHttpClientBuilder;
import brave.jaxrs2.TracingClientFilter;
import com.google.common.base.Strings;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;
import javax.ws.rs.client.Client;

public class ZipkinClientBuilder extends JerseyClientBuilder {
  private final Environment environment;
  private HttpTracing httpTracing;

  /**
   * Constructor
   *
   * @param environment Environment
   * @param httpTracing HttpTracing instance
   */
  public ZipkinClientBuilder(final Environment environment, final HttpTracing httpTracing) {
    super(environment);
    this.environment = environment;
    this.httpTracing = httpTracing;
    setApacheHttpClientBuilder(
        new HttpClientBuilder(environment) {
          @Override
          protected org.apache.http.impl.client.HttpClientBuilder createBuilder() {
            return TracingHttpClientBuilder.create(httpTracing);
          }
        });
  }

  @Override
  public JerseyClientBuilder using(JerseyClientConfiguration configuration) {
    if (configuration instanceof ZipkinClientConfiguration) {
      final String remoteServiceName = ((ZipkinClientConfiguration) configuration).getServiceName();
      if (!Strings.isNullOrEmpty(remoteServiceName)) {
        httpTracing = httpTracing.clientOf(remoteServiceName);
      }
    }
    return super.using(configuration);
  }

  /**
   * Build a new Jersey Client that is instrumented for Zipkin
   *
   * @param configuration Configuration to use for the client
   * @return new Jersey Client
   * @deprecated use {@code this} as a {@link JerseyClientBuilder} instead
   */
  @Deprecated
  public Client build(final ZipkinClientConfiguration configuration) {
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
   * @deprecated use {@code this} as a {@link JerseyClientBuilder} instead
   */
  @Deprecated
  public Client build(final Client client) {
    client.register(TracingClientFilter.create(httpTracing));
    return client;
  }
}
