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

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.util.Duration;
import java.io.IOException;
import org.junit.Test;

public class HttpZipkinFactoryTest {

  @Test
  public void isDiscoverable() {
    assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
        .contains(HttpZipkinFactory.class);
  }

  @Test
  public void shouldBeConfigurable() throws IOException {
    ObjectMapper mapper =
        new ObjectMapper(new YAMLFactory()).setSubtypeResolver(new DiscoverableSubtypeResolver());

    final ZipkinFactory factory =
        mapper.readValue(
            "enabled: true\n"
                + "collector: http\n"
                + "baseUrl: http://example.com:1234/zipkin\n"
                + "connectTimeout: 1d\n"
                + "readTimeout: 2d\n"
                + "reportTimeout: 3d\n",
            ZipkinFactory.class);
    assertThat(factory).isInstanceOf(HttpZipkinFactory.class);
    HttpZipkinFactory httpFactory = (HttpZipkinFactory) factory;
    assertThat(httpFactory.getBaseUrl()).isEqualTo("http://example.com:1234/zipkin");
    assertThat(httpFactory.getReportTimeout()).isEqualTo(Duration.days(3));
  }
}
