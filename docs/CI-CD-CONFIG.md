# CI/CD 配置说明文档

## 概述

本项目已配置 GitHub Actions 自动化构建和测试流程，支持以下功能：

- ✅ 自动运行单元测试
- ✅ 自动构建 Debug APK
- ✅ 自动构建 Release APK（创建 tag 时）
- ✅ 测试覆盖率报告
- ✅ 构建产物自动上传

## 工作流文件

**位置**: `.github/workflows/android-ci.yml`

### 触发条件

| 事件 | 说明 |
|------|------|
| `push` | 推送到 `main`, `master`, `develop` 分支 |
| `pull_request` | 向主分支发起 PR |
| `release` | 创建新的 GitHub Release |
| `tag` | 创建新的 tag（`refs/tags/*`） |

### 工作流任务

#### 1. Build & Test (主任务)

每次 push 或 PR 时自动运行：

```yaml
步骤:
1. 检出代码
2. 设置 JDK 17 (Temurin)
3. 缓存 Gradle 依赖
4. 运行单元测试 (./gradlew test)
5. 构建 Debug APK
6. 上传 Debug APK 为 artifact（保留 30 天）
7. 生成测试覆盖率报告 (jacoco)
```

#### 2. Release Build (可选)

仅当创建 tag 或 Release 时运行：

```yaml
步骤:
1. 构建 Release APK
2. 自动上传到 GitHub Releases
3. 构建 AAB (Android App Bundle)
4. 上传 AAB 到 GitHub Releases
```

## 使用指南

### 启用 GitHub Actions

1. 提交并推送配置：
```bash
cd /vol1/1000/openclaw/projects/ssh-pad
git add .github/workflows/android-ci.yml
git commit -m "ci: 添加 GitHub Actions CI/CD 配置"
git push
```

2. 访问 GitHub Actions 页面查看构建状态：
   - URL: `https://github.com/<owner>/ssh-pad/actions`

### 首次运行检查清单

- [ ] 确认 `gradlew` 有执行权限 (`chmod +x gradlew`)
- [ ] 确认项目使用 JDK 17
- [ ] 确认 `build.gradle.kts` 配置了测试任务
- [ ] (可选) 配置 Release 签名密钥

### 配置 Release 签名（可选）

如需自动签名 Release APK，需在 GitHub Secrets 中添加：

| Secret 名称 | 说明 |
|-------------|------|
| `RELEASE_STORE_FILE` | Keystore 文件（Base64 编码） |
| `RELEASE_KEY_ALIAS` | 密钥别名 |
| `RELEASE_STORE_PASSWORD` | 密钥库密码 |
| `RELEASE_KEY_PASSWORD` | 密钥密码 |

然后在 `android-ci.yml` 中取消注释相关环境变量。

## 构建产物

### Debug 构建

- **位置**: `app/build/outputs/apk/debug/app-debug.apk`
- **保留期**: 30 天
- **用途**: 测试、内部验证

### Release 构建

- **APK**: `app/build/outputs/apk/release/app-release-unsigned.apk`
- **AAB**: `app/build/outputs/bundle/release/app-release.aab`
- **用途**: 正式发布、Google Play 上传

## 测试覆盖率

工作流会生成 JaCoCo 测试覆盖率报告：

```bash
./gradlew jacocoTestReport
```

报告位置：`app/build/reports/jacoco/`

## 故障排查

### 常见问题

1. **Gradle 缓存未命中**
   - 检查 `gradle-wrapper.properties` 版本是否变化
   - 检查 `build.gradle.kts` 是否有重大变更

2. **构建失败**
   - 查看 Actions 日志输出
   - 本地运行 `./gradlew test` 验证

3. **签名失败**
   - 检查 Secrets 配置是否正确
   - 确认 Keystore 文件 Base64 编码正确

### 查看构建日志

1. 访问：`https://github.com/<owner>/ssh-pad/actions`
2. 点击具体的 workflow run
3. 展开 `Build & Test` 或 `Release Build` 查看步骤详情

## 优化建议

1. **缓存优化**: Gradle 缓存已配置，首次构建后速度会显著提升
2. **并行构建**: 可考虑添加 `--parallel` 标志加速构建
3. **增量测试**: PR 时可考虑只运行变更相关的测试
4. **构建时间监控**: 定期查看 Actions 运行时间，优化慢任务

## 下一步

- [ ] 提交配置到 GitHub
- [ ] 验证首次构建成功
- [ ] (可选) 配置代码质量检查 (lint, detekt)
- [ ] (可选) 配置自动发布到 Google Play
- [ ] (可选) 添加通知机制 (Slack, 邮件)

---

**配置时间**: 2026-03-24  
**配置版本**: v1.0  
**项目**: ssh-pad
