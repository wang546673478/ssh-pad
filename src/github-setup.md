# GitHub 仓库设置

## 仓库信息

- **名称:** ssh-pad
- **描述:** Android Tablet SSH Client - Material 3, Keyboard Friendly
- **可见性:** 公开 (开源)
- **许可证:** Apache 2.0

## 仓库结构

```
ssh-pad/
├── .github/
│   ├── workflows/
│   │   ├── build.yml      # CI 构建
│   │   └── release.yml    # 自动发布
│   └── ISSUE_TEMPLATE/
├── app/
│   └── src/
├── docs/
│   ├── README.md
│   ├── CONTRIBUTING.md
│   └── ARCHITECTURE.md
├── fastlane/               # Google Play 元数据
└── README.md
```

## CI/CD 配置

### GitHub Actions

| Workflow | 触发条件 | 任务 |
|----------|----------|------|
| build | PR/push | 构建 + 测试 |
| release | tag push | 构建 APK + 发布 |
| lint | PR | 代码风格检查 |

## 分支策略

- `main` - 稳定版本
- `develop` - 开发分支
- `feature/*` - 功能分支
- `release/*` - 发布分支

---

*创建时间：2026-04-05*
*负责人：张工*
*状态：待创建*
