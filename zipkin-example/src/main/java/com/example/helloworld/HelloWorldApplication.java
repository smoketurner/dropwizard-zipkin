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
package com.example.helloworld;

import brave.http.HttpTracing;
import com.example.helloworld.resources.HelloWorldResource;
import com.smoketurner.dropwizard.zipkin.ZipkinBundle;
import com.smoketurner.dropwizard.zipkin.ZipkinFactory;
import com.smoketurner.dropwizard.zipkin.client.ZipkinClientBuilder;
import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.Optional;
import javax.ws.rs.client.Client;

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {

  private ZipkinBundle<HelloWorldConfiguration> zipkinBundle;

  public static void main(String[] args) throws Exception {
    new HelloWorldApplication().run(args);
  }

  @Override
  public String getName() {
    return "hello-world";
  }

  @Override
  public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
    zipkinBundle =
        new ZipkinBundle<HelloWorldConfiguration>(getName()) {
          @Override
          public ZipkinFactory getZipkinFactory(HelloWorldConfiguration configuration) {
            return configuration.getZipkin();
          }
        };
    bootstrap.addBundle(zipkinBundle);
  }

  @Override
  public void run(HelloWorldConfiguration configuration, Environment environment) {

    final Optional<HttpTracing> tracing = zipkinBundle.getHttpTracing();

    final JerseyClientBuilder clientBuilder;
    if (tracing.isPresent()) {
      clientBuilder = new ZipkinClientBuilder(environment, tracing.get().clientOf(configuration.getZipkinClient().getServiceName()));
    } else {
      clientBuilder = new JerseyClientBuilder(environment);
    }

    final Client client = clientBuilder.using(configuration.getZipkinClient()).build(getName());

    // Register resources
    final HelloWorldResource resource = new HelloWorldResource(client);
    environment.jersey().register(resource);
  }
}
