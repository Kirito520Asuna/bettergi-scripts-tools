#!/bin/bash
maven_build() {
  local SKIP_TESTS=${1:-false}

  echo "🔨 使用 Maven 构建项目"

  if [ "$SKIP_TESTS" = "true" ]; then
    echo "⏭️ 跳过测试"
    mvn clean package -B -DskipTests -Dquickly
  else
    mvn clean package -B -Dquickly
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

  echo "🔄 重命名 JAR 文件（移除版本号）"

  mkdir -p /tmp/renamed-jars

  if [ -z "$JAR_DIR_PREFIX" ]; then
    echo "查找匹配的 JAR 文件 (*/target/*.jar):"
    find . -type f -path "*/target/*.jar" -exec bash -c '
      for jar_file; do
        [ -f "$jar_file" ] || continue
        base_name=$(basename "$jar_file" .jar)
        #new_name=$(echo "$base_name" | sed -E "s/-v?[0-9]+(\.[0-9]+)*(-[a-zA-Z0-9]+)*$//")
        #new_name=$(echo "$base_name" | sed -E "s/-[vV]?[0-9]+.*$//"
        # 匹配：-v1.2.3, -1.2.3, -v0.0.7-dev3.2 等（从最后一个连字符后的版本号开始）
        new_name=$(echo "$base_name" | sed -E "s/-[vV]?[0-9]+(\.[0-9]+)*(-[a-zA-Z0-9._-]+)*$//")
        new_path="/tmp/renamed-jars/${new_name}.jar"
        echo "处理：$jar_file -> $new_path"
        cp "$jar_file" "$new_path"
      done
    ' _ {} +
  else
    echo "查找匹配的 JAR 文件 (*/${JAR_DIR_PREFIX}/*/target/*.jar, */${JAR_DIR_PREFIX}/target/*.jar):"
    find . -type f \( -path "*/${JAR_DIR_PREFIX}/*/target/*.jar" -o -path "*/${JAR_DIR_PREFIX}/target/*.jar" \) -exec bash -c '
      for jar_file; do
        [ -f "$jar_file" ] || continue
        base_name=$(basename "$jar_file" .jar)
        #new_name=$(echo "$base_name" | sed -E "s/-v?[0-9]+(\.[0-9]+)*(-[a-zA-Z0-9]+)*$//")
        #new_name=$(echo "$base_name" | sed -E "s/-[vV]?[0-9]+.*$//"
        # 匹配：-v1.2.3, -1.2.3, -v0.0.7-dev3.2 等（从最后一个连字符后的版本号开始）
        new_name=$(echo "$base_name" | sed -E "s/-[vV]?[0-9]+(\.[0-9]+)*(-[a-zA-Z0-9._-]+)*$//")
        new_path="/tmp/renamed-jars/${new_name}.jar"
        echo "处理：$jar_file -> $new_path"
        cp "$jar_file" "$new_path"
      done
    ' _ {} +
  fi

  echo "✅ 重命名完成"
  ls -la /tmp/renamed-jars
}
package_jars_zip() {
  local ZIP_NAME=${1:-"jars.zip"}

  echo "🗜️ 打包所有 JAR 文件成 ZIP: $ZIP_NAME"

  zip -j "$ZIP_NAME" /tmp/renamed-jars/*.jar

  echo "✅ 打包完成"
  unzip -l "$ZIP_NAME"
}
extract_jars_zip() {
  local ZIP_PATH=${1:-"jars.zip"}
  local EXTRACT_DIR=${2:-"/tmp/extracted-jars"}

  echo "📂 解压 ZIP 文件：$ZIP_PATH"

  mkdir -p "$EXTRACT_DIR"
  unzip "$ZIP_PATH" -d "$EXTRACT_DIR"

  echo "✅ 解压完成"
  ls -la "$EXTRACT_DIR"
}
extract_jars() {
  local JAR_ZIP_PATH=${1:-"jars.zip"}

  echo "📂 解压 JAR 到 /tmp/extracted-jars"

  mkdir -p /tmp/extracted-jars
  unzip "$JAR_ZIP_PATH" -d /tmp/extracted-jars

  echo "✅ 解压完成"
  ls -la /tmp/extracted-jars
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

  [ ! -d "/tmp/extracted-jars" ] && { echo "❌ /tmp/extracted-jars 不存在"; exit 1; }

  mkdir -p /tmp/extracted-exes

  for jar in /tmp/extracted-jars/*.jar; do
    [ -f "$jar" ] || continue

    jar_name=$(basename "$jar" .jar)
    jar_name=$(echo "$jar_name" | sed -E 's/-v?[0-9]+(\.[0-9]+)*(-[a-zA-Z0-9]+)*$//')
    exe_file="/tmp/extracted-exes/${jar_name}.exe"

    echo "======================================"
    echo "处理：$jar → $exe_file"

    # 提取 Main-Class
    main_class=$(unzip -p "$jar" META-INF/MANIFEST.MF | grep '^Main-Class:' | sed 's/^Main-Class: *//' | tr -d '\r')
    [ -z "$main_class" ] && { echo "⚠️ 跳过无 Main-Class 的 $jar"; continue; }
    echo "Main-Class: $main_class"

    # 版本处理
    raw_tag="${TAG_NAME#v}"
    clean_ver=$(echo "$raw_tag" | sed 's/[^0-9.]//g' | sed 's/\.\.*/./g')
    IFS='.' read -r major minor patch build <<< "$clean_ver"
    file_ver="${major:-0}.${minor:-0}.${patch:-0}.${build:-0}"

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
  ls -lh /tmp/extracted-exes
}
package_standalone() {
  local REPO_NAME=$1

  echo "📦 打包独立运行包（EXE + JRE + 配置文件）"

  [ ! -d "/tmp/extracted-jars" ] && echo "⚠️ /tmp/extracted-jars 不存在"
  [ ! -d "/tmp/extracted-exes" ] && echo "⚠️ /tmp/extracted-exes 不存在"

  mkdir -p "${REPO_NAME}"

  # 拷贝 exe
  echo "📋 拷贝 EXE 文件..."
  cp /tmp/extracted-exes/*.exe "${REPO_NAME}/" 2>/dev/null || echo "⚠️ 无 EXE 文件"
  # 拷贝 jar
  echo "📋 拷贝 JAR 文件..."
  cp /tmp/extracted-jars/*.jar "${REPO_NAME}/" 2>/dev/null || echo "⚠️ 无 jar 文件"

  # 拷贝 JRE
  echo "📋 拷贝 JRE..."
  mkdir -p /tmp/extracted-exes/jre
  cp -r /tmp/extracted-jars/jre/* /tmp/extracted-exes/jre/ 2>/dev/null || echo "⚠️ JRE 不存在"
  cp -r /tmp/extracted-exes/jre "${REPO_NAME}/" 2>/dev/null || echo "⚠️ JRE 拷贝失败"

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

  wget -O jdk.zip "$DOWNLOAD_URL" || { echo "❌ 下载失败"; exit 1; }
  unzip -q jdk.zip -d jdk_extract

  local JDK_ROOT=$(find jdk_extract -mindepth 1 -maxdepth 1 -type d | head -1)
  [ -z "$JDK_ROOT" ] && { echo "❌ 未找到 JDK 根目录"; exit 1; }

  mkdir -p /tmp/extracted-jars/jre

  if [[ "${JAVA_VERSION}" == "8" ]]; then
    if [ -d "$JDK_ROOT/jre" ]; then
      cp -r "$JDK_ROOT/jre/"* /tmp/extracted-jars/jre/
    else
      echo "JDK8 无 jre 子目录，使用整个 JDK"
      cp -r "$JDK_ROOT/"* /tmp/extracted-jars/jre/
    fi
  else
    cp -r "$JDK_ROOT/"* /tmp/extracted-jars/jre/
  fi

  echo "✅ JRE 捆绑完成"
  du -sh /tmp/extracted-jars/jre

  rm -rf jdk.zip jdk_extract
}
