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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smoketurner.dropwizard.zipkin.ConsoleZipkinFactory;
import com.smoketurner.dropwizard.zipkin.ZipkinFactory;
import com.smoketurner.dropwizard.zipkin.client.ZipkinClientConfiguration;
import io.dropwizard.Configuration;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class HelloWorldConfiguration extends Configuration {

  @Valid @NotNull public final ZipkinFactory zipkin = new ConsoleZipkinFactory();

  @Valid @NotNull
  private final ZipkinClientConfiguration zipkinClient = new ZipkinClientConfiguration();

  @JsonProperty
  public ZipkinFactory getZipkin() {
    return zipkin;
  }

  @JsonProperty
  public ZipkinClientConfiguration getZipkinClient() {
    return zipkinClient;
  }
}
