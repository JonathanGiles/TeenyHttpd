package net.jonathangiles.tools.teenyhttpd.configuration;

import net.jonathangiles.tools.teenyhttpd.annotations.EventListener;
import net.jonathangiles.tools.teenyhttpd.annotations.*;
import net.jonathangiles.tools.teenyhttpd.annotations.doc.Required;
import net.jonathangiles.tools.teenyhttpd.annotations.doc.Schema;
import net.jonathangiles.tools.teenyhttpd.annotations.doc.Tag;
import net.jonathangiles.tools.teenyhttpd.annotations.doc.Tags;
import net.jonathangiles.tools.teenyhttpd.implementation.EndpointMapping;
import net.jonathangiles.tools.teenyhttpd.implementation.ReflectionUtils;
import net.jonathangiles.tools.teenyhttpd.json.JsonAlias;
import net.jonathangiles.tools.teenyhttpd.json.JsonIncludeNonNull;
import net.jonathangiles.tools.teenyhttpd.json.TeenyJson;
import net.jonathangiles.tools.teenyhttpd.model.Header;
import net.jonathangiles.tools.teenyhttpd.model.Request;
import net.jonathangiles.tools.teenyhttpd.model.StatusCode;
import net.jonathangiles.tools.teenyhttpd.model.TypedResponse;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
public class OpenApiConfiguration {

    private String specification;
    private List<EndpointMapping> endpoints = new LinkedList<>();
    private Set<Class<?>> dtos = new HashSet<>();

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

        ApiDoc apiDoc = new ApiDoc();
        apiDoc.setInfo(buildInfo());

        Map<String, List<EndpointMapping>> pathMap = endpoints.stream()
                .collect(Collectors.groupingBy(EndpointMapping::getPath));

        for (Map.Entry<String, List<EndpointMapping>> entry : pathMap.entrySet()) {
            apiDoc.putPath(entry.getKey(), buildPathDoc(entry.getValue()));
        }

        apiDoc.putComponent("schemas", dtos.stream()
                .filter(clazz -> !clazz.getName().startsWith("java.") && !clazz.isPrimitive())
                .collect(Collectors.toMap(Class::getSimpleName, this::buildSchemaDoc)));

        specification = new TeenyJson().writeValueAsString(apiDoc);

        endpoints.clear();
        dtos.clear();
        endpoints = null;
        dtos = null;
    }

    private InfoDoc buildInfo() {
        return new InfoDoc("TeenyHttpd", "1.0.0");
    }

    private Map<String, MethodDoc> buildPathDoc(List<EndpointMapping> paths) {
        return paths.stream()
                .collect(Collectors.toMap(m -> m.getMethod().name().toLowerCase(), this::buildMethodDoc));
    }

    private MethodDoc buildMethodDoc(EndpointMapping mapping) {
        for (Parameter parameter : mapping.getParameters()) {
            if (parameter.isAnnotationPresent(RequestBody.class)) {
                dtos.add(parameter.getType());
            }
        }

        MethodDoc doc = new MethodDoc(mapping.getTarget().getName(), buildSummary(mapping), buildParameters(mapping),
                buildResponses(mapping), buildTags(mapping), buildRequestBody(mapping));

        if (mapping.getTarget().isAnnotationPresent(Deprecated.class)) {
            doc.setDeprecated(true);
        }

        return doc;
    }

    private Map<String, Object> buildRequestBody(EndpointMapping mapping) {
        if (mapping.getTarget().getParameterCount() == 0) {
            return null;
        }

        Parameter parameter = null;

        for (Parameter param : mapping.getTarget().getParameters()) {
            if (param.isAnnotationPresent(RequestBody.class)) {
                parameter = param;
                break;
            }
        }

        if (parameter == null) {
            return null;
        }

        dtos.add(parameter.getType());

        SchemaDoc schemaDoc = buildSchemaFromField(parameter);

        return Map.of("content", Map.of(mapping.getContentType(), Map.of("schema", schemaDoc)),
                "required", true);
    }

    private SchemaDoc buildSchemaDoc(Class<?> clazz) {
        SchemaDoc schema = new SchemaDoc();
        schema.setType("object");

        for (Field field : clazz.getDeclaredFields()) {

            if (field.isAnnotationPresent(Required.class)) {
                schema.addRequired(field.getName());
            }

            schema.addProperty(field.getName(), buildPropertyDoc(field));

            if (field.isAnnotationPresent(Schema.class)) {
                Schema schemaAnnotation = field.getAnnotation(Schema.class);

                if (!schemaAnnotation.pattern().isEmpty())
                    schema.setPattern(schemaAnnotation.pattern());

                if (!schemaAnnotation.format().isEmpty())
                    schema.setFormat(schemaAnnotation.format());

                if (!schemaAnnotation.minimum().isEmpty())
                    schema.setMinimum(schemaAnnotation.minimum());

                if (!schemaAnnotation.maximum().isEmpty())
                    schema.setMaximum(schemaAnnotation.maximum());

                if (!schemaAnnotation.minimum().isEmpty()) {
                    schema.setMinLength(schemaAnnotation.minLength());
                }
                if (!schemaAnnotation.maximum().isEmpty()) {
                    schema.setMaxLength(schemaAnnotation.maxLength());
                }
            }
        }

        return schema;
    }

    public Class<?> getReturnType(Method method) {
        Class<?> type = method.getReturnType();

        if (type == void.class || type == Void.class) {
            return String.class;
        } else if (type == boolean.class || type == Boolean.class) {
            return String.class;
        } else if (type == StatusCode.class) {
            return String.class;
        }

        if (method.getGenericReturnType() instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) method.getGenericReturnType();

            if (parameterizedType.getActualTypeArguments()[0] instanceof ParameterizedType) {
                return ReflectionUtils.getParameterType(parameterizedType.getActualTypeArguments()[0])
                        .getFirstType();
            }

            return ReflectionUtils.getParameterType(parameterizedType)
                    .getFirstType();

        }

        return type;

    }

    private SchemaDoc buildPropertyDoc(Field field) {
        SchemaDoc schemaDoc = buildSchemaFromField(field);

        if (field.isAnnotationPresent(Schema.class)) {
            Schema schema = field.getAnnotation(Schema.class);
            schemaDoc.setFormat(schema.format());
        }

        return schemaDoc;
    }

    private List<ParameterDoc> buildParameters(EndpointMapping mapping) {
        List<ParameterDoc> parameterList = Arrays.stream(mapping.getTarget()
                        .getParameters())
                .filter(a -> a.isAnnotationPresent(PathParam.class) || a.isAnnotationPresent(QueryParam.class) || a.isAnnotationPresent(RequestHeader.class))
                .map(this::buildParameterDoc)
                .collect(Collectors.toList());

        if (parameterList.isEmpty()) return null;

        return parameterList;
    }

    private ParameterDoc buildParameterDoc(Parameter field) {
        SchemaDoc schemaDoc = buildSchemaFromField(field);

        ParameterDoc parameterDoc = new ParameterDoc(schemaDoc);
        parameterDoc.setName(field.getName());

        if (field.isAnnotationPresent(PathParam.class)) {
            parameterDoc.setIn("path");
            parameterDoc.setRequired(true);
            parameterDoc.setName(field.getAnnotation(PathParam.class).value());
        } else if (field.isAnnotationPresent(QueryParam.class)) {
            parameterDoc.setIn("query");

            QueryParam queryParam = field.getAnnotation(QueryParam.class);
            parameterDoc.setName(queryParam.value());
            if (queryParam.required()) {
                parameterDoc.setRequired(true);
            }

        } else if (field.isAnnotationPresent(RequestHeader.class)) {
            parameterDoc.setIn("header");
            parameterDoc.setName(field.getAnnotation(RequestHeader.class).value());
        }

        return parameterDoc;
    }


    private SchemaDoc buildSchemaFromField(Field field) {
        if (field.getType().isArray()) {
            SchemaDoc schemaDoc = new SchemaDoc();
            schemaDoc.setType("array");
            schemaDoc.setUniqueItems(false);
            schemaDoc.setItems(buildSchema(field.getType().getComponentType()));
            return schemaDoc;
        }

        if (field.getType() == List.class || field.getType() == Set.class) {
            SchemaDoc schemaDoc = new SchemaDoc();
            schemaDoc.setType("array");
            schemaDoc.setUniqueItems(field.getType() == Set.class);
            schemaDoc.setItems(buildSchema(ReflectionUtils.getParameterType(field.getGenericType()).getFirstType()));
            return schemaDoc;
        }

        return buildSchema(field.getType());
    }

    private SchemaDoc buildSchemaFromField(Parameter parameter) {
        return buildSchema(parameter.getType());
    }


    private SchemaDoc buildSchema(Class<?> type) {
        SchemaDoc schemaDoc = new SchemaDoc();

        if (type == Request.class) {
            schemaDoc.setType("object");
        } else if (type == void.class || type == Void.class) {
            schemaDoc.setType("string");
        } else if (type == String.class || type == Header.class) {
            schemaDoc.setType("string");
        } else if (LocalDateTime.class == type) {
            schemaDoc.setType("string");
            schemaDoc.setFormat("date-time");
        } else if (LocalDate.class == type) {
            schemaDoc.setType("string");
            schemaDoc.setFormat("date");
        } else if (BigDecimal.class == type) {
            schemaDoc.setType("number");
        } else if (int.class == type || Integer.class == type) {
            schemaDoc.setType("integer");
            schemaDoc.setFormat("int32");
        } else if (long.class == type || Long.class == type) {
            schemaDoc.setType("integer");
            schemaDoc.setFormat("int64");
        } else if (float.class == type || Float.class == type) {
            schemaDoc.setType("number");
            schemaDoc.setFormat("float");
        } else if (double.class == type || Double.class == type) {
            schemaDoc.setType("number");
            schemaDoc.setFormat("double");
        } else if (boolean.class == type || Boolean.class == type) {
            schemaDoc.setType("boolean");
        } else {
            schemaDoc.setRef("#/components/schemas/" + type.getSimpleName());
        }

        return schemaDoc;
    }

    private String buildSummary(EndpointMapping mapping) {
        return null;
    }


    private Map<String, ResponseDoc> buildResponses(EndpointMapping mapping) {
        ResponseDoc responseDoc = new ResponseDoc("Successful response");
        Class<?> returnType = getReturnType(mapping.getTarget());
        dtos.add(returnType);
        SchemaDoc schema = buildSchema(returnType);
        responseDoc.putContent(mapping.getContentType(), schema);

        return Map.of("200", responseDoc);
    }

    private Set<String> buildTags(EndpointMapping mapping) {
        if (mapping.getTarget().isAnnotationPresent(Tags.class)) {
            return Arrays.stream(mapping.getTarget().getAnnotation(Tags.class).value())
                    .map(Tag::value)
                    .collect(Collectors.toSet());
        }

        return Set.of(mapping.getTarget().getDeclaringClass().getSimpleName());
    }

    private static class ApiDoc {
        private final Map<String, Map<String, MethodDoc>> paths;
        private List<TagDoc> tags;
        private List<ServerDoc> servers;
        private final Map<String, Object> components;
        private final String openapi;
        private InfoDoc info;

        public ApiDoc() {
            this.openapi = "3.0.1";
            this.components = new HashMap<>();
            this.paths = new HashMap<>();
        }

        public Map<String, Map<String, MethodDoc>> getPaths() {
            return paths;
        }

        public void putPath(String key, Map<String, MethodDoc> pathDoc) {
            paths.put(key, pathDoc);
        }

        public List<TagDoc> getTags() {
            return tags;
        }

        public void setTags(List<TagDoc> tags) {
            this.tags = tags;
        }

        public List<ServerDoc> getServers() {
            return servers;
        }

        public void setServers(List<ServerDoc> servers) {
            this.servers = servers;
        }

        public Map<String, Object> getComponents() {
            return components;
        }

        public void putComponent(String key, Object object) {
            components.put(key, object);
        }

        public String getOpenapi() {
            return openapi;
        }

        public InfoDoc getInfo() {
            return info;
        }

        public void setInfo(InfoDoc info) {
            this.info = info;
        }
    }

    private static class ParameterDoc {
        private String name;
        private String in;
        private String description;
        private boolean required;
        private final SchemaDoc schema;

        public ParameterDoc(SchemaDoc schema) {
            this.schema = schema;
        }

        public String getName() {
            return name;
        }

        public String getIn() {
            return in;
        }

        public String getDescription() {
            return description;
        }

        public boolean isRequired() {
            return required;
        }

        public SchemaDoc getSchema() {
            return schema;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setIn(String in) {
            this.in = in;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }
    }

    private static class InfoDoc {
        private final String title;
        private final String version;

        public InfoDoc(String title, String version) {
            this.title = title;
            this.version = version;
        }

        public String getTitle() {
            return title;
        }

        public String getVersion() {
            return version;
        }
    }

    private static class TagDoc {
        private final String name;
        private final String description;

        public TagDoc(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }

    private static class ServerDoc {
        private final String url;
        private final String description;

        public ServerDoc(String url, String description) {
            this.url = url;
            this.description = description;
        }

        public String getUrl() {
            return url;
        }

        public String getDescription() {
            return description;
        }
    }


    @JsonIncludeNonNull
    private static class MethodDoc {
        private final String operationId;
        private final String summary;
        private final List<ParameterDoc> parameters;
        private final Map<String, ResponseDoc> responses;
        private final Set<String> tags;
        private final Map<String, Object> requestBody;
        private Boolean deprecated;

        public MethodDoc(String operationId, String summary, List<ParameterDoc> parameters,
                         Map<String, ResponseDoc> responses, Set<String> tags, Map<String, Object> requestBody) {
            this.operationId = operationId;
            this.summary = summary;
            this.parameters = parameters;
            this.responses = responses;
            this.tags = tags;
            this.requestBody = requestBody;
        }

        public void setDeprecated(Boolean deprecated) {
            this.deprecated = deprecated;
        }

        public Boolean getDeprecated() {
            return deprecated;
        }

        public Map<String, Object> getRequestBody() {
            return requestBody;
        }

        public Set<String> getTags() {
            return tags;
        }


        public String getOperationId() {
            return operationId;
        }

        public String getSummary() {
            return summary;
        }

        public List<ParameterDoc> getParameters() {
            return parameters;
        }

        public Map<String, ResponseDoc> getResponses() {
            return responses;
        }
    }

    private static class ResponseDoc {
        private final String description;
        private final Map<String, Map<String, SchemaDoc>> content;

        public ResponseDoc(String description) {
            this.description = description;
            this.content = new HashMap<>();
        }

        public Map<String, Map<String, SchemaDoc>> getContent() {
            return content;
        }

        public void putContent(String key, SchemaDoc schemaDoc) {
            content.put(key, Map.of("schema", schemaDoc));
        }

        public String getDescription() {
            return description;
        }
    }


    @JsonIncludeNonNull
    private static class SchemaDoc {
        private String type;
        private String format;
        private Map<String, SchemaDoc> properties;
        private Set<String> required;
        private Boolean uniqueItems;
        private String ref;
        private SchemaDoc items;
        private Integer maxLength;
        private Integer minLength;
        private String pattern;
        private String minimum;
        private String maximum;

        public SchemaDoc() {
        }

        public void setProperties(Map<String, SchemaDoc> properties) {
            this.properties = properties;
        }

        public Integer getMaxLength() {
            return maxLength;
        }

        public void setMaxLength(Integer maxLength) {
            this.maxLength = maxLength;
        }

        public Integer getMinLength() {
            return minLength;
        }

        public void setMinLength(Integer minLength) {
            this.minLength = minLength;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public String getMinimum() {
            return minimum;
        }

        public void setMinimum(String minimum) {
            this.minimum = minimum;
        }

        public String getMaximum() {
            return maximum;
        }

        public void setMaximum(String maximum) {
            this.maximum = maximum;
        }

        public SchemaDoc getItems() {
            return items;
        }

        public void setItems(SchemaDoc items) {
            this.items = items;
        }

        public void setRequired(Set<String> required) {
            this.required = required;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public void setType(String type) {
            this.type = type;
        }

        @JsonAlias("$ref")
        public String getRef() {
            return ref;
        }

        public void setRef(String ref) {
            this.ref = ref;
        }

        public Boolean getUniqueItems() {
            return uniqueItems;
        }

        public SchemaDoc setUniqueItems(Boolean uniqueItems) {
            this.uniqueItems = uniqueItems;
            return this;
        }

        public void addProperty(String name, SchemaDoc property) {
            if (properties == null) {
                properties = new HashMap<>();
            }

            properties.put(name, property);
        }

        public void addRequired(String name) {
            required.add(name);
        }

        public String getType() {
            return type;
        }

        public String getFormat() {
            return format;
        }

        public Map<String, SchemaDoc> getProperties() {
            return properties;
        }

        public Set<String> getRequired() {
            return required;
        }
    }


}
