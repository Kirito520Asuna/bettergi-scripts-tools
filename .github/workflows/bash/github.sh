#!/bin/bash
# 临时目录配置（允许通过环境变量覆盖）
TMP_EXTRACTED_JARS=${TMP_EXTRACTED_JARS:-/tmp/extracted-jars}
TMP_RENAMED_JARS=${TMP_RENAMED_JARS:-/tmp/renamed-jars}
TMP_EXTRACTED_EXES=${TMP_EXTRACTED_EXES:-/tmp/extracted-exes}
JARS_ZIP=${JARS_ZIP:-jars.zip}

BGI_TOOLS_YML="bgi-tools/src/main/resources/application.yml"
FRONTEND_ENV="frontend/.env.prod"

maven_build() {
  local SKIP_TESTS=${1:-false}

  echo "🔨 使用 Maven 构建项目"

  if [ "$SKIP_TESTS" = "true" ]; then
    echo "⏭️ 跳过测试"
    mvnd clean package -B -DskipTests -Dquickly
#    mvn clean package -B -DskipTests -Dquickly
  else
    mvnd clean package -B -Dquickly
#    mvn clean package -B -Dquickly
  fi

  echo "✅ Maven 构建完成"
}
list_jar_files() {
  local JAR_DIR_PREFIX=$1

  echo "📋 列出匹配的 JAR 文件"

  if [ -z "$JAR_DIR_PREFIX" ]; then
    echo "匹配的 JAR 文件 (*/target/*.jar):"
    find . -type f -path "*/target/*.jar" || echo "⚠️ 未找到匹配的 JAR 文件"
  else
    echo "匹配的 JAR 文件 (*/${JAR_DIR_PREFIX}/*/target/*.jar, */${JAR_DIR_PREFIX}/target/*.jar):"
    find . -type f \( -path "*/${JAR_DIR_PREFIX}/*/target/*.jar" -o -path "*/${JAR_DIR_PREFIX}/target/*.jar" \) || echo "⚠️ 未找到匹配的 JAR 文件"
  fi
}
rename_jar_files() {
  local JAR_DIR_PREFIX=$1

  echo "🔄 重命名 JAR 文件（移除版本号）TMP_RENAMED_JARS:$TMP_RENAMED_JARS"

  mkdir -p "$TMP_RENAMED_JARS"
  # 使用数组接收 find 命令结果
  local jar_files=()
  if [ -z "$JAR_DIR_PREFIX" ]; then
    echo "查找匹配的 JAR 文件 (*/target/*.jar):"
    while IFS= read -r file; do
      jar_files+=("$file")
    done < <(find . -type f \( -path "*/target/*.jar" \))
#    find . -type f -path "*/target/*.jar" -exec bash -c '
#      for jar_file; do
#        [ -f "$jar_file" ] || continue
#        base_name=$(basename "$jar_file" .jar)
#        #new_name=$(echo "$base_name" | sed -E "s/-v?[0-9]+(\.[0-9]+)*(-[a-zA-Z0-9]+)*$//")
#        #new_name=$(echo "$base_name" | sed -E "s/-[vV]?[0-9]+.*$//"
#        # 匹配：-v1.2.3, -1.2.3, -v0.0.7-dev3.2 等（从最后一个连字符后的版本号开始）
#        new_name=$(echo "$base_name" | sed -E "s/-[vV]?[0-9]+(\.[0-9]+)*(-[a-zA-Z0-9._-]+)*$//")
#        new_path="$TMP_RENAMED_JARS/${new_name}.jar"
#        echo "处理：$jar_file -> $new_path"
#        cp "$jar_file" "$new_path"
#      done
#    ' _ {} +
  else
    echo "查找匹配的 JAR 文件 (*/${JAR_DIR_PREFIX}/*/target/*.jar, */${JAR_DIR_PREFIX}/target/*.jar):"
    while IFS= read -r file; do
      jar_files+=("$file")
    done < <(find . -type f \( -path "*/${JAR_DIR_PREFIX}/*/target/*.jar" -o -path "*/${JAR_DIR_PREFIX}/target/*.jar" \))
#    find . -type f \( -path "*/${JAR_DIR_PREFIX}/*/target/*.jar" -o -path "*/${JAR_DIR_PREFIX}/target/*.jar" \) -exec bash -c '
#      for jar_file; do
#        [ -f "$jar_file" ] || continue
#        base_name=$(basename "$jar_file" .jar)
#        #new_name=$(echo "$base_name" | sed -E "s/-v?[0-9]+(\.[0-9]+)*(-[a-zA-Z0-9]+)*$//")
#        #new_name=$(echo "$base_name" | sed -E "s/-[vV]?[0-9]+.*$//"
#        # 匹配：-v1.2.3, -1.2.3, -v0.0.7-dev3.2 等（从最后一个连字符后的版本号开始）
#        new_name=$(echo "$base_name" | sed -E "s/-[vV]?[0-9]+(\.[0-9]+)*(-[a-zA-Z0-9._-]+)*$//")
#        new_path="$TMP_RENAMED_JARS/${new_name}.jar"
#        echo "处理：$jar_file -> $new_path"
#        cp "$jar_file" "$new_path"
#      done
#    ' _ {} +
  fi
  echo "找到 ${#jar_files[@]} 个 JAR 文件"

  for jar_file in "${jar_files[@]}"; do
    [ -f "$jar_file" ] || continue
    base_name=$(basename "$jar_file" .jar)
    new_name=$(echo "$base_name" | sed -E "s/-[vV]?[0-9]+(\.[0-9]+)*(-[a-zA-Z0-9._-]+)*$//")
    new_path="${TMP_RENAMED_JARS}/${new_name}.jar"
    echo "处理：$jar_file -> $new_path"
    cp "$jar_file" "$new_path"
  done
  echo "✅ 重命名完成"
  ls -la "$TMP_RENAMED_JARS"
}
package_jars_zip() {
  local ZIP_NAME=${1:-"$JARS_ZIP"}

  echo "🗜️ 打包所有 JAR 文件成 ZIP: $ZIP_NAME"

  zip -j "$ZIP_NAME" "$TMP_RENAMED_JARS"/*.jar

  echo "✅ 打包完成"
  unzip -l "$ZIP_NAME"
}
extract_jars_zip() {
  local ZIP_PATH=${1:-"$JARS_ZIP"}
  local EXTRACT_DIR=${2:-"$TMP_EXTRACTED_JARS"}

  echo "📂 解压 ZIP 文件：$ZIP_PATH"

  mkdir -p "$EXTRACT_DIR"
  unzip "$ZIP_PATH" -d "$EXTRACT_DIR"

  echo "✅ 解压完成"
  ls -la "$EXTRACT_DIR"
}
extract_jars() {
  local JAR_ZIP_PATH=${1:-"$JARS_ZIP"}

  echo "📂 解压 JAR 到 $TMP_EXTRACTED_JARS"

  mkdir -p "$TMP_EXTRACTED_JARS"
  unzip "$JAR_ZIP_PATH" -d "$TMP_EXTRACTED_JARS"

  echo "✅ 解压完成"
  ls -la "$TMP_EXTRACTED_JARS"
}
install_mingw_wine() {
  echo "🔧 安装 Wine 和 MinGW 工具链（cross-compile Windows exe）"

  sudo dpkg --add-architecture i386
  sudo apt-get update -y
  sudo apt-get install -y \
    wine-stable wine32 \
    mingw-w64 binutils-mingw-w64-i686 gcc-mingw-w64-i686 g++-mingw-w64-i686 \
    jq unzip curl wget zip

  echo "✅ 工具链安装完成"
}
setup_launch4j() {
  echo "🔧 下载并配置 Launch4j（Linux 版）"

  wget -O launch4j.tgz "https://sourceforge.net/projects/launch4j/files/launch4j-3/3.50/launch4j-3.50-linux-x64.tgz/download"
  tar -xzf launch4j.tgz -C /tmp
  chmod +x /tmp/launch4j/launch4j

  # 强制使用系统 windres/ld
  mkdir -p /tmp/launch4j/bin
  ln -sf /usr/bin/i686-w64-mingw32-windres /tmp/launch4j/bin/windres
  ln -sf /usr/bin/i686-w64-mingw32-ld     /tmp/launch4j/bin/ld
  ln -sf /usr/bin/i686-w64-mingw32-ar     /tmp/launch4j/bin/ar

  # 测试
  /tmp/launch4j/bin/windres --version || exit 1
  echo "✅ Launch4j 配置完成"
  ls -lh /tmp/launch4j/
}
build_exe() {
  local TAG_NAME=$1
  local ICON_PATH=$2
  local REPO_NAME=$3

  echo "🔨 为每个 JAR 生成 Windows EXE"
  echo "工作目录：$(pwd)"

  [ ! -d "$TMP_EXTRACTED_JARS" ] && { echo "❌ $TMP_EXTRACTED_JARS 不存在"; exit 1; }

  mkdir -p "$TMP_EXTRACTED_EXES"

  for jar in "$TMP_EXTRACTED_JARS"/*.jar; do
    [ -f "$jar" ] || continue

    jar_name=$(basename "$jar" .jar)
    jar_name=$(echo "$jar_name" | sed -E 's/-v?[0-9]+(\.[0-9]+)*(-[a-zA-Z0-9]+)*$//')
    exe_file="$TMP_EXTRACTED_EXES/${jar_name}.exe"

    echo "======================================"
    echo "处理：$jar → $exe_file"

    # 提取 Main-Class
    main_class=$(unzip -p "$jar" META-INF/MANIFEST.MF | grep '^Main-Class:' | sed 's/^Main-Class: *//' | tr -d '\r')
    [ -z "$main_class" ] && { echo "⚠️ 跳过无 Main-Class 的 $jar"; continue; }
    echo "Main-Class: $main_class"

    # 版本处理
    local raw_tag="${TAG_NAME#v}"
#    local clean_ver=$(echo "$raw_tag" | sed 's/[^0-9.]//g' | sed 's/\.\.*/./g')
#    IFS='.' read -r major minor patch build <<< "$clean_ver"
    # 提取纯数字部分（移除所有非数字和点号的字符）
    local version_parts=($(echo "$raw_tag" | grep -oE '[0-9]+' | head -4))

    # 填充默认值，确保 4 段
    local major=${version_parts[0]:-0}
    local minor=${version_parts[1]:-0}
    local patch=${version_parts[2]:-0}
    local build=${version_parts[3]:-0}

    local file_ver="${major:-0}.${minor:-0}.${patch:-0}.${build:-0}"

    # icon 路径
    icon_path="$GITHUB_WORKSPACE/${ICON_PATH}"
    [ ! -f "$icon_path" ] && { echo "⚠️ 图标文件不存在：$icon_path"; icon_path=""; }

    # 生成 XML 配置
    config="/tmp/${jar_name}_config.xml"
    cat > "$config" << EOF
<launch4jConfig>
  <dontWrapJar>false</dontWrapJar>
  <headerType>console</headerType>
  <jar>$(realpath "$jar")</jar>
  <outfile>$exe_file</outfile>
  <errTitle>${REPO_NAME} Error</errTitle>
  <classPath><mainClass>$main_class</mainClass></classPath>
  <jre>
    <path>jre;%JAVA_HOME%;%PATH%</path>
    <minVersion>${JAVA_VERSION}.0</minVersion>
    <maxVersion>${JAVA_VERSION}.999</maxVersion>
    <jdkPreference>preferBundled</jdkPreference>
  </jre>
  <versionInfo>
    <fileVersion>$file_ver</fileVersion>
    <txtFileVersion>${TAG_NAME}</txtFileVersion>
    <productVersion>$file_ver</productVersion>
    <txtProductVersion>${TAG_NAME}</txtProductVersion>
    <fileDescription>${REPO_NAME} Standalone</fileDescription>
    <copyright>Copyright (c) 2023-$(date +%Y)</copyright>
    <productName>${REPO_NAME}</productName>
    <internalName>$jar_name</internalName>
    <originalFilename>$jar_name.exe</originalFilename>
  </versionInfo>
  <icon>${icon_path}</icon>
</launch4jConfig>
EOF

    # 执行 launch4j
    java -Djava.awt.headless=true -jar /tmp/launch4j/launch4j.jar "$config" || {
      echo "❌ Launch4j 失败"
      cat /tmp/launch4j.log 2>/dev/null || echo "无日志"
      exit 1
    }

    [ -f "$exe_file" ] || { echo "❌ EXE 未生成"; exit 1; }
    echo "✅ 生成成功：$exe_file ($(du -h "$exe_file" | cut -f1))"
  done

  echo "✅ 所有 EXE 生成完成"
  ls -lh "$TMP_EXTRACTED_EXES"
}
package_standalone() {
  local REPO_NAME=$1

  echo "📦 打包独立运行包（EXE + JRE + 配置文件）"

  [ ! -d "$TMP_EXTRACTED_JARS" ] && echo "⚠️ $TMP_EXTRACTED_JARS 不存在"
  [ ! -d "$TMP_EXTRACTED_EXES" ] && echo "⚠️ $TMP_EXTRACTED_EXES 不存在"

  mkdir -p "${REPO_NAME}"

  # 拷贝 exe
  echo "📋 拷贝 EXE 文件..."
  cp "$TMP_EXTRACTED_EXES"/*.exe "${REPO_NAME}/" 2>/dev/null || echo "⚠️ 无 EXE 文件"
  # 拷贝 jar
  echo "📋 拷贝 JAR 文件..."
  cp "$TMP_EXTRACTED_JARS"/*.jar "${REPO_NAME}/" 2>/dev/null || echo "⚠️ 无 jar 文件"

  # 拷贝 JRE
  echo "📋 拷贝 JRE..."
  mkdir -p "$TMP_EXTRACTED_EXES"/jre
  cp -r "$TMP_EXTRACTED_JARS"/jre/* "$TMP_EXTRACTED_EXES"/jre/ 2>/dev/null || echo "⚠️ JRE 不存在"
  cp -r "$TMP_EXTRACTED_EXES"/jre "${REPO_NAME}/" 2>/dev/null || echo "⚠️ JRE 拷贝失败"

  # 配置文件
  if [ -f "src/main/resources/application-prod.yml" ]; then
    cp "src/main/resources/application-prod.yml" "${REPO_NAME}/"
    echo "✅ 已拷贝 application-prod.yml"
  else
    cat > "${REPO_NAME}/application-prod.yml" << 'EOF'
server:
  port: 8081
ws:
  url: ws://localhost:8081/ws
  access-token-name: access-token
EOF
    echo "✅ 使用默认配置文件"
  fi
  # 生成使用说明 README.md
  echo "📄 生成使用说明 README.md..."
  cat > "${REPO_NAME}/README.md" << EOF
# ${REPO_NAME} 使用说明

## 📦 包内容

- \`*.exe\` - Windows 可执行文件（推荐）
- \`*.jar\` - Java 可执行文件（备用）
- \`jre/\` - 捆绑的 Java 运行环境
- \`application-prod.yml\` - 配置文件

## 🚀 快速开始

### 方式 1：运行 EXE（推荐）

双击 \`*.exe\` 或命令行运行：
\`\`\`bash
*.exe
\`\`\`

### 方式 2：运行 JAR

\`\`\`bash
# 使用捆绑的 JRE
jre\\bin\\java -jar *.jar

# 或使用系统 Java
java -jar *.jar
\`\`\`

## ⚙️ 配置说明

编辑 \`application-prod.yml\`：

\`\`\`yaml
server:
  port: 8081  # 修改端口
ws:
  url: ws://localhost:8081/ws
  access-token-name: access-token
\`\`\`

## 🌐 访问地址

启动后访问：\`http://localhost:8081/bgi\`

## 🛠️ 常见问题

### 1. 端口被占用
修改 \`application-prod.yml\` 中的 \`port\` 配置

### 2. 闪退/无法启动
- 检查是否有 Java 环境（如未使用捆绑 JRE）
- 查看日志：运行目录下的日志文件
- 确保防火墙允许 8081 端口

### 3. EXE 无法运行
- 需要 Windows 7 或更高版本
- 可能需要安装 Visual C++ Redistributable

## 📊 版本信息

- **版本**：${TAG_NAME:-unknown}
- **Java 版本**：${JAVA_VERSION:-unknown}
- **生成时间**：$(date '+%Y-%m-%d %H:%M:%S')

## 📞 技术支持

如有问题，请访问 GitHub Issues 或联系开发者。
EOF

  # 预览
  echo "📂 打包内容预览："
  ls -R "${REPO_NAME}/" || echo "无内容"
  # 预览
  echo "📂 打包内容预览："
  ls -R "${REPO_NAME}/" || echo "无内容"

  # 打包
  local ZIP_NAME="${REPO_NAME}-Windows.zip"
  echo "🗜️ 创建 ZIP 包：$ZIP_NAME"

  zip -r "$ZIP_NAME" "${REPO_NAME}/"* || { echo "❌ 打包失败"; exit 1; }

  echo "✅ 打包成功：$ZIP_NAME ($(du -h "$ZIP_NAME" | cut -f1))"
  unzip -l "$ZIP_NAME" | head -20
}
download_windows_jre() {
  local JAVA_VERSION=$1

  local API_URL="https://api.adoptium.net/v3/assets/latest/${JAVA_VERSION}/hotspot?image_type=jdk&os=windows&arch=x64&vendor=adoptium"

  echo " 下载 Windows JRE (Java ${JAVA_VERSION})"
  echo "API: $API_URL"

  local RESPONSE=$(curl -s --fail -L "$API_URL" || { echo "❌ API 请求失败"; exit 1; })

  # 提取下载链接
  local DOWNLOAD_URL=$(echo "$RESPONSE" | jq -r '
    if type == "array" then
      .[] | select(.binary.architecture == "x64") | .binary.package.link
    else
      .binary.package.link // empty
    end // .[0].binary.package.link // empty
  ')

  if [ -z "$DOWNLOAD_URL" ]; then
    echo "❌ 无法提取下载链接"
    echo "$RESPONSE" | jq . || echo "$RESPONSE"
    exit 1
  fi

  echo "下载链接：$DOWNLOAD_URL"
  local JDK_ZIP="jdk.zip"
  local JDK_EXTRACT="jdk_extract"
  wget -O "$JDK_ZIP" "$DOWNLOAD_URL" || { echo "❌ 下载失败"; exit 1; }
  unzip -q "$JDK_ZIP" -d "$JDK_EXTRACT"

  local JDK_ROOT=$(find "$JDK_EXTRACT" -mindepth 1 -maxdepth 1 -type d | head -1)
  [ -z "$JDK_ROOT" ] && { echo "❌ 未找到 JDK 根目录"; exit 1; }

  mkdir -p "$TMP_EXTRACTED_JARS/jre"

  if [[ "${JAVA_VERSION}" == "8" ]]; then
    if [ -d "$JDK_ROOT/jre" ]; then
      cp -r "$JDK_ROOT/jre/"* "$TMP_EXTRACTED_JARS/jre/"
    else
      echo "JDK8 无 jre 子目录，使用整个 JDK"
      cp -r "$JDK_ROOT/"* "$TMP_EXTRACTED_JARS/jre/"
    fi
  else
    cp -r "$JDK_ROOT/"* "$TMP_EXTRACTED_JARS/jre/"
  fi

  echo "✅ JRE 捆绑完成"
  du -sh "$TMP_EXTRACTED_JARS/jre"

  rm -rf "$JDK_ZIP" "$JDK_EXTRACT"
}
update_salt(){
  echo " 随机生成盐值"
  wget -q https://github.com/mikefarah/yq/releases/latest/download/yq_linux_amd64 -O /usr/local/bin/yq
  chmod +x /usr/local/bin/yq
  #  todo 随机生成盐值
  local RANDOM_SALT=$(openssl rand -hex 16)
  echo "正在生成随机盐值"
  #echo "生成的随机盐值：$RANDOM_SALT"

  yq -i ".sign.api.salt = \"$RANDOM_SALT\"" $BGI_TOOLS_YML
  sed -i "s/^VITE_BASE_SALT=.*/VITE_BASE_SALT=$RANDOM_SALT/" $FRONTEND_ENV
  echo "✅ 盐值已更新到配置文件"
}

update_check_token(){
  echo " 随机生成check-token"
  wget -q https://github.com/mikefarah/yq/releases/latest/download/yq_linux_amd64 -O /usr/local/bin/yq
  chmod +x /usr/local/bin/yq
  #  todo 随机生成check-token
  local check_token_name="token_$(openssl rand -hex 8)"
  local check_token=$(openssl rand -hex 16)
  echo "正在生成随机check-token"

  yq -i ".check.token.name = \"$check_token_name\"" $BGI_TOOLS_YML
  yq -i ".check.token.value = \"$check_token\"" $BGI_TOOLS_YML
  echo "✅ check-token已更新到配置文件"
}
update_config(){
#    echo "更新sign,timestamp别名"
    echo "正在更新配置文件"
    wget -q https://github.com/mikefarah/yq/releases/latest/download/yq_linux_amd64 -O /usr/local/bin/yq
    chmod +x /usr/local/bin/yq

    local SIGN_AS_NAME=$(yq '.sign.api.sign-as-name' $BGI_TOOLS_YML)
    local TIMESTAMP_AS_NAME=$(yq '.sign.api.timestamp-as-name' $BGI_TOOLS_YML)
    local CLIENT_PUBLIC_KEY_AS_NAME=$(yq '.sign.api.encryption-as-name' $BGI_TOOLS_YML)
    local CLIENT_ID_AS_NAME=$(yq '.sign.api.id-as-name' $BGI_TOOLS_YML)
    local ENABLE_DOUBLE_SYMMETRIC_ENCRYPTION=$(yq '.sign.api.enable-double-symmetric-encryption' $BGI_TOOLS_YML)

    if [ -z "$SIGN_AS_NAME" ] || [ -z "$TIMESTAMP_AS_NAME" ]; then
         echo "⚠️ 警告：sign.api 配置为空，跳过更新"
    else
         echo "正在更新 sign.api 配置"
         yq -i "(.springdoc.open.header.api[] | select(.name == \"sign\").value) = \"$SIGN_AS_NAME\"" $BGI_TOOLS_YML
         yq -i "(.springdoc.open.header.api[] | select(.name == \"timestamp\").value) = \"$TIMESTAMP_AS_NAME\"" $BGI_TOOLS_YML

         echo "✅ 已根据 sign.api 配置更新 springdoc.open.header.api"
         sed -i "s/^VITE_BASE_SIGN_AS_NAME=.*/VITE_BASE_SIGN_AS_NAME=$SIGN_AS_NAME/" $FRONTEND_ENV
         sed -i "s/^VITE_BASE_TIMESTAMP_AS_NAME=.*/VITE_BASE_TIMESTAMP_AS_NAME=$TIMESTAMP_AS_NAME/" $FRONTEND_ENV
         echo "✅ 已根据 sign.api 配置更新前端环境变量"
    fi

    if [ -z "$CLIENT_PUBLIC_KEY_AS_NAME" ]; then
        echo "⚠️ 警告：CLIENT_PUBLIC_KEY_AS_NAME 配置为空，跳过更新"
    else
        echo "正在更新 CLIENT_PUBLIC_KEY_AS_NAME 配置"
        sed -i "s/^VITE_BASE_CLIENT_PUBLIC_KEY=.*/VITE_BASE_CLIENT_PUBLIC_KEY=$CLIENT_PUBLIC_KEY_AS_NAME/" $FRONTEND_ENV
        echo "✅ 已根据 CLIENT_PUBLIC_KEY_AS_NAME 配置更新前端环境变量"
    fi

    if [ -z "$CLIENT_ID_AS_NAMEE" ]; then
        echo "⚠️ 警告：CLIENT_ID_AS_NAME 配置为空，跳过更新"
    else
        echo "正在更新 CLIENT_ID_AS_NAME 配置"
        sed -i "s/^VITE_BASE_CLIENT_ID_AS_NAME=.*/VITE_BASE_CLIENT_ID_AS_NAME=$CLIENT_ID_AS_NAMEE/" $FRONTEND_ENV
        echo "✅ 已根据 CLIENT_ID_AS_NAME 配置更新前端环境变量"
    fi

    if [ -z "$ENABLE_DOUBLE_SYMMETRIC_ENCRYPTION" ]; then
        echo "⚠️ 警告：ENABLE_DOUBLE_SYMMETRIC_ENCRYPTION 配置为空，跳过更新"
    else
        echo "正在更新 ENABLE_DOUBLE_SYMMETRIC_ENCRYPTION 配置"
        sed -i "s/^VITE_BASE_ENABLE_DOUBLE_SYMMETRIC_ENCRYPTION=.*/VITE_BASE_ENABLE_DOUBLE_SYMMETRIC_ENCRYPTION=$ENABLE_DOUBLE_SYMMETRIC_ENCRYPTION/" $FRONTEND_ENV
        echo "✅ 已根据 ENABLE_DOUBLE_SYMMETRIC_ENCRYPTION 配置更新前端环境变量"
    fi
}
