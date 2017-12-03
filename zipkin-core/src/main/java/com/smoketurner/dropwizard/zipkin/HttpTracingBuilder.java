package com.smoketurner.dropwizard.zipkin;

import brave.Tracing;
import brave.http.HttpTracing;

public interface HttpTracingBuilder {
    HttpTracing build(Tracing tracing);

    public static HttpTracingBuilder DEFAULT = new HttpTracingBuilder() {

        @Override
        public HttpTracing build(Tracing tracing) {
            return HttpTracing.create(tracing);
        }
    };
}
