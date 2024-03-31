package net.jonathangiles.tools.teenyhttpd.configuration;

import net.jonathangiles.tools.teenyhttpd.annotations.Configuration;
import net.jonathangiles.tools.teenyhttpd.annotations.EventListener;
import net.jonathangiles.tools.teenyhttpd.annotations.Get;
import net.jonathangiles.tools.teenyhttpd.annotations.OnApplicationReady;
import net.jonathangiles.tools.teenyhttpd.implementation.EndpointMapping;
import net.jonathangiles.tools.teenyhttpd.model.TypedResponse;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class OpenApiConfiguration {

    private String specification;
    private final List<EndpointMapping> endpoints = new LinkedList<>();

    @Get("/docs/openapi")
    public TypedResponse<String> getSpecification() {
        if (specification == null) return TypedResponse.notFound();
        return TypedResponse.ok(specification);
    }

    @EventListener
    public void addEndpoint(EndpointMapping method) {
        endpoints.add(method);
    }

    @OnApplicationReady
    public void buildSpecification() {
        if (specification != null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"openapi\": \"3.0.0\",\n");
        sb.append("  \"info\": {\n");
        sb.append("    \"title\": \"TeenyHTTPd API\",\n");
        sb.append("    \"version\": \"1.0.0\"\n");
        sb.append("  },\n");
        sb.append("  \"paths\": {\n");

        Map<String, List<EndpointMapping>> pathMap = endpoints.stream()
                .collect(Collectors.groupingBy(EndpointMapping::getPath));

        String paths = pathMap.entrySet().stream()
                .map(entry -> "\"" + entry.getKey() + "\"" + ": {\n" + build(entry.getValue()) + "    }\n")
                .collect(Collectors.joining(", "));

        sb.append(paths);

        sb.append("  }\n");
        sb.append("}\n");

        specification = sb.toString();
    }

    private String build(List<EndpointMapping> paths) {
        return paths.stream()
                .map(mapping -> "      \"" + mapping.getMethod().name().toLowerCase() + "\": {\n" +
                        "        \"operationId\": \"" + mapping.getTarget().getName() + "\",\n" +
                        "        \"summary\": \"" + mapping.getPath() + "\",\n" +
                        "        \"responses\": {\n" +
                        "          \"200\": {\n" +
                        "            \"description\": \"OK\"\n" +
                        "          }\n" +
                        "        }\n" +
                        "      }\n" +
                        "    ").collect(Collectors.joining(",\n"));
    }
}
