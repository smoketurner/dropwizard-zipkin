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
import static org.assertj.core.api.Assertions.entry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.util.Duration;
import java.io.IOException;
import org.junit.Test;

public class KafkaZipkinFactoryTest {

  @Test
  public void isDiscoverable() {
    assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
        .contains(KafkaZipkinFactory.class);
  }

  @Test
  public void shouldBeConfigurable() throws IOException {
    ObjectMapper mapper =
        new ObjectMapper(new YAMLFactory()).setSubtypeResolver(new DiscoverableSubtypeResolver());

    final ZipkinFactory factory =
        mapper.readValue(
            "enabled: true\n"
                + "collector: kafka\n"
                + "bootstrapServers: example.com:1234\n"
                + "topic: foo\n"
                + "overrides:\n"
                + "  acks: all\n"
                + "reportTimeout: 3d\n",
            ZipkinFactory.class);
    assertThat(factory).isInstanceOf(KafkaZipkinFactory.class);
    KafkaZipkinFactory kafkaFactory = (KafkaZipkinFactory) factory;
    assertThat(kafkaFactory.getBootstrapServers()).isEqualTo("example.com:1234");
    assertThat(kafkaFactory.getTopic()).isEqualTo("foo");
    assertThat(kafkaFactory.getOverrides()).containsExactly(entry("acks", "all"));
    assertThat(kafkaFactory.getReportTimeout()).isEqualTo(Duration.days(3));
  }
}
