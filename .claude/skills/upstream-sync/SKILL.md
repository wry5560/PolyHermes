---
name: upstream-sync
description: 管理上游仓库同步。检查上游更新、查看差异、合并或 cherry-pick 上游变更。当用户提到"上游"、"同步"、"upstream"、"合并上游"、"检查更新"时使用此技能。
allowed-tools: Bash(git:*), Bash(./scripts/upstream-check.sh), Read, Grep, Glob
---

# 上游同步工作流 (Upstream Sync)

## 仓库配置

本项目是 WrBug/PolyHermes 的 fork，remote 配置如下：
- `origin`: 用户的 fork (wry5560/PolyHermes) - 可推送
- `upstream`: 原始仓库 (WrBug/PolyHermes) - 只读

分支策略：
- `main`: 用户的主分支，包含所有自定义修改
- `upstream-sync`: 跟踪上游的本地分支
- `feature/*`, `fix/*`: 用户的功能/修复分支

## 快速命令

### 检查上游更新
```bash
./scripts/upstream-check.sh
```

### 获取上游最新代码
```bash
git fetch upstream
```

### 查看上游新提交
```bash
git log main..upstream/main --oneline
```

### 查看文件差异
```bash
git diff main upstream/main --stat   # 文件统计
git diff main upstream/main          # 详细差异
```

## 合并策略

### 方式 A：合并全部更新（推荐）
```bash
git checkout main
git merge upstream/main
# 解决冲突后
git push origin main
```

### 方式 B：Cherry-pick 特定提交
```bash
git log upstream/main --oneline      # 查看提交列表
git checkout main
git cherry-pick <commit-hash>        # 选择性合并
```

### 方式 C：只同步特定文件
```bash
git checkout main
git checkout upstream/main -- path/to/file
git commit -m "sync: 从上游同步 file"
```

## 更新 upstream-sync 分支

保持 upstream-sync 与上游一致（用于对比）：
```bash
git checkout upstream-sync
git reset --hard upstream/main
git push origin upstream-sync --force
```

## 提交规范

- 自己的修改: `custom: xxx` 或 `custom-fix: xxx`
- 同步上游: `sync: 合并上游 vX.X.X`

## 冲突处理

```bash
# 查看冲突文件
git status

# 解决冲突后
git add <file>
git merge --continue
# 或
git cherry-pick --continue
```

## 查看上游 Release

```bash
gh release list -R WrBug/PolyHermes -L 10
```

## 工作流程图

```
upstream/main  ←── 上游原始代码（只读）
      │
      │ git fetch upstream
      ▼
upstream-sync  ←── 跟踪上游的本地分支
      │
      │ 评估 & 选择性合并
      ▼
    main  ←── 你的主分支（包含所有修改）
      │
      ├── feature/xxx
      └── fix/xxx
```
