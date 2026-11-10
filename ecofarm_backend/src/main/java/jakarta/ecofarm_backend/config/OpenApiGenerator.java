package jakarta.ecofarm_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class OpenApiGenerator {

    private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public static String generateJson(String contextPath) {
        try {
            ObjectNode root = mapper.createObjectNode();
            root.put("openapi", "3.0.3");

            // Info
            ObjectNode info = root.putObject("info");
            info.put("title", "NLU-ECOFARM Backend API");
            info.put("version", "1.0.0");
            info.put("description", "Tài liệu API được quét và sinh tự động từ mã nguồn Jakarta REST");

            ObjectNode contact = info.putObject("contact");
            contact.put("name", "NLU-ECOFARM Team");
            contact.put("email", "ecofarm@nlu.edu.vn");

            // Servers
            ArrayNode servers = root.putArray("servers");
            String ctx = (contextPath != null && !contextPath.isBlank()) ? contextPath : "";
            servers.addObject().put("url", "/").put("description", "Root Path (/)");
            servers.addObject().put("url", ctx + "/api").put("description", "Current Server (/api)");


            // Tags set
            Set<String> tagNames = new LinkedHashSet<>();
            ObjectNode pathsNode = root.putObject("paths");

            // Quét tất cả các class có @Path
            List<Class<?>> resourceClasses = scanResourceClasses();

            for (Class<?> clazz : resourceClasses) {
                Path classPathAnn = clazz.getAnnotation(Path.class);
                if (classPathAnn == null) continue;

                String classPath = normalizePath(classPathAnn.value());
                String defaultTag = clazz.getSimpleName().replace("Resource", "").replace("Controller", "");
                if (clazz.isAnnotationPresent(Tag.class)) {
                    defaultTag = clazz.getAnnotation(Tag.class).name();
                }
                tagNames.add(defaultTag);

                for (Method method : clazz.getDeclaredMethods()) {
                    String httpMethod = getHttpMethod(method);
                    if (httpMethod == null) continue;

                    String methodPath = "";
                    if (method.isAnnotationPresent(Path.class)) {
                        methodPath = normalizePath(method.getAnnotation(Path.class).value());
                    }

                    String fullPath = normalizeFullPath(classPath, methodPath);
                    ObjectNode pathItem = pathsNode.has(fullPath)
                            ? (ObjectNode) pathsNode.get(fullPath)
                            : pathsNode.putObject(fullPath);

                    ObjectNode operation = pathItem.putObject(httpMethod.toLowerCase());

                    // Tags
                    String methodTag = defaultTag;
                    if (method.isAnnotationPresent(Tag.class)) {
                        methodTag = method.getAnnotation(Tag.class).name();
                        tagNames.add(methodTag);
                    }
                    operation.putArray("tags").add(methodTag);

                    // Operation info
                    String summary = method.getName();
                    String description = "";
                    if (method.isAnnotationPresent(Operation.class)) {
                        Operation op = method.getAnnotation(Operation.class);
                        if (!op.summary().isBlank()) summary = op.summary();
                        if (!op.description().isBlank()) description = op.description();
                    }
                    operation.put("summary", summary);
                    if (!description.isBlank()) {
                        operation.put("description", description);
                    }
                    operation.put("operationId", method.getName());

                    // Parameters
                    ArrayNode paramsNode = operation.putArray("parameters");
                    Parameter requestBodyParam = null;

                    for (Parameter p : method.getParameters()) {
                        if (p.isAnnotationPresent(PathParam.class)) {
                            ObjectNode paramObj = paramsNode.addObject();
                            paramObj.put("name", p.getAnnotation(PathParam.class).value());
                            paramObj.put("in", "path");
                            paramObj.put("required", true);
                            paramObj.putObject("schema").put("type", getSwaggerType(p.getType()));
                        } else if (p.isAnnotationPresent(QueryParam.class)) {
                            ObjectNode paramObj = paramsNode.addObject();
                            paramObj.put("name", p.getAnnotation(QueryParam.class).value());
                            paramObj.put("in", "query");
                            paramObj.put("required", false);
                            paramObj.putObject("schema").put("type", getSwaggerType(p.getType()));
                        } else if (p.isAnnotationPresent(HeaderParam.class)) {
                            ObjectNode paramObj = paramsNode.addObject();
                            paramObj.put("name", p.getAnnotation(HeaderParam.class).value());
                            paramObj.put("in", "header");
                            paramObj.put("required", false);
                            paramObj.putObject("schema").put("type", getSwaggerType(p.getType()));
                        } else if (!p.getType().getName().startsWith("jakarta.ws.rs")
                                && !p.getType().getName().startsWith("jakarta.servlet")) {
                            requestBodyParam = p;
                        }
                    }

                    // Request Body
                    if (requestBodyParam != null && ("POST".equalsIgnoreCase(httpMethod) || "PUT".equalsIgnoreCase(httpMethod) || "PATCH".equalsIgnoreCase(httpMethod))) {
                        ObjectNode requestBody = operation.putObject("requestBody");
                        requestBody.put("required", true);
                        ObjectNode content = requestBody.putObject("content");
                        ObjectNode jsonMedia = content.putObject("application/json");
                        ObjectNode schema = jsonMedia.putObject("schema");
                        schema.put("type", "object");
                        schema.put("description", requestBodyParam.getType().getSimpleName());
                    }

                    // Responses
                    ObjectNode responses = operation.putObject("responses");
                    boolean hasCustomResponses = false;

                    if (method.isAnnotationPresent(APIResponses.class)) {
                        for (APIResponse respAnn : method.getAnnotation(APIResponses.class).value()) {
                            ObjectNode r = responses.putObject(respAnn.responseCode());
                            r.put("description", respAnn.description());
                            hasCustomResponses = true;
                        }
                    } else if (method.isAnnotationPresent(APIResponse.class)) {
                        APIResponse respAnn = method.getAnnotation(APIResponse.class);
                        ObjectNode r = responses.putObject(respAnn.responseCode());
                        r.put("description", respAnn.description());
                        hasCustomResponses = true;
                    }

                    if (!hasCustomResponses) {
                        ObjectNode r200 = responses.putObject("200");
                        r200.put("description", "Thành công");
                        ObjectNode content = r200.putObject("content");
                        ObjectNode json = content.putObject("application/json");
                        json.putObject("schema").put("type", "object");
                    }
                }
            }

            // Tags metadata
            ArrayNode tagsArr = root.putArray("tags");
            for (String t : tagNames) {
                tagsArr.addObject().put("name", t);
            }

            // Components & Security Schemes
            ObjectNode components = root.putObject("components");
            ObjectNode secSchemes = components.putObject("securitySchemes");

            ObjectNode bearerAuth = secSchemes.putObject("BearerAuth");
            bearerAuth.put("type", "http");
            bearerAuth.put("scheme", "bearer");
            bearerAuth.put("bearerFormat", "JWT");
            bearerAuth.put("description", "Nhập JWT token theo định dạng: Bearer {token}");

            ObjectNode cookieAuth = secSchemes.putObject("CookieAuth");
            cookieAuth.put("type", "apiKey");
            cookieAuth.put("in", "cookie");
            cookieAuth.put("name", "accept_token");
            cookieAuth.put("description", "Xác thực qua Cookie accept_token");

            ArrayNode security = root.putArray("security");
            security.addObject().putArray("BearerAuth");

            return mapper.writeValueAsString(root);
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private static String getHttpMethod(Method m) {
        if (m.isAnnotationPresent(GET.class)) return "get";
        if (m.isAnnotationPresent(POST.class)) return "post";
        if (m.isAnnotationPresent(PUT.class)) return "put";
        if (m.isAnnotationPresent(DELETE.class)) return "delete";
        if (m.isAnnotationPresent(PATCH.class)) return "patch";
        return null;
    }

    private static String normalizePath(String path) {
        if (path == null) return "";
        path = path.trim();
        if (!path.startsWith("/")) path = "/" + path;
        if (path.endsWith("/") && path.length() > 1) path = path.substring(0, path.length() - 1);
        return path;
    }

    private static String normalizeFullPath(String classPath, String methodPath) {
        String full = classPath + methodPath;
        full = full.replaceAll("//+", "/");
        if (!full.startsWith("/")) full = "/" + full;
        if (full.endsWith("/") && full.length() > 1) full = full.substring(0, full.length() - 1);
        return full;
    }

    private static String getSwaggerType(Class<?> type) {
        if (type == Integer.class || type == int.class) return "integer";
        if (type == Long.class || type == long.class) return "integer";
        if (type == Double.class || type == double.class || type == Float.class || type == float.class) return "number";
        if (type == Boolean.class || type == boolean.class) return "boolean";
        return "string";
    }

    private static List<Class<?>> scanResourceClasses() {
        List<Class<?>> classes = new ArrayList<>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) cl = OpenApiGenerator.class.getClassLoader();

        String[] scanPackages = {"jakarta.ecofarm_backend", "vn.edu.nlu.ecofarm"};
        for (String pkg : scanPackages) {
            try {
                String path = pkg.replace('.', '/');
                Enumeration<URL> resources = cl.getResources(path);
                while (resources.hasMoreElements()) {
                    URL resource = resources.nextElement();
                    if ("file".equals(resource.getProtocol())) {
                        File dir = new File(URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8));
                        if (dir.exists() && dir.isDirectory()) {
                            scanDirectory(dir, pkg, cl, classes);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return classes;
    }

    private static void scanDirectory(File dir, String currentPkg, ClassLoader cl, List<Class<?>> result) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                scanDirectory(f, currentPkg + "." + f.getName(), cl, result);
            } else if (f.getName().endsWith(".class") && !f.getName().contains("$")) {
                String className = currentPkg + "." + f.getName().substring(0, f.getName().length() - 6);
                try {
                    Class<?> clazz = cl.loadClass(className);
                    if (clazz.isAnnotationPresent(Path.class) && !result.contains(clazz)) {
                        result.add(clazz);
                    }
                } catch (Throwable ignored) {
                }
            }
        }
    }
}
