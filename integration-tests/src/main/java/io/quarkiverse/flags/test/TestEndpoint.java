package io.quarkiverse.flags.test;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.HttpHeaders;

import io.quarkiverse.flags.Flags;
import io.quarkus.qute.TemplateInstance;

@Path("test")
public class TestEndpoint {

    @Inject
    Flags flags;

    @GET
    public String get(HttpHeaders headers) {
        if (flags.isEnabled("test.render-template")) {
            return new template(headers.getRequestHeaders().entrySet()
                    .stream().map(e -> e.getKey() + ": " + e.getValue())
                    .toList()).render();
        } else {
            return "User cannot render template";
        }
    }

    record template(List<String> headers) implements TemplateInstance {
    }

}
