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

import javax.validation.constraints.NotEmpty;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.JerseyClientConfiguration;

public class ZipkinClientConfiguration extends JerseyClientConfiguration {

  private String serviceName;

  @JsonProperty
  public String getServiceName() {
    return serviceName;
  }

  /**
   * Sets {@code span.remoteServiceName} in spans associated with this client.
   *
   * @param serviceName the service name this client will call
   */
  @JsonProperty
  public void setServiceName(@NotEmpty final String serviceName) {
    this.serviceName = serviceName;
  }
}
