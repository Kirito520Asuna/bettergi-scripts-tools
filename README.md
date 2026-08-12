# bettergi-script-tools

## 项目简介

bettergi-script-tools 是一套面向 BetterGI 脚本的辅助工具集，通过第三方 HTTP 调用弥补原脚本在部分能力上的不足。  
目前已实现以下功能：

- **WebSocket 消息代理**：借助本工具发送 WebSocket 消息，避免脚本内原生 WebSocket 的限制。
- **Cron 表达式解析**：支持计算未来 N 次执行时间戳，方便完成定时任务规划。
- **OCR 文字识别**：集成第三方 OCR 服务，为脚本提供图像识别能力。
- **自动秘境计划配置存储与查询**：支持按 UID 存取秘境/国家配置信息，实现多终端配置共享。

> 运行服务后，可前往内置 UI 与文档页面查看完整说明：

- 管理界面：<http://localhost:8081/bgi/ui>
- 接口文档（Swagger）：<http://localhost:8081/bgi/doc.html>

---

## 快速开始

### 方式一：直接运行可执行文件（Windows）

前往 [Release 页面](https://github.com/Kirito520Asuna/bettergi-scripts-tools/releases) 下载带有 `windows` 标识的 ZIP 包，解压后双击 `.exe` 文件即可启动。

### 方式二：使用 Java 运行 JAR 包

```bash
java -jar xxxx.jar
```

启动前请在同级目录准备好 `application-prod.yml` 配置文件（见下方章节）。

### 方式三：Docker 部署

> 请先在宿主机上创建配置文件，例如 `/path/to/application-prod.yml`，内容参照配置章节。

```bash
docker pull ghcr.io/kirito520asuna/bettergi-scripts-tools:latest
docker run -d -p 8081:8081 \
  -v /path/to/application-prod.yml:/app/application-prod.yml \
  -v /path/to/cache/:/path/to/cache/ \
  --name bettergi-script-tools \
  ghcr.io/kirito520asuna/bettergi-scripts-tools:latest
```

### 方式四：Docker Compose 部署

创建 `docker-compose.yml`：

```yaml
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
      - WS_URL=ws://backend-service:8080/ws
      - ACCESS_TOKEN_NAME=access-token
      - SPRING_PROFILES_ACTIVE=prod
      # DB 配置
      - DB_PRIMARY=SQLite #SQLite,PgSQL,MySQL #(默认-SQLite )
      # SQLite
      - DB_SQLITE_URL=${DB_URL}
      - DB_SQLITE_USERNAME=${DB_USE}
      - DB_SQLITE_PASSWORD=${DB_PASS} 
      # MySQL
      - DB_MYSQL_URL=${DB_URL}
      - DB_MYSQL_USERNAME=${DB_USE}
      - DB_MYSQL_PASSWORD=${DB_PASS} 
      # PgSQL
      - DB_PGSQL_URL=${DB_URL}
      - DB_PGSQL_USERNAME=${DB_USE}
      - DB_PGSQL_PASSWORD=${DB_PASS}
    volumes:
      - /path/to/application-prod.yml:/app/application-prod.yml
      - /path/to/cache/:/app/cache/
    networks:
      - bgi-network
    restart: unless-stopped

networks:
  bgi-network:
    driver: bridge
```

启动命令：

```bash
docker-compose up -d
```

---

## 配置文件详解

启动服务前，必须在 JAR 同级目录（或挂载路径）创建 `application-prod.yml` 文件。完整示例：

```yaml
server:
  port: 8081                     # 服务端口
  # servlet:
  #   context-path: /bgi         # 0.0.4 版本禁止修改，否则 UI 无法正常工作

# WebSocket 代理相关配置
ws:
  url: ws://localhost:8081/ws       # 可忽略
  access-token-name: access-token

# 缓存与多实例支持（可选用 Redis 替代本地缓存）
spring:
  redis:
    mode: none                     # none: 不使用 Redis; single: 单体; cluster: 集群; sentinel: 哨兵
    # 单体模式
    host: 127.0.0.1
    port: 6379
    database: 0
    # 哨兵模式
    sentinel:
      master: mymaster
      nodes:
        - 192.168.6.128:26379
        - 192.168.6.128:26380
    # 集群模式
    cluster:
      nodes:
        - 192.168.6.128:7000
        - 192.168.6.128:7001
    # 安全认证
    username:      # 默认为空
    password:      # 默认为空
    
# 数据库
db:
  primary: SQLite
  SQLite:
    url: jdbc:sqlite:./cache/bgi-tools.db
    username:
    password:
  MySQL:
    url: jdbc:mysql://localhost:3306/bgi_tools?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: your_password
  PgSQL:
    url: jdbc:postgresql://localhost:5432/bgi_tools
    username: postgres
    password: your_password

# 接口访问 Token 校验（二者任意一项为空则跳过校验）
check:
  token:
    name:        # Token 名称，自行修改
    value:       # Token 值，自行修改

# 默认管理账号密码（建议修改）
auth:
  users:
    - username: bgi_tools
      password: bgi_tools
```

**重要提示**：
- `context-path` 在 **0.0.4 版本** 中**不允许修改**为其他值，否则内嵌 UI 将无法正常加载。
- 多实例部署时，建议将 `spring.redis.mode` 切换为远程缓存（如 `single` 或 `cluster`），避免本地 SQLite 数据不一致。
- Nginx 公网配置（含 WebSocket 支持）:
```nginx 
server { 
    listen 80; 
    listen [::]:80; 
    server_name 域名;
    # 静态资源代理
    location /favicon.ico {
        proxy_pass http://bgi-tools地址/bgi/ui/;
    }

    # WebSocket 日志推送（必须放在 / 前面）
    location /bgi/ws/logs {
        proxy_pass http://bgi-tools地址/bgi/ws/logs;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
    }

    # 主应用代理
    location / {
        proxy_pass http://bgi-tools地址/;
    }
}

server {
    listen 443 ssl;
    server_name  域名;

    # ======================= 证书配置开始 =======================
    # 指定证书文件，请将 xxx.pem 替换为您实际使用的证书文件的绝对路径
    ssl_certificate path/to/xxx.pem;
    # 指定私钥文档，请将 xxx.key 替换为您实际使用的私钥文件的绝对路径
    ssl_certificate_key path/to/xxx.key;
    # 配置 SSL 会话缓存，提高性能
    ssl_session_cache shared:SSL:1m;
    # 设置 SSL 会话超时时间
    ssl_session_timeout 5m;
    # 自定义设置使用的TLS协议的类型以及加密套件（以下为配置示例，请您自行评估是否需要配置）
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE:ECDH:AES:HIGH:!NULL:!aNULL:!MD5:!ADH:!RC4;
    # 指定允许的 TLS 协议版本，TLS协议版本越高，HTTPS通信的安全性越高，但是相较于低版本TLS协议，高版本TLS协议对浏览器的兼容性较差
    ssl_protocols TLSv1.2 TLSv1.3;
    # 优先使用服务端指定的加密套件
    ssl_prefer_server_ciphers on;
    # ======================= 证书配置结束 =======================

    # 传递真实请求头信息
    proxy_set_header Origin $scheme://$host;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;

    location /favicon.ico {
        proxy_pass http://bgi-tools地址/bgi/ui/;
    }

    location /bgi/ws/logs {
        proxy_pass http://bgi-tools地址/bgi/ws/logs;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
    }

    location / {
        proxy_pass http://bgi-tools地址/;
    }
}

```
---

## API 接口说明

所有接口均提供三种访问路径前缀：

- `/bgi/`：无需鉴权（若未配置 `check.token`）
- `/bgi/api/`：需要校验签名(默认不开放)
- `/bgi/jwt/`：需要携带 JWT 令牌或 `check.token` 参数（若已配置）

本文档以 `/bgi/` 前缀为例，实际调用时可根据需要替换。

### 1. WebSocket 代理

**发送消息**

- **请求方式**：`POST`
- **请求路径**：`/bgi/ws-proxy/message/send`
- **请求体**：

```json
{
  "url": "ws://127.0.0.1:8080/ws",
  "token": "your_websocket_token",
  "bodyJson": "要发送的 JSON 字符串"
}
```

示例：

```http
POST http://localhost:8081/bgi/ws-proxy/message/send
Content-Type: application/json

{
  "url": "ws://127.0.0.1:8080/ws",
  "token": "access-token-value",
  "bodyJson": "{}"
}
```

### 2. Cron 表达式解析

**查询下一个符合条件的时间戳**

- **请求方式**：`POST`
- **请求路径**：`/bgi/cron/next-timestamp`
- **请求体**：

```json
{
  "cronExpression": "0 0 8 * * ?",
  "startTimestamp": 1690000000,
  "endTimestamp": 1690900000
}
```

**批量查询（返回每个 key 的下一次时间戳）**

- **请求方式**：`POST`
- **请求路径**：`/bgi/cron/next-timestamp/all`
- **请求体**：

```json
{
  "cronList": [
    {
      "key": "daily_task",
      "cronExpression": "0 0 10 * * ?",
      "startTimestamp": 1690000000,
      "endTimestamp": 1690900000
    }
  ]
}
```

### 3. OCR 文字识别

**上传字节数组进行识别**

- **请求方式**：`POST`
- **请求路径**：`/bgi/ocr/bytes`
- **请求体**：

```json
{
  "bytes": [255, 216, 255, 224, ...]
}
```

### 4. 自动秘境计划配置

**查询指定 UID 的配置**

- **请求方式**：`GET`
- **请求路径**：`/bgi/auto/plan/json?uid=12345678`

**查询全部秘境信息**

- **请求方式**：`GET`
- **请求路径**：`/bgi/auto/plan/domain/json/all`

**存储全部秘境信息（推送）**

- **请求方式**：`POST`
- **请求路径**：`/bgi/auto/plan/domain/json/all`
- **请求体**：

```json
{
  "uid": "12345678",
  "json": "秘境配置 JSON 字符串"
}
```

**存储全部国家信息（推送）**

- **请求方式**：`POST`
- **请求路径**：`/bgi/auto/plan/country/json/all`
- **请求体**：

```json
{
  "json": "国家配置 JSON 字符串"
}
```

---

## 脚本集成示例（OCR 识别）

以下代码演示如何在 BetterGI 脚本中调用本工具的 OCR 识别接口：

```javascript
(async function () {
    const json = {
        x: 1322,
        y: 411,
        w: 96,
        h: 53,
    };
    let fullRegion = captureGameRegion();

    // 方法：DeriveCrop（推荐，自动处理坐标转换和内存）
    let subRegion = fullRegion.DeriveCrop(json.x, json.y, json.w, json.h);
    let mat = subRegion.SrcMat;
    const bytes = Array.from(mat.ToBytes());

    // 构造请求 Body
    let body = { bytes: bytes };
    log.info(`发送 OCR 请求，字节长度：${bytes.length}`);

    const httpResponse = await http.request(
        "POST",
        "http://localhost:8081/bgi/ocr/bytes",
        JSON.stringify(body),
        JSON.stringify({ "Content-Type": "application/json" })
    );

    log.info(`OCR 识别结果：${JSON.stringify(httpResponse)}`);

    // 用完后释放资源
    subRegion.Dispose();
    fullRegion.Dispose();
})();
```
### 5. 自动体力计划服务(推送--待BGI-JS开发)

> 期望其他作者开发 
> 
> 具体的json 文档:http://localhost:8081/bgi/doc.html#/other/自动体力计划服务/saveInfo
> 
> 待开发识别 `培养计划` JS脚本模块 核心功能:(识别功能,计算功能,推送功能)
> 
> 推送后可直接调用 脚本`自动体力计划JS` https://bgi.sh/?type=js&path=AutoPlan

#### 调用工具类
```javascript
export class BgiTools {
    /**
     * 上传培养计划
     * @param http_api -- API地址
     * @param json -- 培养计划Json 
     * @param token -- 授权token 
     * @returns {Promise<void>}
     */
    static async uploadTrainingProgram(http_api="http://localhost:8081/bgi/auto/plan/saveInfo",
                                       json=
                                       {
                                         uid: "",
                                         cultivate: true,
                                         removeCultivate: true,
                                         autoPlanList:[
                                             {
                                                 order: 0,
                                                 days: [],
                                                 dayName: "",
                                                 selectedType: "",
                                                 runType: "",
                                                 enable: true,
                                                 record: true,
                                                 autoStygianOnslaught:{},
                                                 autoLeyLineOutcrop:{},
                                                 autoBoss:{},
                                                 autoDomain:{}
                                             }
                                         ]
                                       }, token = {name: "Authorization", value: ''}
    ){

        let header = {
            "Content-Type": "application/json",
            [token.name]: token.value
        };
        const response = await http.request(
            "POST", http_api
            , JSON.stringify(json), JSON.stringify(header)
        )
        if (response.status_code === 200 && response.body?.code === 200){
          log.info("上传培养计划成功")
        }else {
            throw new Error(`上传失败，状态码: ${response.status_code}, 业务码: ${response.body?.code}, 错误信息: ${response.body?.message}`)
        }
    }
}
```
#### 推送数据格式(培养计划Json):
```json
{
  "uid": "",
  "cultivate": true,
  "removeCultivate": true,
  "autoPlanList": [
    {
      "id": "",
      "order": 0,
      "days": [],
      "dayName": "",
      "selectedType": "",
      "runType": "",
      "enable": true,
      "cultivate": true,
      "record": true,
      "autoDomain": {
        "sundaySelectedName": "",
        "domainName": "",
        "sundaySelectedValue": 0,
        "partyName": "",
        "domainRoundNum": 0,
        "physical": [
          {
            "order": 0,
            "name": "",
            "open": true,
            "count": 0
          }
        ]
      },
      "autoLeyLineOutcrop": {
        "count": 0,
        "country": "",
        "leyLineOutcropType": "",
        "useAdventurerHandbook": true,
        "friendshipTeam": "",
        "team": "",
        "timeout": 0,
        "useFragileResin": true,
        "useTransientResin": true,
        "isGoToSynthesizer": true,
        "isNotification": true
      },
      "autoStygianOnslaught": {
        "physical": [
          {
            "order": 0,
            "name": "",
            "open": true,
            "count": 0
          }
        ],
        "specifyResinUse": true,
        "bossNum": 0,
        "fightTeamName": ""
      },
      "autoBoss": {
        "bossName": "",
        "strategyName": "",
        "combatStrategyPath": "",
        "teamName": "",
        "specifyRunCount": true,
        "runCount": 0,
        "useTransientResin": true,
        "useFragileResin": true,
        "reviveRetryCount": 0,
        "returnToStatueAfterEachRound": true,
        "rewardRecognitionEnabled": true
      }
    }
  ]
}
```

> 若使用 `check.token` 鉴权，请将 URL 中的 `/bgi/` 替换为 `/bgi/jwt/`，并在请求头中加入 `token 名称` 和 `token 值`。

---

## 访问地址

| 资源         | 默认地址                                       | 动态地址（根据配置拼接）                                     |
| ------------ | ---------------------------------------------- | ------------------------------------------------------------ |
| 管理界面     | <http://localhost:8081/bgi/ui>                 | `http://127.0.0.1:${server.port:8080}${server.servlet.context-path:/}/ui` |
| Swagger 文档 | <http://localhost:8081/bgi/doc.html>           | `http://127.0.0.1:${server.port:8080}${server.servlet.context-path:/}/doc.html` |

---

## 注意事项

1. **禁止修改 context-path**：0.0.4 版本中若将 `server.servlet.context-path` 改为非 `/bgi` 的值，会导致内置 UI 和接口无法正常工作。
2. **缓存模式选择**：单机运行可使用默认的 SQLite 缓存；若多实例并行，请务必切换至 Redis，否则缓存数据可能不一致。
3. **Token 鉴权**：`check.token.name` 和 `check.token.value` 同时不为空时，所有 `/bgi/api/` 路径的接口都将进行 Token 校验，调用时需在请求参数中携带对应名称和值。
4. **数据库配置**：示例中提供了 MySQL 和 PostgreSQL 的配置模板，更换数据源时请只保留一个数据源并确保驱动正确。