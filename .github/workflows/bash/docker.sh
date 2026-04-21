#/bin/bash
createDockerUsageReadme(){
local name=$1
local tag_name=$2
local run_id=$3
local tar_file_name=$4


cat <<EOF > README-Docker-Usage.md
# $name Docker 镜像使用说明

**当前镜像版本**：$tag_name
**构建时间**：$(date '+%Y年%m月%d日 %H:%M:%S') (HKT)
**镜像标签示例**：ghcr.io/$name:tag_name或 :latest

这是一个**完整的 Docker 镜像导出包**（.tar 文件），包含 $name应用及运行环境。
支持完全离线部署，无需联网拉取镜像，非常适合内网服务器、测试机或快速分发。

## 1. 导入镜像

在任何安装了 Docker 的机器上，直接运行以下命令：

\`\`\`bash
# 使用 workflow 生成的精确文件名导入镜像
docker load -i $tar_file_name

# 成功后会显示类似：
# Loaded image: ghcr.io/$name:$tag_name
# Loaded image: ghcr.io/$name:latest
\`\`\`

\`\`\`bash
# 查看已导入的镜像
docker images | grep $name
\`\`\`

## 2. 启动容器（推荐方式）

\`\`\`bash
docker run -d \\
--name $name \\
-p 8081:8081 \\
-v \$(pwd)/$name :/app \\           # 可选：挂载配置文件目录
--restart unless-stopped \\
ghcr.io/$name:$tag_name
\`\`\`

- **默认端口**：8081（可在 application-prod.yml 中修改）
- **访问地址**：http://你的服务器IP:8081/bgi
- **配置文件挂载建议**：
在当前目录创建 config 文件夹，将 application-prod.yml 放入其中

## 3. 常用命令速查

\`\`\`bash
# 查看实时日志
docker logs -f $name

# 进入容器内部（调试用）
docker exec -it $name /bin/sh

# 停止并删除容器
docker stop $name && docker rm $name
\`\`\`

## 4. 注意事项

- 需要 Docker 20.10 或更高版本
- 当前镜像平台：linux/amd64（适用于大多数服务器和 PC）
- 如遇端口冲突、网络问题，请检查：
- 防火墙设置
- 端口映射是否正确
- 是否有其他程序占用 8081 端口
- 问题反馈：欢迎在 GitHub Issues 留言

**祝使用愉快！**
文档由 GitHub Actions 自动生成于构建 $run_id
EOF

# 调试输出：确认文件名已正确展开
echo "使用的 TAR_FILE_NAME 值：$tar_file_name"
echo "生成的 Markdown 文件内容预览："
cat README-Docker-Usage.md

}
saveDockerImage(){
  local tags_input=$1
  local tag_name=$2
    # 提取第一个 tag（处理多行输入）
  local image_tag=$(echo "$tags_input" | head -n 1)
  echo "🐳 保存镜像：$image_tag"

  # 使用最安全的文件名策略：版本号 + 时间戳
  local TIMESTAMP=$(date +%Y%m%d-%H%M%S)
  local TAR_FILE_NAME="docker-${tag_name}-${TIMESTAMP}.tar"

  echo "最终安全文件名: $TAR_FILE_NAME"

# 检查镜像是否存在
if ! docker image inspect "$image_tag" >/dev/null 2>&1; then
  echo "❌ 镜像 $image_tag 不存在！当前本地镜像列表："
  docker images
  exit 1
fi

  # 保存镜像
  docker save "$image_tag" -o "$TAR_FILE_NAME"
  # ✅ 添加成功提示和文件大小
  local size=$(du -h "$TAR_FILE_NAME" | cut -f1)
  echo "✅ 已保存：$TAR_FILE_NAME ($size)"
  echo "TAR_FILE_NAME=${TAR_FILE_NAME}" >> $GITHUB_ENV
}
createDockerfile(){
local java_version=$1
local DOCKER_FILE_NAME="Dockerfile"
if [ -f "$DOCKER_FILE_NAME" ]; then
  echo "$DOCKER_FILE_NAME already exists"
  cat "$DOCKER_FILE_NAME"
else
  cat > "$DOCKER_FILE_NAME" << EOF
FROM eclipse-temurin:${java_version}-jre-slim
VOLUME /tmp
WORKDIR /app

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    libgomp1 \
    libstdc++6 \
    fontconfig \
    fonts-wqy-zenhei && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN chmod 777 /tmp

COPY *.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
EOF
  if [ -f "$DOCKER_FILE_NAME" ]; then
    echo "📄 创建 $DOCKER_FILE_NAME 完成"
    echo "$DOCKER_FILE_NAME created successfully"
    cat "$DOCKER_FILE_NAME"
  else
    echo "Error: $DOCKER_FILE_NAME creation failed"
    exit 1
  fi
fi          

echo "DOCKER_FILE_NAME=$DOCKER_FILE_NAME" >> $GITHUB_ENV
}

validateBuildDockerImage(){
  local expected_image_tag=$1
  local timeout_seconds=${2:-30}

  echo "🔍 验证 Docker 镜像：$expected_image_tag"

  # 等待镜像可用（处理构建延迟）
  local count=0
  while ! docker image inspect "$expected_image_tag" >/dev/null 2>&1; do
    count=$((count + 1))
    if [ $count -ge $timeout_seconds ]; then
      echo "❌ 超时：${timeout_seconds}s 内未找到镜像 $expected_image_tag"
      docker images
      exit 1
    fi
    sleep 1
  done

  # 验证镜像详细信息
  echo "✅ 镜像存在，获取详细信息..."
  docker image inspect "$expected_image_tag" --format='{{.Id}}' | head -c 12
  echo ""

  # 显示镜像大小和创建时间
  docker images "$expected_image_tag" --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedAt}}"

  # 快速健康检查（尝试启动并立即停止）
  local container_name="validate_$(date +%s)"
#  echo "🧪 快速启动测试..."
#  if docker run --rm --name "$container_name" "$expected_image_tag" echo "镜像可执行" >/dev/null 2>&1; then
#    echo "✅ 镜像健康检查通过"
#  else
#    echo "⚠️ 镜像可能缺少入口点或命令（非致命）"
#  fi

  echo "✅ Docker 镜像验证完成"
}


main(){
  local mode=$1
  case $mode in
  "createDockerUsageReadme")
  #    local name="$2"
  #    local tag_name="$3"
  #    local run_id="$4"
  #    local tar_file_name="$5"
      echo "创建 Docker 使用说明文档 (README-Docker-Usage.md)"
      createDockerUsageReadme "$2" "$3" "$4" "$5"
      ;;
  "saveDockerImage")
  #    local image_tag="$2"
  #    local tag_name="$2"
      echo "保存 Docker 镜像"
      saveDockerImage "$2" "$3"
      ;;
  *)
      echo "❌ 未知命令：$mode"
      exit 1
      ;;
  esac
}
#local mode=$1
#local arg1=$2
#local arg2=$3
#local arg3=$4
#local arg4=$5
#main "$mode" "$arg1" "$arg2" "$arg3" "$arg4"
#main "$@"

