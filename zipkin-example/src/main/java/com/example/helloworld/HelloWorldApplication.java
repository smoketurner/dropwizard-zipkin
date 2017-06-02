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
package com.example.helloworld;

import java.util.Optional;
import javax.ws.rs.client.Client;
import com.example.helloworld.resources.HelloWorldResource;
import com.smoketurner.dropwizard.zipkin.ZipkinBundle;
import com.smoketurner.dropwizard.zipkin.ZipkinFactory;
import com.smoketurner.dropwizard.zipkin.client.ZipkinClientBuilder;
import com.smoketurner.dropwizard.zipkin.client.ZipkinClientConfiguration;
import com.smoketurner.dropwizard.zipkin.rx.BraveRxJavaSchedulersHook;
import brave.Tracing;
import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import rx.plugins.RxJavaHooks;

public class HelloWorldApplication
        extends Application<HelloWorldConfiguration> {

    public static void main(String[] args) throws Exception {
        new HelloWorldApplication().run(args);
    }

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
        bootstrap.addBundle(
                new ZipkinBundle<HelloWorldConfiguration>(getName()) {
                    @Override
                    public ZipkinFactory getZipkinFactory(
                            HelloWorldConfiguration configuration) {
                        return configuration.getZipkinFactory();
                    }
                });
    }

    @Override
    public void run(HelloWorldConfiguration configuration,
            Environment environment) throws Exception {

        final Optional<Tracing> tracing = configuration.getZipkinFactory()
                .build(environment);

        final Client client;
        if (tracing.isPresent()) {
            client = new ZipkinClientBuilder(environment, tracing.get())
                    .build(configuration.getZipkinClient());

            final BraveRxJavaSchedulersHook hook = new BraveRxJavaSchedulersHook(
                    tracing.get());
            RxJavaHooks.setOnScheduleAction(hook::onSchedule);
        } else {
            final ZipkinClientConfiguration clientConfig = configuration
                    .getZipkinClient();
            client = new JerseyClientBuilder(environment).using(clientConfig)
                    .build(clientConfig.getServiceName());
        }

        // Register resources
        final HelloWorldResource resource = new HelloWorldResource(client);
        environment.jersey().register(resource);
    }
}
