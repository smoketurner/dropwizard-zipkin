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
package com.smoketurner.dropwizard.zipkin.client;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.ws.rs.client.Client;
import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.http.DefaultSpanNameProvider;
import com.github.kristofa.brave.jaxrs2.BraveClientRequestFilter;
import com.github.kristofa.brave.jaxrs2.BraveClientResponseFilter;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;

public class ZipkinClientBuilder {
    private final Environment environment;
    private final Brave brave;

    /**
     * Constructor
     *
     * @param environment
     *            Environment
     * @param brave
     *            Brave instance
     */
    public ZipkinClientBuilder(@Nonnull final Environment environment,
            @Nonnull final Brave brave) {
        this.environment = Objects.requireNonNull(environment);
        this.brave = Objects.requireNonNull(brave);
    }

    /**
     * Build a new Jersey Client that is instrumented for Zipkin
     * 
     * @param configuration
     *            Configuration to use for the client
     * @return new Jersey Client
     */
    public Client build(
            @Nonnull final ZipkinClientConfiguration configuration) {
        final Client client = new JerseyClientBuilder(environment)
                .using(configuration).build(configuration.getServiceName());
        return build(client);
    }

    /**
     * Instrument an existing Jersey client
     *
     * @param client
     *            Jersey client
     * @return an instrumented Jersey client
     */
    public Client build(@Nonnull final Client client) {
        // Register the request filter for outgoing client requests
        client.register(
                new BraveClientRequestFilter(new DefaultSpanNameProvider(),
                        brave.clientRequestInterceptor()));

        // Register the response filter for incoming client requests
        client.register(new BraveClientResponseFilter(
                brave.clientResponseInterceptor()));

        return client;
    }
}
