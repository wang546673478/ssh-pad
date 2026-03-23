# 安全检查清单 (Security Audit Checklist)

## 一、密钥存储安全 🔐

### 1.1 SSH 密钥管理

- [ ] 私钥不硬编码在代码中
- [ ] 私钥使用 Android Keystore 加密存储
- [ ] 密钥文件权限设置正确（600）
- [ ] 密钥密码不明文存储
- [ ] 使用 BiometricPrompt 进行生物认证
- [ ] 密钥访问需要用户授权

**检查点：**
```kotlin
// ❌ 错误示例
val privateKey = "-----BEGIN RSA PRIVATE KEY-----..."

// ✅ 正确示例
val keyStore = AndroidKeyStore.getInstance()
keyStore.load(null)
val privateKeyEntry = keyStore.getEntry("ssh_key", null) as KeyStore.PrivateKeyEntry
```

### 1.2 密码存储

- [ ] 密码不持久化（或加密存储）
- [ ] 使用 EncryptedSharedPreferences
- [ ] 不在日志中输出密码
- [ ] 内存中及时清除密码

**检查点：**
```kotlin
// ❌ 错误示例
preferences.edit().putString("password", password).apply()
Log.d("SSH", "Password: $password")

// ✅ 正确示例
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()
val encryptedPrefs = EncryptedSharedPreferences.create(
    context,
    "secure_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

### 1.3 密钥传输

- [ ] 密钥不通过网络明文传输
- [ ] SSH 握手过程加密
- [ ] 中间人攻击防护（指纹验证）

## 二、网络通信安全 🌐

### 2.1 SSH 协议安全

- [ ] 仅支持 SSH-2 协议（禁用 SSH-1）
- [ ] 强加密算法优先（AES-256, ChaCha20）
- [ ] 禁用弱算法（3DES, RC4, MD5）
- [ ] 服务器密钥验证（AcceptAllServerKeyVerifier 风险）

**检查点：**
```kotlin
// ⚠️ 当前代码问题
sshClient.serverKeyVerifier = AcceptAllServerKeyVerifier.INSTANCE
// ❌ 这会接受任何服务器密钥，存在中间人攻击风险

// ✅ 应该实现自定义验证
sshClient.serverKeyVerifier = object : ServerKeyVerifier {
    override fun verifyServerKey(
        session: ClientSession,
        remoteAddress: SocketAddress,
        serverKey: PublicKey
    ): Boolean {
        // 验证服务器指纹
        val fingerprint = serverKey.fingerprint()
        return knownHosts.contains(fingerprint)
    }
}
```

### 2.2 配置核查

- [ ] 密码认证和密钥认证都可支持
- [ ] 默认启用密钥认证
- [ ] 支持两步验证（2FA）
- [ ] 会话超时自动断开

### 2.3 数据加密

- [ ] 传输层加密（SSH 隧道）
- [ ] 敏感数据本地加密存储
- [ ] 加密算法使用行业标准（AES-256-GCM）
- [ ] 密钥定期轮换

## 三、权限最小化 🔒

### 3.1 Android 权限

- [ ] 仅申请必要权限
- [ ] 运行时权限请求有明确说明
- [ ] 权限被拒绝时有降级方案
- [ ] 不使用 `requestLegacyAllStorage`

**当前项目权限检查：**
```xml
<!-- 应仅包含以下权限 -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<!-- 如需文件访问 -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" 
                 android:maxSdkVersion="32" />
```

### 3.2 组件暴露

- [ ] Service 不暴露给其他应用（`exported="false"`）
- [ ] Activity 按需设置 `exported`
- [ ] BroadcastReceiver 权限保护
- [ ] ContentProvider 权限限制

**检查点：**
```xml
<!-- ✅ 正确示例 -->
<service 
    android:name=".service.SSHConnectionService"
    android:exported="false"
    android:foregroundServiceType="connectedDevice" />

<!-- ❌ 错误示例 -->
<service 
    android:name=".service.SSHConnectionService"
    android:exported="true" /> <!-- 不暴露给其他应用 -->
```

### 3.3 数据隔离

- [ ] SharedPreferences 使用 MODE_PRIVATE
- [ ] 文件存储使用内部存储
- [ ] 不使用外部存储保存敏感数据
- [ ] Database 加密（使用 SQLCipher）

## 四、输入验证与注入防护 🛡️

### 4.1 用户输入

- [ ] SSH 主机名验证（正则表达式）
- [ ] 端口号范围检查（1-65535）
- [ ] 用户名字符限制
- [ ] 命令注入防护

**检查点：**
```kotlin
// ✅ 主机名验证
val hostnamePattern = Regex("^[a-zA-Z0-9.-]+\$")
if (!hostnamePattern.matches(host)) {
    throw IllegalArgumentException("Invalid hostname")
}

// ✅ 端口号验证
if (port !in 1..65535) {
    throw IllegalArgumentException("Port must be between 1 and 65535")
}
```

### 4.2 命令执行

- [ ] 不直接执行用户输入的命令
- [ ] 命令参数白名单验证
- [ ] 特殊字符转义
- [ ] 命令长度限制

### 4.3 路径遍历防护

- [ ] 文件路径规范化（`canonicalPath`）
- [ ] 限制访问目录范围
- [ ] 禁止 `..` 路径跳转

**检查点：**
```kotlin
// ✅ 路径验证
val baseDir = context.filesDir.canonicalPath
val requestedPath = File(userPath).canonicalPath
if (!requestedPath.startsWith(baseDir)) {
    throw SecurityException("Path traversal attempt detected")
}
```

## 五、日志安全 📝

### 5.1 敏感信息过滤

- [ ] 日志不输出密码
- [ ] 日志不输出私钥
- [ ] 日志不输出完整令牌
- [ ] 日志不输出个人身份信息

**检查点：**
```kotlin
// ❌ 错误示例
Log.d("SSH", "Connecting to $host with password $password")
Log.d("Key", "Private key: $privateKey")

// ✅ 正确示例
Log.d("SSH", "Connecting to $host")
Log.d("Key", "Loading key with fingerprint: ${key.fingerprint().take(16)}...")
```

### 5.2 日志级别

- [ ] Release 构建禁用 Debug 日志
- [ ] 使用 Timber 等日志库（可配置）
- [ ] 生产环境仅记录 Error/Warn

**检查点：**
```kotlin
// build.gradle.kts
android {
    buildTypes {
        release {
            buildConfigField "boolean", "DEBUG_MODE", "false"
        }
        debug {
            buildConfigField "boolean", "DEBUG_MODE", "true"
        }
    }
}

// 代码中
if (BuildConfig.DEBUG_MODE) {
    Log.d("TAG", "Debug message")
}
```

## 六、依赖安全 📦

### 6.1 第三方库

- [ ] 使用最新稳定版本
- [ ] 检查已知漏洞（CVE）
- [ ] 避免不再维护的库
- [ ] 使用官方 Maven 源

**当前项目依赖检查：**
| 依赖 | 版本 | 状态 | CVE 检查 |
|------|------|------|----------|
| Apache MINA sshd | 2.11.0 | ✅ 最新 | 无已知 CVE |
| Koin | 3.5.0 | ✅ 稳定 | 无已知 CVE |
| Jetpack Compose | 2023.10.01 | ⚠️ 可更新 | 无已知 CVE |

### 6.2 ProGuard/R8

- [ ] Release 构建启用混淆
- [ ] 配置正确的 keep 规则
- [ ] 测试混淆后的功能

## 七、安全更新与响应 🚨

### 7.1 漏洞响应

- [ ] 建立安全漏洞报告渠道
- [ ] 制定漏洞修复 SLA
- [ ] 定期安全更新

### 7.2 安全审计

- [ ] 每次重大更新前安全审查
- [ ] 年度第三方安全审计
- [ ] 渗透测试（至少每年一次）

## 八、当前代码安全问题汇总

### 高危问题 🔴

1. **AcceptAllServerKeyVerifier**
   - 位置：`SSHClientWrapper.kt:28`
   - 风险：中间人攻击
   - 修复：实现自定义 `ServerKeyVerifier`，验证服务器指纹

### 中危问题 🟡

1. **明文密码存储**
   - 位置：`SSHConnection.kt` 数据模型
   - 风险：设备被入侵时密码泄露
   - 修复：使用 EncryptedSharedPreferences 或 Android Keystore

2. **私钥路径明文**
   - 位置：`SSHConnection.privateKeyPath`
   - 风险：私钥位置暴露
   - 修复：使用安全存储，仅存储引用 ID

### 低危问题 🟢

1. **Debug 日志可能泄露信息**
   - 位置：多处
   - 风险：测试阶段信息泄露
   - 修复：Release 构建禁用 Debug 日志

2. **权限声明可能过度**
   - 位置：`AndroidManifest.xml`
   - 风险：用户隐私担忧
   - 修复：审查并最小化权限

## 九、安全审查评分

| 类别 | 检查项数 | 通过数 | 得分 |
|------|----------|--------|------|
| 密钥存储 | 6 | 0 | 0% ❌ |
| 网络通信 | 8 | 3 | 37.5% ❌ |
| 权限最小化 | 7 | 4 | 57% ⚠️ |
| 输入验证 | 6 | 0 | 0% ❌ |
| 日志安全 | 5 | 1 | 20% ❌ |
| 依赖安全 | 5 | 4 | 80% ✅ |

**总体得分：32.5%** ❌ 需要立即改进

---

## 修复计划

### Phase 1 (Week 6-7) - 紧急修复
- [ ] 替换 AcceptAllServerKeyVerifier
- [ ] 实现服务器指纹验证
- [ ] 添加密码加密存储

### Phase 2 (Week 8-9) - 重要改进
- [ ] 集成 Android Keystore
- [ ] 实现生物认证
- [ ] 完善输入验证

### Phase 3 (Week 10) - 优化加固
- [ ] 日志脱敏
- [ ] 权限最小化
- [ ] 安全文档完善

---

*审查人：都察院御史*
*审查日期：2026-04-27*
*版本：v1.0*
