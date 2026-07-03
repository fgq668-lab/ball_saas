# GitHub 发布清单

## 已准备内容

- README 项目首页说明
- 管理后台截图
- 部署说明
- 架构、需求、测试文档
- 最终测试报告
- Docker Compose 配置
- 本地测试脚本
- 服务器冒烟脚本

## 建议提交前确认

- 仓库是公开还是私有。
- 是否允许 README 中展示测试环境截图。
- 是否需要隐藏测试服务器 IP。当前 README 使用 `<server-ip>` 占位，历史文档中仍有测试 IP 记录。
- 是否需要添加开源许可证，例如 MIT、Apache-2.0，或保持无 License。
- `.agents`、`.codex` 等 Codex 工作目录已加入 `.gitignore`，不会提交。

## 推荐 Git 操作

如果当前目录不是有效 Git 仓库：

```bash
git init
git add README.md deploy docs backend admin miniprogram scripts .gitignore
git commit -m "docs: prepare project readme and deployment guide"
git branch -M main
git remote add origin <github-url>
git push -u origin main
```

如果你提供的是已有仓库地址，需要先确认：

```bash
git remote -v
git status
```

再决定是添加 remote、切换分支，还是合并已有历史。

## 当前不应提交的内容

- `backend/target/`
- `admin/node_modules/`
- `admin/dist/`
- `.m2/`
- `.tools/`
- `*.pem`、`*.key`、`.env*`

这些内容已经在 `.gitignore` 中排除。

