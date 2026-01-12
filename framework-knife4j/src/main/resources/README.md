# knife4j 配置说明
## 配置文件 application.yml
```yaml
## 自定义open API公共描述信息
api:
  authorization: token
  common:
    version: 3.0.0
    title: SecuityAPI
    description: SecuityAPI
    termsOfService:
    license: 未经许可不得转载
    licenseUrl: xxx.license.com
    externalDocDesc:
    externalDocUrl:
    contact:
      name: kirito-asuna
      url:
      email:

springdoc:
  api-docs:
    path: /v3/api-docs
# 请注意使用group名时如果配置了GroupedOpenApi引用自动配置时组名不能重复
  open:
    default-group-configs:
      enable: true
      api: true
      jwt: true
      other: true
#  group-configs:
#    - group: "api"
#      paths-to-match: "/api/**"
#    - group: "jwt"
#      paths-to-match: "/jwt/**"
#    - group: "other"
#      paths-to-exclude:
#        - "/swagger-resources/**"
#        - "/v2/api-docs"
#        - "/v3/api-docs"
#        - "/api/**"
#        - "/jwt/**"
  swagger-ui:
    operations-sorter: alpha
    path: /swagger-ui.html
    tags-sorter: alpha
#Authorization 在knife4j中失效==>替代方案 使用knife4j中全局参数设置Authorization
knife4j:
  cors: true
  enable: true
  setting:
    enable-dynamic-parameter: true
  #swagger
  basic:
    enable: true
    username: root
    password: root

#cors: true - 开启跨域资源共享（CORS），允许不同域名的请求访问。
#enable: true - 启用knife4j。
#setting: enable-dynamic-parameter: true - 开启动态参数功能，允许在运行时动态修改API接口参数。
#basic: enable: true - 开启基本认证，使用用户名和密码进行认证。
#username: root - 用户名为root。
#password: root - 密码为root。
```
## pom.xml 配置
### spring-boot2
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.actual_combat</groupId>
        <artifactId>micro-framework</artifactId>
        <version>${revision}</version>
    </parent>
    
    <artifactId>framework-knife4j</artifactId>

    <properties>
        <!--swagger 版本-->
        <knife4j.version>4.1.0</knife4j.version>
        <springdoc-openapi-ui.version>1.6.15</springdoc-openapi-ui.version>
        <javax.servlet-api.version>4.0.1</javax.servlet-api.version>
        <javax.annotation-api.version>1.3.2</javax.annotation-api.version>
        <spring-boot.version>2.5.0</spring-boot.version>
    </properties>
    
     <dependencyManagement>
         <dependencies>
             <dependency>
                 <groupId>org.springframework.boot</groupId>
                 <artifactId>spring-boot-dependencies</artifactId>
                 <version>${spring-boot.version}</version>
                 <type>pom</type>
                 <scope>import</scope>
             </dependency>
             <!--swagger-->
             <dependency>
                 <groupId>com.github.xiaoymin</groupId>
                 <artifactId>knife4j-openapi3-spring-boot-starter</artifactId>
                 <version>${knife4j.version}</version>
             </dependency>

             <dependency>
                 <groupId>org.springdoc</groupId>
                 <artifactId>springdoc-openapi-ui</artifactId>
                 <version>${springdoc-openapi-ui.version}</version>
             </dependency>

             <dependency>
                 <groupId>javax.servlet</groupId>
                 <artifactId>javax.servlet-api</artifactId>
                 <version>${javax.servlet-api.version}</version>
             </dependency>

             <dependency>
                 <groupId>javax.annotation</groupId>
                 <artifactId>javax.annotation-api</artifactId>
                 <version>${javax.annotation-api.version}</version>
             </dependency>
         </dependencies>
     </dependencyManagement>
    <!--      网关引用需要排除
    <exclusions>
                <exclusion>
                    <groupId>org.springframework</groupId>
                    <artifactId>spring-webmvc</artifactId>
                </exclusion>
            </exclusions>
            -->
    <dependencies>
        <!--swagger-->
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-openapi3-spring-boot-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-ui</artifactId>
        </dependency>

        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
        </dependency>

        <dependency>
            <groupId>javax.annotation</groupId>
            <artifactId>javax.annotation-api</artifactId>
        </dependency>

        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
        </dependency>
    </dependencies>

</project>
```
### spring-boot3
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.actual_combat</groupId>
        <artifactId>micro-framework</artifactId>
        <version>${revision}</version>
    </parent>
    
    <artifactId>framework-knife4j</artifactId>

    <properties>
        <!--swagger 版本-->
        <knife4j.version>4.4.0</knife4j.version>
        <springdoc-openapi.version>2.6.0</springdoc-openapi.version>
        <spring-boot.version>3.3.5</spring-boot.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            
            <dependency>
                <groupId>com.github.xiaoymin</groupId>
                <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
                <version>${knife4j.version}</version>
            </dependency>

            <dependency>
                <groupId>org.springdoc</groupId>
                <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
                <version>${springdoc-openapi.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
    <!--      网关引用需要排除
    <exclusions>
                <exclusion>
                    <groupId>org.springframework</groupId>
                    <artifactId>spring-webmvc</artifactId>
                </exclusion>
            </exclusions>
            -->
    <dependencies>
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        </dependency>

        <dependency>
            <groupId>jakarta.annotation</groupId>
            <artifactId>jakarta.annotation-api</artifactId>
        </dependency>
    </dependencies>

</project>
```
### spring-boot2 类 包所在
```
GroupedOpenApi==>org.springdoc.core.GroupedOpenApi
@PostConstruct==>javax.annotation.PostConstruct
@Resource==>javax.annotation.Resource
```
### spring-boot3 类 包所在
```
GroupedOpenApi==>org.springdoc.core.models.GroupedOpenApi
@PostConstruct==>jakarta.annotation.PostConstruct
@Resource==>jakarta.annotation.Resource
```