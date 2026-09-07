package com.elsevier.cardiac_api_gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Merges all 4 downstream services' OpenAPI documents (fetched from the Gateway's own
 * /api-docs/{service} passthrough routes in GatewayRoutesConfig) into a single combined
 * document, served at /api-docs/unified. Configured as Swagger UI's one and only "url"
 * (springdoc.swagger-ui.url, not the multi-doc "urls" list) in application.yaml, so the
 * Gateway's /swagger-ui/index.html shows every endpoint from every service on one page -
 * no "Select a definition" dropdown, no separate Authorize state per service. Endpoints
 * still group visually by each service's own @Tag ("Auth", "Profile", "Diagnosis",
 * "Bookmarks"), which is how this single page still reads as organized sections rather
 * than one flat unstructured list.
 *
 * Each downstream path is rewritten to its real external Gateway path (e.g. Profile
 * Service's own "/profile" becomes "/api/profile" here, matching
 * GatewayRoutesConfig's actual routes) so "Try it out" calls resolve correctly against
 * the single merged server ("" - relative to whatever origin served this page, i.e. the
 * Gateway). Auth Service's paths need no rewrite since its controller already declares
 * the full "/api/auth/..." path and the Gateway forwards it unchanged (see
 * GatewayRoutesConfig - it's the only route with no stripPrefix).
 *
 * Component schemas are prefixed per-service (e.g. "Auth_RegisterRequest" vs
 * "Profile_ProfileResponseDto") before merging, since two services could otherwise
 * declare same-named-but-different schemas; every "$ref" pointing at a renamed schema
 * is rewritten to match. All 4 services share one identical "bearerAuth" security
 * scheme definition, so only one copy of it ends up in the merged document - one
 * Authorize button, one token, covering every operation's existing per-endpoint
 * security requirement (only the operations that actually need a JWT show the lock,
 * exactly as each service's own OpenApiConfig/controller already defined it).
 */
@RestController
public class AggregatedApiDocsController {

    private record ServiceSpec(String docsPath, String pathPrefix, String schemaPrefix) {
    }

    private static final Set<String> HTTP_METHODS =
            Set.of("get", "post", "put", "delete", "patch", "options", "head", "trace");

    private static final ServiceSpec[] SERVICES = {
            new ServiceSpec("/api-docs/auth", "", "Auth"),
            new ServiceSpec("/api-docs/profile", "/api", "Profile"),
            new ServiceSpec("/api-docs/diagnosis", "/api", "Diagnosis"),
            new ServiceSpec("/api-docs/bookmark", "/api", "Bookmark"),
    };

    @Value("${server.port}")
    private String serverPort;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping(value = "/api-docs/unified", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> unifiedApiDocs() throws com.fasterxml.jackson.core.JsonProcessingException {

        ObjectNode merged = objectMapper.createObjectNode();
        merged.put("openapi", "3.1.0");

        ObjectNode info = merged.putObject("info");
        info.put("title", "Cardiac Diagnostics System API");
        info.put("version", "v1");
        info.put("description",
                "Unified API across the Auth, Profile, Diagnosis, and Bookmark services, "
                        + "accessed through the API Gateway. Endpoints below are grouped by "
                        + "service - use the tag sections to jump between them.");

        merged.putArray("servers").addObject().put("url", "");

        ObjectNode paths = merged.putObject("paths");
        ObjectNode components = merged.putObject("components");
        ObjectNode schemas = components.putObject("schemas");
        ObjectNode securitySchemes = components.putObject("securitySchemes");

        for (ServiceSpec service : SERVICES) {
            mergeService(service, paths, schemas, securitySchemes);
        }

        // Serialize with our own ObjectMapper rather than returning the JsonNode directly -
        // Spring's auto Jackson message converter in this Spring Boot 4 setup doesn't
        // tree-serialize com.fasterxml.jackson.databind.JsonNode correctly (it falls back
        // to reflecting over JsonNode's own isXxx() getters, producing garbage like
        // {"array":false,"bigDecimal":false,...} instead of the actual document).
        return ResponseEntity.ok(objectMapper.writeValueAsString(merged));
    }

    private void mergeService(
            ServiceSpec service,
            ObjectNode mergedPaths,
            ObjectNode mergedSchemas,
            ObjectNode mergedSecuritySchemes
    ) {
        JsonNode root;

        try {
            String url = "http://localhost:" + serverPort + service.docsPath();
            String rawJson = restTemplate.getForObject(url, String.class);
            root = objectMapper.readTree(rawJson);
        } catch (Exception exception) {
            // One unreachable downstream service shouldn't break the whole unified
            // document - its endpoints just won't appear this time.
            return;
        }

        Map<String, String> renameMap = new LinkedHashMap<>();
        JsonNode schemasNode = root.at("/components/schemas");

        if (schemasNode.isObject()) {
            schemasNode.fields().forEachRemaining(entry ->
                    renameMap.put(entry.getKey(), service.schemaPrefix() + "_" + entry.getKey()));
        }

        JsonNode securitySchemesNode = root.at("/components/securitySchemes");
        if (securitySchemesNode.isObject()) {
            securitySchemesNode.fields().forEachRemaining(entry ->
                    mergedSecuritySchemes.set(entry.getKey(), entry.getValue()));
        }

        JsonNode globalSecurity = root.get("security");
        JsonNode pathsNode = root.get("paths");
        if (pathsNode != null && pathsNode.isObject()) {
            pathsNode.fields().forEachRemaining(entry -> {
                String rewrittenPath = service.pathPrefix() + entry.getKey();
                JsonNode rewritten = rewriteSchemaRefs(entry.getValue(), renameMap);
                if (rewritten.isObject()) {
                    applyDefaultSecurity((ObjectNode) rewritten, globalSecurity);
                }
                mergedPaths.set(rewrittenPath, rewritten);
            });
        }

        if (schemasNode.isObject()) {
            schemasNode.fields().forEachRemaining(entry -> {
                String prefixedName = renameMap.get(entry.getKey());
                mergedSchemas.set(prefixedName, rewriteSchemaRefs(entry.getValue(), renameMap));
            });
        }
    }

    /**
     * Applies a service's global default `security` requirement to every operation in this
     * path item that doesn't declare its own. A service-level `security` field (as set by
     * each service's OpenApiConfig via addSecurityItem) implicitly covers every operation
     * that omits its own `security`, but this merge only ever copied `paths` and
     * `components` - the global default itself was silently dropped, so any endpoint
     * relying on it lost its auth requirement in the unified spec and Swagger UI stopped
     * attaching the bearer token for it.
     */
    private void applyDefaultSecurity(ObjectNode pathItem, JsonNode globalSecurity) {
        if (globalSecurity == null || !globalSecurity.isArray() || globalSecurity.isEmpty()) {
            return;
        }
        pathItem.fields().forEachRemaining(entry -> {
            if (HTTP_METHODS.contains(entry.getKey()) && entry.getValue().isObject()) {
                ObjectNode operation = (ObjectNode) entry.getValue();
                if (!operation.has("security")) {
                    operation.set("security", globalSecurity.deepCopy());
                }
            }
        });
    }

    /** Recursively rewrites every "$ref": "#/components/schemas/X" to X's renamed key. */
    private JsonNode rewriteSchemaRefs(JsonNode node, Map<String, String> renameMap) {

        if (node.isObject()) {
            ObjectNode result = objectMapper.createObjectNode();

            node.fields().forEachRemaining(entry -> {
                if (entry.getKey().equals("$ref") && entry.getValue().isTextual()) {
                    String ref = entry.getValue().asText();
                    String prefix = "#/components/schemas/";

                    if (ref.startsWith(prefix)) {
                        String originalName = ref.substring(prefix.length());
                        String renamed = renameMap.getOrDefault(originalName, originalName);
                        result.put("$ref", prefix + renamed);
                        return;
                    }
                }
                result.set(entry.getKey(), rewriteSchemaRefs(entry.getValue(), renameMap));
            });

            return result;
        }

        if (node.isArray()) {
            ArrayNode result = objectMapper.createArrayNode();
            node.forEach(child -> result.add(rewriteSchemaRefs(child, renameMap)));
            return result;
        }

        return node;
    }
}
