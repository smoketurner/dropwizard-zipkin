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
package com.smoketurner.dropwizard.zipkin;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.kristofa.brave.Brave;
import io.dropwizard.jackson.Discoverable;
import io.dropwizard.setup.Environment;

/**
 * A factory for building {@link Brave} instances for Dropwizard applications.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "collector",
              defaultImpl = LoggingZipkinFactory.class)
public interface ZipkinFactory extends Discoverable {

    /**
     * Build a Brave instance for the given Dropwizard application.
     *
     * @param environment
     *            the application's environment
     * @return a {@link Brave} instance
     */
    Brave build(Environment environment);

    /**
     * Set the name of this service.
     *
     * @param name
     *            Service name
     */
    void setServiceName(String name);
}
