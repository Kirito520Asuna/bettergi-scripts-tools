#/bin/bash
BGI_TOOLS="bgi-tools"
BGI_TOOLS_YML="bgi-tools/src/main/resources/application.yml"
BGI_TOOLS_YML_VERSION="bgi-tools.version"
BGI_TOOLS_PROJECT_VERSION="project-module.version"
PARENT_POM_XML_PATH="pom.xml"
update_version(){
  local TAG_NAME=$1
  sed -i "s/<${BGI_TOOLS_PROJECT_VERSION}>[^<]*<\/${BGI_TOOLS_PROJECT_VERSION}>/<${BGI_TOOLS_PROJECT_VERSION}>${TAG_NAME}<\/${BGI_TOOLS_PROJECT_VERSION}>/" pom.xml

  #sudo apt install yq -y
                # 下载 Go 版本的 yq（mikefarah/yq），覆盖 Python 版本
  wget -q https://github.com/mikefarah/yq/releases/latest/download/yq_linux_amd64 -O /usr/local/bin/yq
  chmod +x /usr/local/bin/yq

  # 修复变量展开语法错误
  local TAG_WITHOUT_V="${TAG_NAME}"
  local NEW_YML_VERSION="${TAG_WITHOUT_V#v}"

  # 使用 yq 修改 application.yml
  yq -i ".${BGI_TOOLS_YML_VERSION} = \"${NEW_YML_VERSION}\"" $BGI_TOOLS_YML

  #打印修改后的文件内容
  echo "═══════════════════════════════════════"
  echo "📄 修改后的 pom.xml (${BGI_TOOLS_PROJECT_VERSION}):"
  echo "═══════════════════════════════════════"
  grep -A 2 -B 2 "${BGI_TOOLS_PROJECT_VERSION}" "${PARENT_POM_XML_PATH}"

  echo ""
  echo "═══════════════════════════════════════"
  echo "📄 修改后的 $BGI_TOOLS_YML (${BGI_TOOLS} 部分):"
  echo "═══════════════════════════════════════"
  yq ".${BGI_TOOLS}" $BGI_TOOLS_YML

  echo ""
  echo "═══════════════════════════════════════"
  echo "✅ 版本号验证:"
  echo "═══════════════════════════════════════"
  echo "POM 版本：$(grep -oP "<${BGI_TOOLS_PROJECT_VERSION}>\K[^<]+" "${PARENT_POM_XML_PATH}")"
  echo "YML 版本：$(yq ".${BGI_TOOLS_YML_VERSION}" $BGI_TOOLS_YML)"

}

#update_version "$1"