# SSH-Pad 部署准备报告

**报告日期：** 2026-03-24  
**项目版本：** v0.1.0-alpha (versionCode: 1)  
**报告部门：** 工部  

---

## 一、APK 构建状态

### 1.1 Debug APK ✅

**输出位置：**
```
/vol1/1000/openclaw/projects/ssh-pad/app/build/outputs/apk/debug/app-debug.apk
```

**文件信息：**
- 文件大小：56MB
- Application ID: `com.sshpad.app.debug`
- 版本名称：0.1.0-alpha
- 版本代码：1
- 签名状态：已签名（Debug 自动签名）
- V2 签名：已启用

**验证方法：**
```bash
# 使用 apksigner 验证
apksigner verify --verbose app/build/outputs/apk/debug/app-debug.apk
```

### 1.2 Release APK ✅

**输出位置：**
```
/vol1/1000/openclaw/projects/ssh-pad/app/build/outputs/apk/release/app-release-unsigned.apk
```

**文件信息：**
- 文件大小：41MB
- Application ID: `com.sshpad.app`
- 版本名称：0.1.0-alpha
- 版本代码：1
- 签名状态：**未签名**（需配置 Release 签名）
- V2 签名：已启用
- V1/V3/V4 签名：未启用

**⚠️ 注意事项：**
- Release APK 未签名，需要配置签名后才能发布
- 签名后文件名会变为 `app-release.apk`

---

## 二、签名配置状态 ⚠️

### 2.1 当前状态

**检查结果：**
- ❌ 未在 `build.gradle.kts` 中配置 signingConfig
- ❌ 未发现 `keystore.properties` 文件
- ❌ 未发现 `.keystore` 或 `.jks` 密钥库文件
- ✅ Gradle 配置支持 V2 签名

### 2.2 配置签名步骤

**步骤 1: 生成密钥库**
```bash
keytool -genkey -v -keystore ssh-pad-release.keystore \
  -alias ssh-pad \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass <STOREPASS> \
  -keypass <KEYPASS> \
  -dname "CN=SSH-Pad, OU=Development, O=SSH-Pad, L=City, S=Province, C=CN"
```

**步骤 2: 创建 keystore.properties**
```properties
# 文件位置：project-root/keystore.properties
storeFile=/vol1/1000/openclaw/projects/ssh-pad/ssh-pad-release.keystore
storePassword=<STOREPASS>
keyAlias=ssh-pad
keyPassword=<KEYPASS>
```

**步骤 3: 更新 build.gradle.kts**
```kotlin
// 在 app/build.gradle.kts 的 android 块中添加
signingConfigs {
    create("release") {
        val keystorePropertiesFile = rootProject.file("keystore.properties")
        val keystoreProperties = java.util.Properties()
        if (keystorePropertiesFile.exists()) {
            keystoreProperties.load(java.io.FileInputStream(keystorePropertiesFile))
        }
        
        storeFile = file(keystoreProperties["storeFile"] ?: System.getenv("RELEASE_STORE_FILE"))
        storePassword = keystoreProperties["storePassword"]?.toString() ?: System.getenv("RELEASE_STORE_PASSWORD")
        keyAlias = keystoreProperties["keyAlias"]?.toString() ?: System.getenv("RELEASE_KEY_ALIAS")
        keyPassword = keystoreProperties["keyPassword"]?.toString() ?: System.getenv("RELEASE_KEY_PASSWORD")
    }
}

buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
        isMinifyEnabled = false
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

**安全建议：**
- 将 `keystore.properties` 添加到 `.gitignore`
- 密钥库文件不要提交到 Git
- 在 CI/CD 中使用环境变量传递签名信息

---

## 三、发布渠道准备

### 3.1 GitHub Releases ✅

**仓库信息：**
- 仓库 URL: https://github.com/wang546673478/ssh-pad
- Git Remote: 已配置

**发布流程：**

**方法 A: 使用 gh CLI**
```bash
# 1. 创建 Release
gh release create v0.1.0-alpha \
  --title "SSH-Pad v0.1.0-alpha" \
  --notes-file RELEASE_NOTES.md \
  --prerelease

# 2. 上传 APK
gh release upload v0.1.0-alpha \
  app/build/outputs/apk/release/app-release.apk \
  --clobber
```

**方法 B: 手动上传**
1. 访问 https://github.com/wang546673478/ssh-pad/releases/new
2. 创建标签 v0.1.0-alpha
3. 填写发布说明
4. 勾选 "Set as a pre-release"
5. 上传 APK 文件
6. 点击 "Publish release"

**发布说明模板：**
```markdown
## 版本亮点
SSH-Pad v0.1.0-alpha 是首个 Alpha 测试版本，包含核心 SSH 连接功能。

## 功能特性
- SSH 密码和私钥认证
- 安全的凭据存储
- 终端仿真界面
- Keep-Alive 机制

## 系统要求
- Android 8.0+ (API 26+)
- 512MB 内存（推荐 1GB+）

## 下载
- [SSH-Pad v0.1.0-alpha.apk](链接)

## 已知问题
详见 [Issues](https://github.com/wang546673478/ssh-pad/issues)
```

### 3.2 酷安（CoolAPK）⏳

**准备状态检查：**

| 项目 | 状态 | 说明 |
|------|------|------|
| 应用描述 | ✅ 已完成 | 见 COOLAPK_MATERIALS.md |
| 隐私政策 | ✅ 已完成 | PRIVACY_POLICY.md |
| 应用图标 | ⏳ 待准备 | 512x512 PNG |
| 应用截图 | ⏳ 待准备 | 10 张 1080x2400 PNG |
| 签名 APK | ⏳ 待配置 | 需配置 Release 签名 |
| 开发者账号 | ❓ 待确认 | 需登录酷安开发者后台 |

**上传流程：**

1. **登录开发者后台**
   - URL: https://developer.coolapk.com/
   - 需要开发者账号

2. **创建新应用**
   - 应用名称：SSH-Pad
   - 包名：com.sshpad.app
   - 分类：工具 > 效率

3. **上传材料**
   - APK 文件（已签名）
   - 应用图标（512x512 PNG）
   - 应用截图（10 张）
   - 应用描述
   - 隐私政策链接

4. **提交审核**
   - 审核时间：1-3 个工作日
   - 保持联系方式畅通

**注意事项：**
- 明确标注为 "Alpha 测试版"
- 隐私政策需可公开访问（GitHub 链接即可）
- 准备好用户反馈邮箱

---

## 四、CI/CD 状态 ❌

### 4.1 GitHub Actions

**当前状态：**
- ❌ 未配置 GitHub Actions 工作流
- ❌ 无 `.github/workflows/` 目录

**建议配置：**

**文件：`.github/workflows/build.yml`**
```yaml
name: Build and Release

on:
  push:
    branches: [ main ]
    tags:
      - 'v*'
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Build Debug APK
      run: ./gradlew assembleDebug
    
    - name: Upload Debug APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
    
    - name: Build Release APK (if tag)
      if: startsWith(github.ref, 'refs/tags/v')
      env:
        RELEASE_STORE_FILE: ${{ secrets.RELEASE_STORE_FILE }}
        RELEASE_STORE_PASSWORD: ${{ secrets.RELEASE_STORE_PASSWORD }}
        RELEASE_KEY_ALIAS: ${{ secrets.RELEASE_KEY_ALIAS }}
        RELEASE_KEY_PASSWORD: ${{ secrets.RELEASE_KEY_PASSWORD }}
      run: |
        echo $RELEASE_STORE_FILE | base64 --decode > release.keystore
        ./gradlew assembleRelease
    
    - name: Upload Release APK to GitHub
      if: startsWith(github.ref, 'refs/tags/v')
      uses: softprops/action-gh-release@v1
      with:
        files: app/build/outputs/apk/release/app-release.apk
```

**配置 Secrets（GitHub 仓库设置）：**
- `RELEASE_STORE_FILE`: Base64 编码的密钥库文件
- `RELEASE_STORE_PASSWORD`: 密钥库密码
- `RELEASE_KEY_ALIAS`: 密钥别名
- `RELEASE_KEY_PASSWORD`: 密钥密码

### 4.2 自动化测试

**当前状态：**
- ✅ 项目包含测试依赖（JUnit, MockK, Espresso）
- ❌ 未配置自动测试运行

**运行本地测试：**
```bash
# 单元测试
./gradlew testDebugUnitTest

# Instrumentation 测试（需要设备/模拟器）
./gradlew connectedDebugAndroidTest
```

**建议：** 在 GitHub Actions 中添加测试步骤
```yaml
- name: Run Unit Tests
  run: ./gradlew testDebugUnitTest

- name: Upload Test Report
  uses: actions/upload-artifact@v3
  if: always()
  with:
    name: test-report
    path: app/build/reports/tests/
```

---

## 五、发布检查清单

### P0 - 必须完成

- [x] Debug APK 构建成功
- [x] Release APK 构建成功（未签名）
- [ ] 配置 Release 签名
- [ ] 签名 Release APK
- [ ] 创建 GitHub Release
- [ ] 上传 APK 到 GitHub

### P1 - 重要项目

- [ ] 准备应用图标（512x512 PNG）
- [ ] 准备应用截图（10 张）
- [ ] 配置 GitHub Actions CI/CD
- [ ] 酷安开发者账号准备
- [ ] 提交酷安审核

### P2 - 建议完成

- [ ] 配置自动化测试
- [ ] 设置用户反馈邮箱
- [ ] 创建 GitHub Issue 模板
- [ ] 准备常见问题回复模板

---

## 六、发布步骤总结

### 阶段一：签名配置（1 小时）
1. 生成密钥库
2. 创建 keystore.properties
3. 更新 build.gradle.kts
4. 重新构建 Release APK
5. 验证签名

### 阶段二：GitHub Release（30 分钟）
1. 准备发布说明
2. 创建 GitHub Release
3. 上传签名后的 APK
4. 验证下载链接

### 阶段三：酷安上架（2-3 小时）
1. 准备应用图标和截图
2. 登录酷安开发者后台
3. 填写应用信息
4. 上传 APK 和素材
5. 提交审核

### 阶段四：CI/CD 配置（1-2 小时）
1. 创建 GitHub Actions 工作流
2. 配置仓库 Secrets
3. 测试自动构建
4. 验证自动发布

---

## 七、相关文件

| 文件 | 位置 | 状态 |
|------|------|------|
| Debug APK | `app/build/outputs/apk/debug/app-debug.apk` | ✅ 已生成 |
| Release APK | `app/build/outputs/apk/release/app-release-unsigned.apk` | ⏳ 待签名 |
| 构建配置 | `app/build.gradle.kts` | ✅ 已配置 |
| 发布说明 | `RELEASE_NOTES_v0.5.0-alpha.md` | ✅ 已准备 |
| 酷安材料 | `COOLAPK_MATERIALS.md` | ✅ 已准备 |
| 隐私政策 | `PRIVACY_POLICY.md` | ✅ 已准备 |
| 待办事项 | `RELEASE_TODO.md` | ✅ 已创建 |

---

## 八、建议与风险提示

### ⚠️ 风险提示

1. **签名密钥安全**
   - 密钥库一旦丢失无法恢复
   - 建议备份到安全位置
   - 不要提交到版本控制

2. **酷安审核**
   - Alpha 版本可能审核更严格
   - 需明确标注测试版本
   - 准备好应对审核反馈

3. **版本管理**
   - 当前版本代码为 1
   - 后续更新需递增 versionCode
   - versionName 可自由定义

### 💡 优化建议

1. **APK 大小优化**
   - 当前 Release APK 41MB
   - 建议启用 R8 代码压缩
   - 考虑使用 Android App Bundle (AAB)

2. **分发渠道**
   - GitHub：开发者和技术用户
   - 酷安：国内 Android 用户
   - 可考虑 Google Play（需 25 美元注册费）

3. **自动化**
   - 配置 GitHub Actions 自动构建
   - 使用 Fastlane 自动化发布流程
   - 配置自动测试和代码检查

---

**报告编制：** 工部  
**审核：** 都察院  
**批准：** 内阁  

---

**下次更新：** 完成签名配置后
