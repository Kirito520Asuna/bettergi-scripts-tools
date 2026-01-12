# bettergi-script-tools

## 简介

bettergi-script-tools 是一个基于 bettergi-script 的工具集，提供了一系列便捷的bgi js脚本不支持的扩展功能，通过第三方http请求实现
目前功能:

- 支持 WebSocket 请求的代理
- 支持 Cron 表达式解析

详情请运行后查看文档 http://localhost:8081/bgi/doc.html

## 使用

### 1. 下载代码

```shell
git clone https://github.com/Kirito520Asuna/bettergi-script-tools.git
```

### 2. 新建配置文件 application-prod.yml

与.jar同一文件夹

```shell
server:
  port: 8081
  servlet:
    context-path: /bgi
ws:
  url: ws://localhost:8081/ws #可忽略
  access-token-name: access-token   
```

### 3. 运行

#### 1.windows exe 直接运行

前往 [release](https://github.com/Kirito520Asuna/bettergi-script-tools/releases) 下载 带windows的zip包解压运行.exe文件即可

#### 2.java

```shell
java -jar xxxx.jar
```

#### 3.部署docker

```shell
docker pull ghcr.io/kirito520asuna/bettergi-scripts-tools:latest
docker run -d -p 8081:8081 -v /path/to/application-prod.yml:/app/application-prod.yml --name bettergi-script-tools ghcr.io/kirito520asuna/bettergi-script-tools:latest
```

```shell
# 在 docker-compose.yml 文件所在目录执行
docker-compose up -d
```

```yml
version: '3.8'

services:
  bettergi-script-tools:
    image: ghcr.io/kirito520asuna/bettergi-scripts-tools:latest
    container_name: bettergi-script-tools
    ports:
      - "8081:8081"
    environment:
      - SERVER_PORT=8081
      - SERVER_SERVLET_CONTEXT_PATH=/bgi
      - WS_URL=ws://backend-service:8080/ws  # 连接后端服务
      - ACCESS_TOKEN_NAME=access-token
      - SPRING_PROFILES_ACTIVE=prod
    volumes:
      - /path/to/application-prod.yml:/app/application-prod.yml
    networks:
      - bgi-network
    restart: unless-stopped
networks:
  bgi-network:
    driver: bridge

```

## swagger 文档地址

```text
默认地址：http://localhost:8081/bgi/doc.html
动态地址：http://127.0.0.1:${server.port:8080}${server.servlet.context-path:/}/doc.html
```

## Http请求示例

### 1.WsProxy

```http request
###
###没有配置token时，可忽略
POST http://localhost:8081/bgi/ws-proxy/message/send
Content-Type: application/json

{
  "url": "ws://127.0.0.1:8080/ws",
  "token": "token",
  "bodyJson": ""
}


###
POST http://localhost:8081/bgi/api/ws-proxy/message/send
Content-Type: application/json

{
  "url": "",
  "token": "",
  "bodyJson": ""
}

###
POST http://localhost:8081/bgi/jwt/ws-proxy/message/send
Content-Type: application/json

{
  "url": "",
  "token": "",
  "bodyJson": ""
}
```
### 2.Cron
```http request
###
POST http://localhost:8081/bgi/cron/next-timestamp
Content-Type: application/json

{
  "cronExpression": "",
  "startTimestamp": 1,
  "endTimestamp": 1
}

###
POST http://localhost:8081/bgi/api/cron/next-timestamp
Content-Type: application/json

{
  "cronExpression": "",
  "startTimestamp": 1,
  "endTimestamp": 1
}

###
POST http://localhost:8081/bgi/jwt/cron/next-timestamp
Content-Type: application/json

{
  "cronExpression": "",
  "startTimestamp": 1,
  "endTimestamp": 1
}
```