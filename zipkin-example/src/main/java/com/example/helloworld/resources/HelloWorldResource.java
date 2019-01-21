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
package com.example.helloworld.resources;

import com.codahale.metrics.annotation.Timed;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path("/")
public class HelloWorldResource {
  private final Client client;

  public HelloWorldResource(Client client) {
    this.client = Objects.requireNonNull(client);
  }

  @GET
  @Timed
  @Path("/fetch")
  public Response fetch(@Context HttpServletRequest request) throws InterruptedException {
    final Random random = new Random();
    TimeUnit.MILLISECONDS.sleep(random.nextInt(1000));
    return client.target("http://127.0.0.1:" + request.getServerPort() + "/result").request().get();
  }

  @GET
  @Timed
  @Path("/result")
  public Response result() throws InterruptedException {
    final Random random = new Random();
    TimeUnit.MILLISECONDS.sleep(random.nextInt(1000));
    return Response.noContent().build();
  }
}
