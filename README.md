Dropwizard Zipkin Bundle
========================
[![Build Status](https://travis-ci.org/smoketurner/dropwizard-zipkin.svg?branch=master)](https://travis-ci.org/smoketurner/dropwizard-zipkin)
[![Coverage Status](https://coveralls.io/repos/smoketurner/dropwizard-zipkin/badge.svg)](https://coveralls.io/r/smoketurner/dropwizard-zipkin)
[![Maven Central](https://img.shields.io/maven-central/v/com.smoketurner.dropwizard/dropwizard-zipkin.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/com.smoketurner.dropwizard/dropwizard-zipkin/)
[![GitHub license](https://img.shields.io/github/license/smoketurner/dropwizard-zipkin.svg?style=flat-square)](https://github.com/smoketurner/dropwizard-zipkin/tree/master)
[![Become a Patron](https://img.shields.io/badge/Patron-Patreon-red.svg)](https://www.patreon.com/bePatron?u=9567343)

A bundle for submitting tracing data to [Zipkin](http://zipkin.io) from Dropwizard applications. Internally, this library uses [Brave](https://github.com/openzipkin/brave) to interface a Zipkin collector.

Dependency Info
---------------
```xml
<dependency>
    <groupId>com.smoketurner.dropwizard</groupId>
    <artifactId>zipkin-core</artifactId>
    <version>1.3.1-1</version>
</dependency>
```

Beginning with v1.2.2-4, if you are using the Kafka sender, you must explicitly add the `kafka-clients` dependency into your `pom.xml` as it has been excluded from the `zipkin-core` module.

```xml
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>0.11.0.2</version>
</dependency>
```

Usage
-----
Add a `ZipkinBundle` to your [Application](http://www.dropwizard.io/1.3.1/dropwizard-core/apidocs/io/dropwizard/Application.html) class.

```java
@Override
public void initialize(Bootstrap<MyConfiguration> bootstrap) {
    // ...
    bootstrap.addBundle(new ZipkinBundle<MyConfiguration>(getName()) {
        @Override
        public ZipkinFactory getZipkinFactory(MyConfiguration configuration) {
            return configuration.getZipkinFactory();
        }
    });
}

@Override
public void run(MyConfiguration configuration, Environment environment) throws Exception {
    Optional<HttpTracing> tracing = configuration.getZipkinFactory().build(environment);
}
```

Configuration
-------------
For configuring the Zipkin connection, there is a `ZipkinFactory`:

```yaml
zipkin:

  # Required properties
  # Whether tracing is enabled or not (defaults to true)
  enabled: true
  # Listening IP address of the service
  serviceHost: 192.168.1.100
  # Listening port of the service
  servicePort: 8080

  # Optional properties
  # Span collector to use (console, http, kafka or empty)
  collector: http
  # If using the http collector, provide the baseUrl
  baseUrl: http://127.0.0.1:9411/
  # If using the http collector, milliseconds before timing out when connecting (defaults to null)
  connectTimout: 10000
  # If using the http collector, milliseconds before timing out when reading the response (defaults to null)
  readTimeout: 60000
  # If using the kafka collector, provide the Kafka bootstrap servers
  bootstrapServers: 127.0.0.1:9092;10.0.1.1:9092
```

Example Application
-------------------
This bundle includes a modified version of the `HelloWorldApplication` from Dropwizard's [Getting Started](http://www.dropwizard.io/1.3.1/docs/getting-started.html) documentation.


You can execute this application by first starting Zipkin on your local machine then running:

```
mvn clean package
java -jar zipkin-example/target/zipkin-example-1.3.1-2-SNAPSHOT.jar server zipkin-example/hello-world.yml
```

This will start the application on port `8080` (admin port `8180`). This application demonstrations the following Zipkin integration points:

- You can use the included `ZipkinClientBuilder` to construct instrumented `JerseyClient`'s that will send span traces to Zipkin
- The service will send request and response traces to Zipkin

Support
-------
Please file bug reports and feature requests in [GitHub issues](https://github.com/smoketurner/dropwizard-zipkin/issues).

License
-------
Copyright (c) 2018 Smoke Turner, LLC

This library is licensed under the Apache License, Version 2.0.

See http://www.apache.org/licenses/LICENSE-2.0.html or the [LICENSE](LICENSE) file in this repository for the full license text.
