package com.minimalism.swagger.abs;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * 配置api组
     * 注意请和yaml中配置的组名不要重复(俩者配置同时生效)
     *
     * @return
     */

    //@Lazy//懒加载
    default GroupedOpenApi buildApiGroupedOpenApi() {
        String path = new StringBuilder().append("/api/**").toString();
        List<String> paths = CollUtil.newArrayList(path);

        SwaggerParameter sign = new SwaggerParameter()
                .setName("sign").setDescription("签名")
                .setStringSchemaDefault("签名sign")
                .setRequired(true);

        SwaggerParameter timestamp = new SwaggerParameter()
                .setName("timestamp").setDescription("时间戳")
                .setStringSchemaDefault(String.valueOf(System.currentTimeMillis()+ 3000))
                .setRequired(true);

        List<SwaggerParameter> swaggerParameters = CollUtil.newArrayList(sign, timestamp);
        //加入全局
        List<Parameter> parameterList = swaggerParameters.stream().filter(ObjectUtil::isNotEmpty)
                .map(this::buildHeaderParameter).collect(Collectors.toList());

        //Parameter signParameter = buildHeaderParameter("sign", "验签",
        //        null, "验签1"
        //        , null, null).required(true);
        //
        //Parameter timestampParameter = buildHeaderParameter("timestamp", "时间戳",
        //        null, System.currentTimeMillis() + 3000 + ""
        //        , null, "时间戳1").required(true);
        //
        ////加入全局
        //List<Parameter> parameterList = CollUtil.newArrayList(signParameter, timestampParameter);

        GroupSwagger groupSwagger = new GroupSwagger()
                .setGroupName(apiGroupName)
                .setPaths(paths)
                .setParameterList(parameterList);

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
        String path = new StringBuilder().append("/jwt/**").toString();

        List<String> paths = CollUtil.newArrayList(path);
        List<Parameter> parameterList = CollUtil.newArrayList(buildSecurityHeaderParameter(getAuthorization()));

        GroupSwagger groupSwagger = new GroupSwagger()
                .setGroupName(jwtGroupName)
                .setPaths(paths)
                .setParameterList(parameterList);

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
        //无需验证，无组必传参数
        String apiPath = new StringBuilder().append("/api/**").toString();
        String jwtPath = new StringBuilder().append("/jwt/**").toString();

        List<String> excludePaths = CollUtil.newArrayList(apiPath, jwtPath);
        GroupSwagger groupSwagger = new GroupSwagger()
                .setGroupName(otherGroupName)
                .setExcludePaths(excludePaths);
        GroupedOpenApi groupedOpenApi = buildGroupedOpenApi(groupSwagger);
        return groupedOpenApi;
    }

    @Data @Accessors(chain = true)
    @NoArgsConstructor
    @AllArgsConstructor
    class GroupSwagger {
        /**
         * 组名
         */
        String groupName;
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
    default GroupedOpenApi buildGroupedOpenApi(GroupSwagger groupSwagger){
        String groupName = groupSwagger.getGroupName();
        List<String> paths = groupSwagger.getPaths();
        List<String> excludePaths = groupSwagger.getExcludePaths();
        List<Parameter> parameterList = groupSwagger.getParameterList();
        return buildGroupedOpenApi(groupName, paths, excludePaths, parameterList);
    }
    /**
     * 配置 ，组|指定路径|全局 --必传参数
     *
     * @param groupName     该组名
     * @param paths         该组路径
     * @param excludePaths  该组排除路径
     * @param parameterList 该组参数
     * @return
     */
    default GroupedOpenApi buildGroupedOpenApi(String groupName, List<String> paths, List<String> excludePaths, List<Parameter> parameterList) {
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
        if (CollUtil.isNotEmpty(parameterList)) {
            builder.addOperationCustomizer(
                    //加全局变量
                    (operation, handlerMethod) -> {
                        Operation reOperation = new Operation();
                        for (Parameter parameter : parameterList) {
//                            parameter.setRequired(true);
                            //再原有方法参数基础上添加全局参数
                            reOperation = operation.addParametersItem(parameter);
                        }
                        //该方法会覆盖原有参数
                        //Operation parameters = operation.parameters(parameterList);
                        return reOperation;
                    }
            );
        }
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
        //String authorization = getAuthorization();
        Parameter parameter = new Parameter()
                .name(authorization) // header 名
                .description("认证 Token")// 描述
                .in(String.valueOf(SecurityScheme.In.HEADER))// 请求 header
                .schema(new StringSchema()
                        // ._default("Bearer ") // 最好关闭
                        .name(authorization).description("认证 Token"))//
                .required(true);
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
                                .scheme("Bearer ")
                                .in(SecurityScheme.In.HEADER)
                                .description("鉴权token")));
        return api;
    }

    default GlobalOpenApiCustomizer buildGlobalOpenApiCustomizer() {
        //Logger log = LoggerFactory.getLogger(getClass());
        //log.info("start");
        return openApi -> {
            if (ObjectUtil.isNotEmpty(openApi)) {
                openApi.getPaths().forEach((path, pathItem) -> {
                    boolean pathBool = false;
                    //log.info("path:{};pathItem:{};", path, pathItem);
                    List<String> list = getPrefixByAuthorization();
                    if (CollUtil.isEmpty(list)) {
                        pathBool = true;
                    } else {
                        for (String prefix : list) {
                            if (path.startsWith(prefix)) {
                                pathBool = true;
                                break;
                            }
                        }
                    }
                    if (pathBool) {
                        pathItem.readOperations().forEach(operation ->
                                operation.addSecurityItem(new SecurityRequirement().addList(HttpHeaders.AUTHORIZATION))
                        );
                    }
                });
            }
        };
    }

    default GlobalOpenApiCustomizer buildOpenApiCustomizer() {
        GlobalOpenApiCustomizer customizer = buildGlobalOpenApiCustomizer();
        return customizer;
    }
}
