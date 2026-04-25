config_git_user(){
  local name=${1}
  local email=${2}
  git config --global user.name "$name"
  git config --global user.email "$email"
}

check_force_mode() {
  local event_name="${1}"
  local force_push="${2}"

  if [[ "${event_name}" == "workflow_dispatch" && "${force_push}" == "true" ]]; then
    echo "🔥 强制推送模式已启用（跳过检查）"
    echo "true"  # 正确返回值：用 echo 输出
  else
    echo "✅ 正常同步模式（执行检查）"
    echo "false" # 正确返回值
  fi
}
check_github_gitee_diff(){
  local GITEE_USERNAME=${1}
  local GITEE_TOKEN=${2}
  local GITEE_REPO_NAME=${3:-"bettergi-scripts-tools"}
  local GITEE_BRANCH=${4:-"master"}
  echo "🔍 检查 GitHub 与 Gitee 的差异..."

  local LOCAL_SHA=$(git rev-parse HEAD)
  echo "本地最新提交：$LOCAL_SHA"

  local GITEE_URL="https://${GITEE_USERNAME}:${GITEE_TOKEN}@gitee.com/${GITEE_USERNAME}/${GITEE_REPO_NAME}.git"

  if GITEE_SHA=$(git ls-remote "$GITEE_URL" "$GITEE_BRANCH" 2>/dev/null | awk '{print $1}'); then
      if [ -n "$GITEE_SHA" ]; then
          echo "Gitee 最新提交：$GITEE_SHA"
          if [ "$LOCAL_SHA" = "$GITEE_SHA" ]; then
             echo "✅ 无需同步，Gitee 已是最新"
             echo "has_changes=false" >> $GITHUB_OUTPUT
#             echo "false"
             exit 0
          else
            echo "⚠️ 检测到差异，需要同步"
            echo "has_changes=true" >> $GITHUB_OUTPUT
#            echo "true"
            return
          fi
      fi
  fi

  echo "⚠️ 无法访问 Gitee 仓库（可能是首次同步或权限问题）"
  echo "将执行完整同步"
  echo "has_changes=true" >> $GITHUB_OUTPUT
#  echo "true"
}
push_gitee(){
  local GITEE_USERNAME=${1}
  local GITEE_TOKEN=${2}
  local GITEE_REPO_NAME=${3:-"bettergi-scripts-tools"}
  local GITEE_BRANCH=${4:-"master"}
  local force_mode=${5}
  local SOURCE_BRANCH=${6:-$(git branch --show-current)}
  mode_text=""
  [[ "$force_mode" == "true" ]] && mode_text="[强制推送模式] "

  echo "📦 ${mode_text}开始推送到 Gitee..."

  GITEE_URL="https://${GITEE_USERNAME}:${GITEE_TOKEN}@gitee.com/${GITEE_USERNAME}/${GITEE_REPO_NAME}.git"
  git remote remove gitee 2>/dev/null || true
  git remote add gitee "$GITEE_URL"

  echo "✓ Gitee remote 配置完成"
  echo ""

  echo "📦 推送分支 (${SOURCE_BRANCH} ->${GITEE_BRANCH})..."
  if git push -f gitee "${SOURCE_BRANCH}:${GITEE_BRANCH}" 2>&1; then
    echo "✓ 分支推送成功"
  else
    echo "✗ 分支推送失败，请检查 Token 权限和用户名"
    return 1
  fi
  echo "✓ 分支推送完成"
  echo ""

  echo "🏷️ 推送所有标签..."
  local_tag_count=$(git tag -l | wc -l)
  git push -f gitee --tags
  echo "✓ 标签推送完成（共 $local_tag_count 个）"
}