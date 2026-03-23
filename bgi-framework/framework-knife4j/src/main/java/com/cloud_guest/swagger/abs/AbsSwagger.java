package com.cloud_guest.swagger.abs;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.swagger.config.SwaggerConfiguration;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import lombok.extern.slf4j.Slf4j;

import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author yan
 * @Date 2024/7/26 0026 11:47:39
 * @Description
 */
public interface AbsSwagger {
    String authorization = HttpHeaders.AUTHORIZATION;
    String apiGroupName = "api";
    String jwtGroupName = "jwt";
    String otherGroupName = "other";
    String GroupSuffix = "_Group";

    default <T> T defaultIfEmpty(T object, T defaultValue) {
        return ObjectUtil.isEmpty(object) ? defaultValue : object;
    }

    default <T> T defaultIfEmpty(T object) {
        return defaultIfEmpty(object, null);
    }

    default List<String> getPrefixByAuthorization() {
        Environment env = SpringUtil.getBean(Environment.class);
        String property = env.getProperty("authorization.prefix");
        List<String> list = CollUtil.newArrayList("/jwt/", "/test/");
        if (StrUtil.isNotBlank(property)) {
            list = Arrays.stream(property.split(",")).map(o -> {
                String str = "/";
                String strEnd = "**";
                if (!o.startsWith(str)) {
                    o = new StringBuffer(str).append(o).toString();
                }
                if (o.contains(strEnd)) {
                    o = o.replace(strEnd, "");
                }
                if (!o.endsWith(str)) {
                    o = new StringBuffer(o).append(str).toString();
                }
                return o;
            }).collect(Collectors.toList());
        }
        return list;
    }

    default String getAuthorization() {
        return authorization;
    }

    default List<GroupSwagger> buildGroupSwaggerList() {
        String pathApi = new StringBuilder().append("/api/**").toString();
        List<String> pathsApi = CollUtil.newArrayList(pathApi);
        SwaggerParameter sign = new SwaggerParameter()
                .setName("sign").setDescription("签名")
                .setStringSchemaDefault("签名sign")
                .setRequired(true);
        SwaggerParameter timestamp = new SwaggerParameter()
                .setName("timestamp").setDescription("时间戳")
                .setStringSchemaDefault(String.valueOf(System.currentTimeMillis() + 3000))
                .setRequired(true);
        List<SwaggerParameter> swaggerParameters = CollUtil.newArrayList(sign, timestamp);
        //加入全局
        List<Parameter> parameterListApi = swaggerParameters.stream().filter(ObjectUtil::isNotEmpty)
                .map(this::buildHeaderParameter).collect(Collectors.toList());
        GroupSwagger groupSwaggerApi = buildGroup(apiGroupName, false, pathsApi, parameterListApi, null);
        //jwt
        String pathJwt = new StringBuilder().append("/jwt/**").toString();
        List<String> pathsJwt = CollUtil.newArrayList(pathJwt);
        List<Parameter> parameterListJwt = CollUtil.newArrayList(buildSecurityHeaderParameter(getAuthorization()));
        boolean authSecurityGroup = true;
        GroupSwagger groupSwaggerJwt = buildGroup(jwtGroupName, authSecurityGroup, pathsJwt, parameterListJwt, null);
        //other
        String apiPath = new StringBuilder().append("/api/**").toString();
        String jwtPath = new StringBuilder().append("/jwt/**").toString();
        List<String> excludePaths = CollUtil.newArrayList(apiPath, jwtPath);
        GroupSwagger groupSwaggerOther = buildGroup(otherGroupName, false, null, null, excludePaths);

        List<GroupSwagger> list = CollUtil.newArrayList();
        list.add(groupSwaggerApi);
        list.add(groupSwaggerJwt);
        list.add(groupSwaggerOther);


        //SpringDocConfigProperties bean = SpringUtil.getBean(SpringDocConfigProperties.class);

        return list;
    }

    /**
     * 安全模式，这里配置通过请求头 Authorization 传递 token 参数
     */
    default Map<String, SecurityScheme> buildSecuritySchemes() {
        String authorization = getAuthorization();
        Map<String, SecurityScheme> securitySchemes = new HashMap<>();
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY) // 类型
                .name(authorization) // 请求头的 name
                .in(SecurityScheme.In.HEADER); // token 所在位置
        securitySchemes.put(authorization, securityScheme);
        return securitySchemes;
    }
    /*####################################################################################################################################################################################################################################################*/

    default GroupSwagger buildGroup(String groupName, boolean AuthSecurityGroup, List<String> paths, List<Parameter> parameterList, List<String> excludePaths) {
        GroupSwagger groupSwagger = new GroupSwagger()
                .setGroupName(groupName)
                .setAuthSecurityGroup(AuthSecurityGroup)
                .setPaths(paths)
                .setParameterList(parameterList)
                .setExcludePaths(excludePaths);
        return groupSwagger;
    }

    /**
     * 配置api组
     * 注意请和yaml中配置的组名不要重复(俩者配置同时生效)
     *
     * @return
     */

    //@Lazy//懒加载
    default GroupedOpenApi buildApiGroupedOpenApi() {
        GroupSwagger groupSwagger = buildGroupSwaggerList()
                .stream().filter(o -> ObjectUtil.equals(o.getGroupName(), apiGroupName)).findFirst().get();
        GroupedOpenApi groupedOpenApi = buildGroupedOpenApi(groupSwagger);
        return groupedOpenApi;
    }

    /**
     * 配置jwt组
     * 注意请和yaml中配置的组名不要重复(俩者配置同时生效)
     *
     * @return
     */

    //@Lazy//懒加载
    default GroupedOpenApi buildJwtGroupedOpenApi() {
        GroupSwagger groupSwagger = buildGroupSwaggerList()
                .stream().filter(o -> ObjectUtil.equals(o.getGroupName(), jwtGroupName)).findFirst().get();
        GroupedOpenApi groupedOpenApi = buildGroupedOpenApi(groupSwagger);
        return groupedOpenApi;
    }


    /**
     * 配置other组
     * 注意请和yaml中配置的组名不要重复(俩者配置同时生效)
     *
     * @return
     */

    //@Lazy//懒加载
    default GroupedOpenApi buildOtherGroupedOpenApi() {
        GroupSwagger groupSwagger = buildGroupSwaggerList()
                .stream().filter(o -> ObjectUtil.equals(o.getGroupName(), otherGroupName)).findFirst().get();
        GroupedOpenApi groupedOpenApi = buildGroupedOpenApi(groupSwagger);
        return groupedOpenApi;
    }

    @Data
    @Accessors(chain = true)
    @NoArgsConstructor
    @AllArgsConstructor
    class GroupSwagger {
        /**
         * 组名
         */
        String groupName;
        /**
         * 是安全组
         */
        boolean authSecurityGroup;
        /**
         * 路径
         */
        List<String> paths;
        /**
         * 排除路径
         */
        List<String> excludePaths;
        /**
         * 参数
         */
        List<Parameter> parameterList;
    }

    default GroupedOpenApi buildGroupedOpenApi(GroupSwagger groupSwagger) {
        String groupName = groupSwagger.getGroupName();
        List<String> paths = groupSwagger.getPaths();
        List<String> excludePaths = groupSwagger.getExcludePaths();
        List<Parameter> parameterList = groupSwagger.getParameterList();
        boolean authSecurityGroup = groupSwagger.isAuthSecurityGroup();
        return buildGroupedOpenApi(groupName, authSecurityGroup, paths, excludePaths, parameterList);
    }

    /**
     * 配置 ，组|是安全组|指定路径|全局 --必传参数
     *
     * @param groupName         该组名
     * @param authSecurityGroup 是安全组
     * @param paths             该组路径
     * @param excludePaths      该组排除路径
     * @param parameterList     该组参数
     * @return
     */
    default GroupedOpenApi buildGroupedOpenApi(String groupName, boolean authSecurityGroup, List<String> paths, List<String> excludePaths, List<Parameter> parameterList) {
        GroupedOpenApi.Builder builder = GroupedOpenApi.builder();
        if (StrUtil.isNotBlank(groupName)) {
            //加组配置
            builder.group(groupName);
        }
        if (CollUtil.isNotEmpty(paths)) {
            //组路径
            String[] pathsToMatch = paths.toArray(new String[paths.size()]);
            builder.pathsToMatch(pathsToMatch);
        }
        if (CollUtil.isNotEmpty(excludePaths)) {
            //组排除路径
            String[] pathsToExclude = excludePaths.toArray(new String[excludePaths.size()]);
            builder.pathsToExclude(pathsToExclude);
        }
        //全局已加
          /*      // ========== 添加 Bearer 认证配置 ==========
                // 获取安全方案名称（例如从配置文件读取，若无则使用默认值）
                if (authSecurityGroup) {
                    String securitySchemeName = getAuthorization();
                    builder.addOpenApiCustomiser(openApi -> {
                        Components components = openApi.getComponents();
                        if (components == null) {
                            components = new Components();
                            openApi.components(components);
                        }

                        if (!components.getSecuritySchemes().containsKey(securitySchemeName)) {
                            components.addSecuritySchemes(securitySchemeName,
                                    new SecurityScheme()
                                            .type(SecurityScheme.Type.HTTP)
                                            .scheme("bearer")
                                            .bearerFormat("JWT")
                                            .description("请输入 JWT 令牌（无需 Bearer 前缀）")
                            );
                        }

                        List<SecurityRequirement> security = openApi.getSecurity();
                        Boolean hasSecurityRequirement = CollUtil.isNotEmpty(security) && security.stream()
                                .anyMatch(req -> req.containsKey(securitySchemeName));
                        if (!hasSecurityRequirement) {
                            openApi.addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
                        }
                    });

                    builder.addOperationCustomizer((operation, handlerMethod) -> {
                        List<Parameter> parameters = operation.getParameters();
                        Boolean hasAuthParameter = CollUtil.isNotEmpty(parameters) && parameters.stream()
                                .anyMatch(p -> securitySchemeName.equals(p.getName()));
                        if (!hasAuthParameter) {
                            operation.addParametersItem(buildSecurityHeaderParameter(securitySchemeName));
                        }
                        return operation;
                    });
                }


                if (CollUtil.isNotEmpty(parameterList)) {

                    builder.addOperationCustomizer(
                            //加全局变量
                            (operation, handlerMethod) -> {
                                List<Parameter> operationParams = operation.getParameters();
                                for (Parameter parameter : parameterList) {
                                    Boolean hasParameter = CollUtil.isNotEmpty(operationParams) &&
                                            operationParams.stream()
                                                    .anyMatch(p -> ObjectUtil.equals(parameter.getName(), p.getName()));
                                    if (!hasParameter) {
                                        operation.addParametersItem(parameter);
                                    }
                                }
                                return operation;
                            }
                    );
                }*/
        return builder.build();
    }


    /*####################################################################################################################################################################################################################################################*/


    /**
     * 构建 Authorization 认证请求头参数
     * <p>
     * 解决 Knife4j <a href="https://gitee.com/xiaoym/knife4j/issues/I69QBU">Authorize 未生效，请求header里未包含参数</a>
     *
     * @return 认证参数
     */
    default Parameter buildSecurityHeaderParameter(String authorization) {
        Parameter parameter = new Parameter()
                .name(authorization) // header 名
                .description("认证 Token")// 描述
                .in(String.valueOf(SecurityScheme.In.HEADER))// 请求 header
                .schema(new StringSchema()
                        // ._default("Bearer ") // 最好关闭
                        .name(authorization).description("认证 Token"))//
                .required(true)
                .schema(new io.swagger.v3.oas.models.media.StringSchema());
        return parameter;
    }

    @Data
    @Accessors(chain = true)
    @NoArgsConstructor
    @AllArgsConstructor
    class SwaggerParameter {
        /**
         * 名称
         */
        String name = "SwaggerParameter名称";
        /**
         * 描述
         */
        String description = "SwaggerParameter描述";
        /**
         * 请求参数位置
         */
        SecurityScheme.In securitySchemeIn = SecurityScheme.In.HEADER;
        /**
         * schema默认值
         */
        String stringSchemaDefault = null;
        String schemaName = "schemaName";
        /**
         * schema描述
         */
        String schemaDescription = "schema描述";
        /**
         * 是否必须
         */
        Boolean required = false;
    }

    default Parameter buildHeaderParameter(SwaggerParameter swaggerParameter) {
        String name = swaggerParameter.getName();
        String description = swaggerParameter.getDescription();
        SecurityScheme.In securitySchemeIn = swaggerParameter.getSecuritySchemeIn();
        String stringSchemaDefault = swaggerParameter.getStringSchemaDefault();
        String schemaName = swaggerParameter.getSchemaName();
        String schemaDescription = swaggerParameter.getSchemaDescription();
        Boolean required = swaggerParameter.getRequired();
        return buildHeaderParameter(name, description, String.valueOf(securitySchemeIn), stringSchemaDefault, schemaName, schemaDescription)
                .required(required);
    }

    /**
     * 自定义参数
     *
     * @param parameterName        名称
     * @param parameterDescription 描述
     * @param parameterIn          请求参数位置
     * @param schemaDefault        默认值
     * @param schemaName
     * @param schemaDescription    schema描述
     * @return
     */
    default Parameter buildHeaderParameter(String parameterName,
                                           String parameterDescription,
                                           String parameterIn,
                                           String schemaDefault,
                                           String schemaName,
                                           String schemaDescription) {
        // header 名
        parameterName = defaultIfEmpty(parameterName, "header&" + System.currentTimeMillis());
        // 描述
        parameterDescription = defaultIfEmpty(parameterDescription, "认证 Token");
        // 请求 header
        parameterIn = defaultIfEmpty(parameterIn, String.valueOf(SecurityScheme.In.HEADER));
        schemaDefault = defaultIfEmpty(schemaDefault, "StringSchema");
        schemaDescription = defaultIfEmpty(schemaDescription, "schemaDescription");

        StringSchema stringSchema = new StringSchema()
                ._default(schemaDefault);

        Schema schema = stringSchema.name(schemaName)
                .description(schemaDescription);

        Parameter parameter = new Parameter()
                .name(parameterName)
                .description(parameterDescription)
                .in(parameterIn)
                .schema(schema);// 默认：使用用户编号为 1
        return parameter;
    }

    default OpenAPI buildOpenAPI() {
        String authorization = getAuthorization();
        OpenAPI api = new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(authorization))
                .components(new Components()
                        .addSecuritySchemes(authorization, new SecurityScheme().name(authorization)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .in(SecurityScheme.In.HEADER)
                                .description("鉴权token")));
        return api;
    }

    /**
     * 构建 Authorization 认证请求头参数
     * <p>
     * 解决 Knife4j <a href="https://github.com/xiaoymin/knife4j/issues/545">Authorize 未生效，请求header里未包含参数</a>
     * <p>
     * 构建全局 OpenAPI 自定义器，用于配置 API 文档中的安全认证相关信息
     *
     * @return 返回一个 GlobalOpenApiCustomizer 实例，用于自定义 OpenAPI 规范
     */
    default GlobalOpenApiCustomizer buildGlobalOpenApiCustomizer() {
        //return openApi -> {
        //    openApi.getPaths().values().stream()
        //            .flatMap(pathItem -> pathItem.readOperations().stream())
        //            .forEach(operation -> operation.security(openApi.getSecurity()));
        //};
        List<GroupSwagger> groupSwaggerList = buildGroupSwaggerList();
        return openApi -> {
            groupSwaggerList.stream().forEach(group -> {

                List<Parameter> parameterList = group.getParameterList();
                // 既没有认证需求也没有自定义参数时跳过
                if (!group.isAuthSecurityGroup() && CollUtil.isEmpty(parameterList)) {
                    return;
                }
                openApi.getPaths().entrySet().stream()
                        .filter(entry -> {
                            String path = entry.getKey();

                            // 如果同时配置了 paths 和 excludePaths
                            if (CollUtil.isNotEmpty(group.getPaths()) && CollUtil.isNotEmpty(group.getExcludePaths())) {
                                boolean matchesPath = group.getPaths().stream()
                                        .anyMatch(pattern -> path.matches(pattern.replace("**", ".*")));
                                boolean notExcluded = !group.getExcludePaths().stream()
                                        .anyMatch(pattern -> path.matches(pattern.replace("**", ".*")));
                                return matchesPath && notExcluded;
                            }

                            // 只配置了 paths
                            if (CollUtil.isNotEmpty(group.getPaths())) {
                                return group.getPaths().stream()
                                        .anyMatch(pattern -> path.matches(pattern.replace("**", ".*")));
                            }

                            // 只配置了 excludePaths（other 组场景）
                            if (CollUtil.isNotEmpty(group.getExcludePaths())) {
                                return !group.getExcludePaths().stream()
                                        .anyMatch(pattern -> path.matches(pattern.replace("**", ".*")));
                            }

                            // 都没配置，默认包含
                            return true;
                        })
                        .flatMap(entry -> entry.getValue().readOperations().stream())
                        .forEach(operation -> {

                            // 认证配置
                            if (group.isAuthSecurityGroup()) {
                                String securitySchemeName = getAuthorization();
                                //添加认证
                                List<SecurityRequirement> security = operation.getSecurity();
                                Boolean hasSecurityRequirement = CollUtil.isNotEmpty(security) && security.stream()
                                        .anyMatch(req -> req.containsKey(securitySchemeName));
                                if (!hasSecurityRequirement) {
                                    operation.addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
                                }
                            }

                            // 自定义参数配置
                            if (CollUtil.isNotEmpty(group.getParameterList())) {
                                List<Parameter> operationParams = operation.getParameters();
                                parameterList.forEach(parameter -> {
                                    Boolean hasParameter = CollUtil.isNotEmpty(operationParams) &&
                                            operationParams.stream()
                                                    .anyMatch(p -> ObjectUtil.equals(parameter.getName(), p.getName()));
                                    if (!hasParameter) {
                                        operation.addParametersItem(parameter);
                                    }
                                });
                            }
                        });
            });
        };
    }

    default GlobalOpenApiCustomizer buildOpenApiCustomizer() {
        GlobalOpenApiCustomizer customizer = buildGlobalOpenApiCustomizer();
        return customizer;
    }
}
