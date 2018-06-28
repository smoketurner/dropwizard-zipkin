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
package com.smoketurner.dropwizard.zipkin;

import com.google.common.base.Strings;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.Objects;
import javax.annotation.Nonnull;

public abstract class ZipkinBundle<C extends Configuration>
    implements ConfiguredBundle<C>, ZipkinConfiguration<C> {

  private final String serviceName;

  /**
   * Constructor
   *
   * @param serviceName service name
   */
  public ZipkinBundle(@Nonnull final String serviceName) {
    this.serviceName = Objects.requireNonNull(serviceName);
  }

  @Override
  public void initialize(Bootstrap<?> bootstrap) {
    // nothing to initialize
  }

  @Override
  public void run(final C configuration, final Environment environment) throws Exception {
    final ZipkinFactory braveConfig = getZipkinFactory(configuration);
    if (Strings.isNullOrEmpty(braveConfig.getServiceName())) {
      braveConfig.setServiceName(serviceName);
    }
  }
}
