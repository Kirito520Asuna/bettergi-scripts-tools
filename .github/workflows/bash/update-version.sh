#/bin/bash
BGI_TOOLS="bgi-tools"
BGI_TOOLS_YML="bgi-tools/src/main/resources/application.yml"
BGI_TOOLS_YML_VERSION="bgi-tools.version"
BGI_TOOLS_PROJECT_VERSION="project-module.version"
PARENT_POM_XML_PATH="pom.xml"
update_version(){
  local TAG_NAME=$1
  #sed -i "s/<${BGI_TOOLS_PROJECT_VERSION}>[^<]*<\/${BGI_TOOLS_PROJECT_VERSION}>/<${BGI_TOOLS_PROJECT_VERSION}>${TAG_NAME}<\/${BGI_TOOLS_PROJECT_VERSION}>/" pom.xml

  #sudo apt install yq -y
                # 下载 Go 版本的 yq（mikefarah/yq），覆盖 Python 版本
  wget -q https://github.com/mikefarah/yq/releases/latest/download/yq_linux_amd64 -O /usr/local/bin/yq
  chmod +x /usr/local/bin/yq
  # 获取旧版本号
  local OLD_POM_VERSION=$(grep -oP "<${BGI_TOOLS_PROJECT_VERSION}>\K[^<]+" "${PARENT_POM_XML_PATH}")
  local OLD_YML_VERSION=$(yq ".${BGI_TOOLS_YML_VERSION}" "${BGI_TOOLS_YML}")
  # 处理新版本号
  local TAG_WITHOUT_V="${TAG_NAME}"
  local NEW_YML_VERSION="${TAG_WITHOUT_V#v}"

  echo "📋 版本号比对:"
  echo "   POM 旧版本：${OLD_POM_VERSION:-未设置}"
  echo "   POM 新版本：${TAG_NAME}"
  echo "   YML 旧版本：${OLD_YML_VERSION:-未设置}"
  echo "   YML 新版本：${NEW_YML_VERSION}"
  echo ""
  local pom_updated=false
  local yml_updated=false
  # 比对并更新 pom.xml
  if [ "${OLD_POM_VERSION}" = "${TAG_NAME}" ]; then
    echo "⏭️  POM 版本已是最新，跳过更新"
  else
    echo "🔄 更新 pom.xml..."
    sed -i "s/<${BGI_TOOLS_PROJECT_VERSION}>[^<]*<\/${BGI_TOOLS_PROJECT_VERSION}>/<${BGI_TOOLS_PROJECT_VERSION}>${TAG_NAME}<\/${BGI_TOOLS_PROJECT_VERSION}>/" "${PARENT_POM_XML_PATH}"
    echo "✅ POM 更新完成"
    pom_updated=true
  fi

  # 使用 yq 修改 application.yml
  #yq -i ".${BGI_TOOLS_YML_VERSION} = \"${NEW_YML_VERSION}\"" $BGI_TOOLS_YML
  # 比对并更新 application.yml
  if [ "${OLD_YML_VERSION}" = "${NEW_YML_VERSION}" ]; then
    echo "⏭️  YML 版本已是最新，跳过更新"
  else
    echo "🔄 更新 ${BGI_TOOLS_YML}..."
    if ! yq -i ".${BGI_TOOLS_YML_VERSION} = \"${NEW_YML_VERSION}\"" "${BGI_TOOLS_YML}"; then
      echo "❌ 更新 ${BGI_TOOLS_YML} 失败"
      exit 1
    fi
    echo "✅ YML 更新完成"
    yml_updated=true
  fi

  # 判断是否为正式版（v 开头后全数字 或 全数字）
  local is_release=false
  if [[ "$TAG_NAME" =~ ^v[0-9]+(\.[0-9]+)*$ ]] || [[ "$TAG_NAME" =~ ^[0-9]+(\.[0-9]+)*$ ]]; then
    is_release=true
    echo "🎉 检测到正式版：$TAG_NAME"
  else
    echo "📦 检测到开发版/测试版：$TAG_NAME"
  fi

  # 如果是正式版且有更新，则提交到 Git 仓库
  if [ "$is_release" = true ] && ([ "$pom_updated" = true ] || [ "$yml_updated" = true ]); then
    echo ""
    echo "🎯 正式版版本变更，提交到仓库..."

    # 配置 Git 用户信息
    git config --global user.name "GitHub Actions"
    git config --global user.email "actions@github.com"

    # 添加变更文件
    git add "${PARENT_POM_XML_PATH}" "${BGI_TOOLS_YML}"

    # 提交
    git commit -m "chore: release version ${TAG_NAME} [skip ci]"

    # 推送（使用 GITHUB_TOKEN）
    if ! git push https://${GITHUB_TOKEN}@github.com/${GITHUB_REPOSITORY}.git HEAD:${GITHUB_REF}; then
      echo "⚠️ Git 推送失败，但版本更新已完成"
    else
      echo "✅ Git 提交并推送成功"
    fi
  fi

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