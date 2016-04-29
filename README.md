Dropwizard Zipkin Bundle
========================
[![Build Status](https://travis-ci.org/smoketurner/dropwizard-zipkin.svg?branch=master)](https://travis-ci.org/smoketurner/dropwizard-zipkin)
[![Coverage Status](https://coveralls.io/repos/smoketurner/dropwizard-zipkin/badge.svg)](https://coveralls.io/r/smoketurner/dropwizard-zipkin)
[![Maven Central](https://img.shields.io/maven-central/v/com.smoketurner.dropwizard/dropwizard-zipkin.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/com.smoketurner.dropwizard/dropwizard-zipkin/)
[![GitHub license](https://img.shields.io/github/license/smoketurner/dropwizard-zipkin.svg?style=flat-square)](https://github.com/smoketurner/dropwizard-zipkin/tree/master)

A bundle for submitting tracing data to [Zipkin](http://zipkin.io) from Dropwizard applications. Internally, this library uses [Brave](https://github.com/openzipkin/brave) to interface a Zipkin collector.

Dependency Info
---------------
```xml
<dependency>
    <groupId>com.smoketurner.dropwizard</groupId>
    <artifactId>zipkin-core</artifactId>
    <version>0.9.2-5</version>
</dependency>
```

Usage
-----
Add a `ZipkinBundle` to your [Application](http://www.dropwizard.io/0.9.2/dropwizard-core/apidocs/io/dropwizard/Application.html) class.

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
    Brave brave = configuration.getZipkinFactory().build(environment);
}
```

Configuration
-------------
For configuring the Zipkin connection, there is a `ZipkinFactory`:

```yaml
zipkin:

  # Required properties
  # Listening IP address of the service
  serviceHost: 192.168.1.100
  # Listening port of the service
  servicePort: 8080

  # Optional properties
  # Span collector to use (scribe, logging, or empty)
  collector: scribe
  # If using the scribe collector, provide the scribe endpoint
  endpoint: 127.0.0.1:9410
```

Example Application
-------------------
This bundle includes a modified version of the `HelloWorldApplication` from Dropwizard's [Getting Started](http://www.dropwizard.io/0.9.2/docs/getting-started.html) documentation.

```xml
<dependency>
    <groupId>com.smoketurner.dropwizard</groupId>
    <artifactId>zipkin-example</artifactId>
    <version>0.9.2-3</version>
</dependency>
```

You can execute this application by first starting Zipkin on your local machine then running:

```
mvn clean package
java -jar zipkin-example/target/zipkin-example-0.9.2-3.jar server zipkin-example/hello-world.yml
```

This will start the application on port `8080` (admin port `8180`). This application demonstrations the following Zipkin integration points:

- You can use the included `ZipkinClientBuilder` to construct instrumented `JerseyClient`'s that will send span traces to Zipkin
- The service will send request and response traces to Zipkin

Support
-------
Please file bug reports and feature requests in [GitHub issues](https://github.com/smoketurner/dropwizard-zipkin/issues).

License
-------
Copyright (c) 2016 Justin Plock

This library is licensed under the Apache License, Version 2.0.

See http://www.apache.org/licenses/LICENSE-2.0.html or the [LICENSE](LICENSE) file in this repository for the full license text.
