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

import brave.http.HttpTracing;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.assertj.core.api.Assertions;
import org.junit.ClassRule;
import org.junit.Test;

public class ZipkinBundleTest {
  @ClassRule
  public static DropwizardAppRule<Conf> app =
      new DropwizardAppRule<>(App.class, ResourceHelpers.resourceFilePath("test.yaml"));

  @Test
  public void shouldHaveInitializedZipkin() {
    Assertions.assertThat(getApp().httpTracing).isPresent();
  }

  private App getApp() {
    return (App) app.getApplication();
  }

  public static class App extends Application<Conf> {
    public ZipkinBundle<Conf> bundle;
    public Optional<HttpTracing> httpTracing;

    @Override
    public void initialize(Bootstrap<Conf> bootstrap) {
      bundle =
          new ZipkinBundle<Conf>("test") {
            @Override
            public ZipkinFactory getZipkinFactory(Conf configuration) {
              return configuration.zipkin;
            }
          };
      bootstrap.addBundle(bundle);
    }

    @Override
    public void run(Conf conf, Environment environment) {
      httpTracing = bundle.getHttpTracing();
    }
  }

  public static class Conf extends Configuration {
    @Valid @NotNull public final ZipkinFactory zipkin = new ConsoleZipkinFactory();
  }
}
