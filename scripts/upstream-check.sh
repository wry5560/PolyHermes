#!/bin/bash
# upstream-check.sh - 检查上游仓库更新

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}=== 上游同步检查工具 ===${NC}\n"

# 检查 upstream remote
if ! git remote get-url upstream &>/dev/null; then
    echo -e "${RED}错误: upstream remote 未配置${NC}"
    echo "请运行: git remote add upstream https://github.com/WrBug/PolyHermes.git"
    exit 1
fi

echo -e "${YELLOW}[1/4] 获取上游更新...${NC}"
git fetch upstream

echo -e "\n${YELLOW}[2/4] 上游新提交 (最近20条):${NC}"
NEW_COMMITS=$(git log main..upstream/main --oneline 2>/dev/null | head -20)
if [ -z "$NEW_COMMITS" ]; then
    echo -e "${GREEN}没有新提交，你的分支是最新的！${NC}"
else
    echo "$NEW_COMMITS"
    COMMIT_COUNT=$(git log main..upstream/main --oneline 2>/dev/null | wc -l)
    echo -e "\n${BLUE}共 $COMMIT_COUNT 个新提交${NC}"
fi

echo -e "\n${YELLOW}[3/4] 文件变化统计:${NC}"
DIFF_STAT=$(git diff main upstream/main --stat 2>/dev/null | tail -20)
if [ -z "$DIFF_STAT" ]; then
    echo -e "${GREEN}没有文件差异${NC}"
else
    echo "$DIFF_STAT"
fi

echo -e "\n${YELLOW}[4/4] 上游最近 Release:${NC}"
if command -v gh &> /dev/null; then
    gh release list -R WrBug/PolyHermes -L 5 2>/dev/null || echo "无法获取 release 信息"
else
    echo "提示: 安装 gh CLI 可查看 releases (brew install gh / apt install gh)"
fi

echo -e "\n${BLUE}=== 可用操作 ===${NC}"
echo "• 合并所有更新:    git checkout main && git merge upstream/main"
echo "• 选择性合并提交:  git cherry-pick <commit-hash>"
echo "• 同步特定文件:    git checkout upstream/main -- path/to/file"
echo "• 查看详细差异:    git diff main upstream/main"
