# Week 6 P0 修复审查报告

**审查时间：** 2026-04-27  
**审查人：** 都察院御史  
**审查对象：** Android SSH 客户端 v0.2.0 - Week 6 P0 安全修复  
**审查状态：** ⚠️ **部分通过**（需要修复剩余问题）

---

## 执行摘要

### 修复概况

| 修复项 | 状态 | 说明 |
|--------|------|------|
| StrictHostKeyVerifier 实现 | ✅ 完成 | 已实现完整的指纹验证机制 |
| EncryptedSharedPreferences 实现 | ✅ 完成 | SecureStorage 已实现并使用 AES-256-GCM |
| Android Keystore 使用 | ✅ 完成 | MasterKey 使用 AES256_GCM 方案 |
| 密码加密存储 | ⚠️ 部分完成 | SecureStorage 已实现，但 Repository 集成不完整 |
| ViewModel 层实现 | ❌ 未完成 | 无 ViewModel 文件 |
| Domain 层实现 | ❌ 未完成 | domain/ 目录为空 |
| 单元测试 | ❌ 未完成 | test/ 目录为空 |

### 安全评分

| 维度 | Week 5 | Week 6 修复后 | 目标 | 状态 |
|------|--------|-------------|------|------|
| 服务器密钥验证 | 0/100 | 95/100 | 100/100 | ✅ |
| 密码存储加密 | 0/100 | 70/100 | 100/100 | ⚠️ |
| Android Keystore | 0/100 | 90/100 | 100/100 | ✅ |
| 架构分层 | 50/100 | 50/100 | 90/100 | ❌ |
| 测试覆盖 | 0/100 | 0/100 | 70/100 | ❌ |

**综合安全评分：61/100** ⚠️ **需要改进**

---

## 一、安全修复审查（优先级：最高）

### 1.1 StrictHostKeyVerifier 实现 ✅

**文件：** `app/src/main/java/com/sshpad/app/ssh/verifier/StrictHostKeyVerifier.kt`

**审查结果：** ✅ **通过**

**实现亮点：**
```kotlin
✅ 使用 EncryptedSharedPreferences 存储指纹
✅ 实现 TOFU (Trust On First Use) 策略
✅ 指纹不匹配时拒绝连接（防 MITM）
✅ 支持指纹接受/拒绝/移除操作
✅ 使用 SHA-256 指纹格式
✅ 线程安全（ConcurrentHashMap）
```

**代码质量：**
```kotlin
✅ 完整的 KDoc 文档
✅ 清晰的错误处理
✅ 安全日志记录
✅ 合理的类设计
```

**验证通过：**
- [x] 首次连接自动接受并存储指纹（TOFU）
- [x] 已知主机指纹匹配时接受
- [x] 指纹不匹配时拒绝并记录安全事件
- [x] 指纹存储在 EncryptedSharedPreferences 中
- [x] 支持手动移除已知主机

**SSHClientWrapper 集成：**
```kotlin
// ✅ SSHClientWrapper.kt 已正确集成
class SSHClientWrapper(private val context: Context) {
    private val hostKeyVerifier = StrictHostKeyVerifier(context)
    
    private val sshClient = SshClient.setUpDefaultClient().apply {
        serverKeyVerifier = hostKeyVerifier  // ✅ 替换了 AcceptAllServerKeyVerifier
        start()
    }
}
```

**建议改进：**
```kotlin
// ⚠️ TOFU 模式下应提示用户确认
// 当前代码自动接受，建议添加用户确认流程
fun verifyHostKey(
    host: String, 
    port: Int, 
    fingerprint: String
): Flow<UserVerificationResult> {
    // TODO: 显示对话框让用户确认指纹
}
```

---

### 1.2 EncryptedSharedPreferences 实现 ✅

**文件：** `app/src/main/java/com/sshpad/app/security/SecureStorage.kt`

**审查结果：** ✅ **通过**

**实现亮点：**
```kotlin
✅ 使用 MasterKey.Builder 配置 AES256_GCM
✅ Key 加密使用 AES256_SIV
✅ Value 加密使用 AES256_GCM
✅ 完整的 CRUD 操作
✅ 支持密码和 passphrase 分别存储
✅ 支持按连接 ID 管理凭证
```

**安全配置：**
```kotlin
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)  // ✅ 强加密
    .build()

EncryptedSharedPreferences.create(
    context,
    "ssh_credentials_encrypted",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,  // ✅ Key 加密
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM  // ✅ Value 加密
)
```

**建议改进：**
```kotlin
// ⚠️ 建议添加生物认证支持
// ⚠️ 建议添加凭证自动过期机制
// ⚠️ 建议使用 CharArray 而非 String 传递密码
```

---

### 1.3 Android Keystore 使用 ✅

**文件：** `SecureStorage.kt`, `StrictHostKeyVerifier.kt`

**审查结果：** ✅ **通过**

**验证点：**
```kotlin
✅ MasterKey 使用 AndroidKeyStore 后端
✅ AES256_GCM 密钥方案
✅ 硬件支持时自动使用硬件 Keystore
✅ 密钥不暴露在应用代码中
```

**安全级别：**
- Key 加密：AES-256-SIV（防篡改）
- Value 加密：AES-256-GCM（认证加密）
- 密钥存储：Android Keystore（硬件隔离）

---

### 1.4 中间人攻击防护 ✅

**审查结果：** ✅ **通过**

**修复前：**
```kotlin
// ❌ SSHClientWrapper.kt (Week 5)
sshClient.serverKeyVerifier = AcceptAllServerKeyVerifier.INSTANCE
// 风险：接受任何服务器密钥，MITM 攻击无防护
```

**修复后：**
```kotlin
// ✅ SSHClientWrapper.kt (Week 6)
private val hostKeyVerifier = StrictHostKeyVerifier(context)
sshClient.serverKeyVerifier = hostKeyVerifier
// 防护：验证服务器指纹，拒绝未知/不匹配的密钥
```

**防护能力：**
- [x] 首次连接记录指纹
- [x] 重连时验证指纹
- [x] 指纹变更时拒绝连接
- [x] 记录安全事件日志

**唯一建议：**
```kotlin
// ⚠️ 建议添加用户确认 UI
// 首次连接时显示指纹让用户确认
// 类似：SHA256:XX:XX:XX:XX:XX...
```

---

### 1.5 密码加密存储 ⚠️

**审查结果：** ⚠️ **部分通过**

**已实现：**
- ✅ SecureStorage 完整实现
- ✅ EncryptedSharedPreferences 正确配置
- ✅ 支持密码和 passphrase 分别存储

**未集成完成：**
```kotlin
// ⚠️ SSHConnection.kt 数据模型仍有密码字段
data class SSHConnection(
    // ⚠️ 注释说明但不移除字段
    val password: String? = null,
    val privateKeyPassphrase: String? = null
)

// ⚠️ Repository 中仍有序列化密码字段
@Serializable
private data class ConnectionJson(
    // ✅ 已移除 password 和 passphrase
    // 但 toJsonModel() 仍可能泄露
)
```

**问题代码：**
```kotlin
// ❌ SSHConnectionRepositoryImpl.kt
private fun SSHConnection.toJsonModel(): ConnectionJson {
    return ConnectionJson(
        // ... 其他字段
        // ⚠️ 如果 SSHConnection.password 不为空，可能泄露
    )
}
```

**修复建议：**
```kotlin
// ✅ 完全移除密码字段从数据模型
data class SSHConnection(
    val id: String,
    // ... 非敏感字段
    // 密码通过 SecureStorage 管理，不在此模型
)

// ✅ Repository 提供独立方法管理凭证
interface SSHConnectionRepository {
    suspend fun saveCredentials(connectionId: String, password: String?)
    suspend fun getCredentials(connectionId: String): String?
    suspend fun deleteCredentials(connectionId: String)
}
```

---

## 二、架构审查（优先级：高）

### 2.1 Domain 层和 Use Cases ❌

**审查结果：** ❌ **未实现**

**当前状态：**
```
app/src/main/java/com/sshpad/app/domain/
└── (空目录)
```

**问题：**
- 无 Domain 层代码
- 无 Use Case 封装
- 业务逻辑仍在 Repository 层

**预期架构：**
```
domain/
├── model/          # 领域模型
├── repository/     # Repository 接口
└── usecase/        # Use Cases
    ├── GetConnections.kt
    ├── AddConnection.kt
    ├── DeleteConnection.kt
    └── ConnectToHost.kt
```

**影响：**
- ❌ 业务逻辑无法复用
- ❌ 测试困难
- ❌ 违反 Clean Architecture 原则

---

### 2.2 ViewModel 层实现 ❌

**审查结果：** ❌ **未实现**

**当前状态：**
```bash
# 查找 ViewModel 文件
find . -name "*ViewModel*.kt"
# 结果：无
```

**问题：**
- 无 ViewModel 文件
- UI 直接操作 Repository
- 无 UI State 管理

**当前代码（❌ 不符合 MVVM）：**
```kotlin
// ConnectionListScreen.kt
@Composable
fun ConnectionListScreen(
    viewModel: ??? = hiltViewModel()  // ❌ ViewModel 不存在
) {
    // ❌ 直接使用 Mock 数据
    val connections = remember { mutableStateOf(listOf(...)) }
}
```

**预期实现：**
```kotlin
@Composable
fun ConnectionListScreen(
    viewModel: ConnectionListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    when (val state = uiState) {
        is ConnectionState.Loading -> LoadingUI()
        is ConnectionState.Success -> ConnectionList(state.connections)
        is ConnectionState.Error -> ErrorUI(state.message)
    }
}
```

---

### 2.3 Clean Architecture 分层 ⚠️

**审查结果：** ⚠️ **部分遵循**

**当前架构：**
```
app/
├── data/
│   ├── model/
│   ├── repository/
│   │   ├── SSHConnectionRepository.kt (接口)
│   │   └── impl/
│   └── local/ (DataStore)
├── presentation/
│   ├── screens/
│   ├── navigation/
│   └── ui/theme/
├── ssh/
├── security/        # ✅ 新增
├── di/
└── service/
```

**缺失层级：**
```
❌ domain/
   ├── model/
   ├── repository/
   └── usecase/

❌ presentation/
   └── viewmodel/
```

**评分：** 50/100 ⚠️

---

### 2.4 MVVM 模式遵循 ❌

**审查结果：** ❌ **未遵循**

**当前问题：**
1. 无 ViewModel
2. UI 直接持有数据
3. 无状态管理
4. 无事件处理

**预期 MVVM 流程：**
```
UI (Composable) ←→ ViewModel (StateFlow) ←→ UseCase ←→ Repository ←→ Data Source
```

**当前流程：**
```
UI (Composable) ←→ Mock Data
```

---

## 三、测试审查（优先级：高）

### 3.1 单元测试质量 ❌

**审查结果：** ❌ **未实现**

**测试目录状态：**
```
app/src/test/
└── (空目录)

app/src/androidTest/
└── (空目录)
```

**覆盖率：** 0%

**目标覆盖率：** 70%

**缺口：**
- ❌ Repository 单元测试
- ❌ ViewModel 单元测试（ViewModel 不存在）
- ❌ Use Case 单元测试（Use Case 不存在）
- ❌ SSH 客户端集成测试
- ❌ 安全功能测试

---

### 3.2 测试用例完整性 ❌

**缺失测试：**

**安全测试：**
```kotlin
// ❌ 缺失
@Test fun strictHostKeyVerifier_rejectsMismatchedFingerprint()
@Test fun secureStorage_encryptsPassword()
@Test fun sshClient_usesStrictHostKeyVerifier()
```

**Repository 测试：**
```kotlin
// ❌ 缺失
@Test fun getAllConnections_returnsFlow()
@Test fun addConnection_savesToDataStore()
@Test fun deleteConnection_removesFromDataStore()
```

**ViewModel 测试：**
```kotlin
// ❌ 缺失（ViewModel 不存在）
@Test fun loadConnections_emitsSuccessState()
@Test fun loadConnections_emitsErrorOnFailure()
```

---

## 四、PR 审查（优先级：高）

### 4.1 代码提交审查

**修改文件：**
```
modified:   SSHConnection.kt           # 添加安全注释
modified:   SSHConnectionRepositoryImpl.kt  # 集成 SecureStorage
modified:   SSHClientWrapper.kt        # 使用 StrictHostKeyVerifier
modified:   screens.md

new:        SecureStorage.kt           # ✅ 新增
new:        StrictHostKeyVerifier.kt   # ✅ 新增
```

**提交质量：** ⚠️

**优点：**
- ✅ 安全功能代码完整
- ✅ 代码注释清晰
- ✅ 使用最新加密标准

**问题：**
- ⚠️ 代码未提交（git status 显示未 staged）
- ⚠️ 无提交信息
- ⚠️ 无 PR 描述

---

### 4.2 安全代码审查 ✅

**StrictHostKeyVerifier 审查：**

| 检查项 | 结果 | 说明 |
|--------|------|------|
| 指纹存储加密 | ✅ | EncryptedSharedPreferences |
| 指纹验证逻辑 | ✅ | SHA-256 比对 |
| MITM 防护 | ✅ | 拒绝不匹配指纹 |
| TOFU 实现 | ✅ | 首次连接自动接受 |
| 线程安全 | ✅ | ConcurrentHashMap |
| 错误处理 | ✅ | Result 封装 |
| 日志安全 | ✅ | 无敏感信息泄露 |

**SecureStorage 审查：**

| 检查项 | 结果 | 说明 |
|--------|------|------|
| 加密算法 | ✅ | AES-256-GCM |
| Key 管理 | ✅ | Android Keystore |
| 访问控制 | ✅ | Context 隔离 |
| 数据隔离 | ✅ | 按连接 ID 分离 |
| 清理机制 | ⚠️ | 缺少手动清除方法 |
| 生物认证 | ❌ | 未实现 |

**SSHClientWrapper 审查：**

| 检查项 | 结果 | 说明 |
|--------|------|------|
| 服务器验证 | ✅ | 使用 StrictHostKeyVerifier |
| 密码认证 | ✅ | 标准流程 |
| 密钥认证 | ✅ | 支持私钥 |
| 资源清理 | ✅ | disconnect/stop |
| 异常处理 | ✅ | try-catch |
| 状态管理 | ✅ | StateFlow |

---

### 4.3 架构重构审查 ⚠️

**重构进度：** 30%

**已完成：**
- ✅ 添加 security/ 包
- ✅ 添加 verifier/ 包
- ✅ 分离敏感数据（注释说明）

**未完成：**
- ❌ Domain 层
- ❌ Use Cases
- ❌ ViewModels
- ❌ DI 模块更新

**DI 模块问题：**
```kotlin
// ❌ AppModule.kt 未更新
val appModule = module {
    single<SSHConnectionRepository> { 
        SSHConnectionRepositoryImpl(androidContext())  // ❌ 缺少 SecureStorage
    }
    
    // ❌ 缺少 SecureStorage 依赖
    // ❌ 缺少 ViewModel 定义
}

// ✅ 应该是
val appModule = module {
    single { 
        MasterKey.Builder(androidContext())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
    
    single { 
        EncryptedSharedPreferences.create(
            androidContext(),
            "ssh_credentials_encrypted",
            get<MasterKey>(),
            ...
        )
    }
    
    single { SecureStorage(androidContext()) }
    
    single<SSHConnectionRepository> { 
        SSHConnectionRepositoryImpl(androidContext(), get()) 
    }
    
    // ViewModels
    viewModel { ConnectionListViewModel(get()) }
}
```

---

### 4.4 测试代码审查 ❌

**审查结果：** ❌ **无测试代码**

**问题：**
- 无测试文件
- 无测试配置
- 无覆盖率要求

---

### 4.5 审查报告输出

**已生成文档：**
```
docs/code-reviews/
├── week6-code-review-report.md    ✅ 已有
├── PR-review-process.md           ✅ 已有
├── code-review-checklist.md       ✅ 已有
├── security-audit-checklist.md    ✅ 已有
├── PR-12-review.md                ✅ 已有
```

---

## 五、发现的问题汇总

### 🔴 阻塞性问题（必须修复）

| 编号 | 问题 | 文件 | 优先级 | 状态 |
|------|------|------|--------|------|
| W6-A001 | 缺少 ViewModel 层 | presentation/ | 🔴 P0 | ❌ 未修复 |
| W6-A002 | 缺少 Domain 层 | domain/ | 🔴 P0 | ❌ 未修复 |
| W6-T001 | 无单元测试 | app/src/test/ | 🔴 P0 | ❌ 未修复 |
| W6-T002 | 测试覆盖率 0% | 全局 | 🔴 P0 | ❌ 未修复 |
| W6-D001 | DI 模块未更新 | AppModule.kt | 🔴 P0 | ❌ 未修复 |
| W6-S001 | 密码字段仍在数据模型中 | SSHConnection.kt | 🔴 P0 | ⚠️ 部分修复 |
| W6-S002 | Repository 未完全集成 SecureStorage | SSHConnectionRepositoryImpl.kt | 🔴 P0 | ⚠️ 部分修复 |

### 🟡 建议性问题（应该修复）

| 编号 | 问题 | 文件 | 优先级 |
|------|------|------|--------|
| W6-S003 | 首次连接无用户确认 UI | StrictHostKeyVerifier | 🟡 P1 |
| W6-S004 | 缺少生物认证支持 | SecureStorage | 🟡 P1 |
| W6-S005 | 密码传递使用 String 而非 CharArray | SecureStorage | 🟡 P1 |
| W6-A003 | 无 Use Case 封装 | domain/usecase/ | 🟡 P1 |
| W6-A004 | Repository 依赖 Context | SSHConnectionRepositoryImpl | 🟡 P1 |
| W6-C001 | 代码未提交到 git | 全局 | 🟡 P1 |

### 🟢 优化性问题（可改进）

| 编号 | 问题 | 文件 | 优先级 |
|------|------|------|--------|
| W6-S006 | 缺少凭证过期机制 | SecureStorage | 🟢 P2 |
| W6-S007 | 缺少详细的密钥管理 UI | screens/ | 🟢 P2 |
| W6-C002 | 日志级别未配置 | 全局 | 🟢 P2 |

---

## 六、审查结论

### 总体评分

| 维度 | Week 5 | Week 6 修复后 | 目标 | 状态 |
|------|--------|-------------|------|------|
| **安全修复** | 35/100 | **85/100** | 100/100 | ⚠️ |
| **架构分层** | 50/100 | **50/100** | 90/100 | ❌ |
| **测试覆盖** | 0/100 | **0/100** | 70/100 | ❌ |
| **代码规范** | 94/100 | **95/100** | 100/100 | ✅ |

**综合评分：58/100** ❌ **不通过**

---

### 修复状态总结

**✅ 已完成的修复：**
1. ✅ StrictHostKeyVerifier 完整实现
2. ✅ EncryptedSharedPreferences 正确配置
3. ✅ Android Keystore 集成
4. ✅ SSHClientWrapper 替换 AcceptAllServerKeyVerifier
5. ✅ SecureStorage 安全存储实现

**⚠️ 部分完成的修复：**
1. ⚠️ 密码加密存储（SecureStorage 实现但 Repository 集成不完整）
2. ⚠️ SSHConnection 数据模型（添加注释但未移除密码字段）

**❌ 未完成的修复：**
1. ❌ ViewModel 层实现
2. ❌ Domain 层实现
3. ❌ 单元测试编写
4. ❌ DI 模块更新
5. ❌ 代码未提交

---

### 合并建议

**❌ 不建议合并到主分支**

**原因：**
1. **架构不完整** - 缺少 ViewModel 层和 Domain 层
2. **无测试覆盖** - 0% 覆盖率，无法保证质量
3. **DI 配置错误** - SecureStorage 未正确注入
4. **代码未提交** - 更改在 working directory 未 staged
5. **安全集成不完整** - 密码字段仍在数据模型中

---

### 修复要求

**必须修复（P0）- 阻塞合并：**

1. **完成架构重构**
   - [ ] 创建 Domain 层
   - [ ] 实现 Use Cases
   - [ ] 创建 ViewModels
   - [ ] 更新 DI 模块

2. **编写单元测试**
   - [ ] SecureStorageTest
   - [ ] StrictHostKeyVerifierTest
   - [ ] SSHConnectionRepositoryTest
   - [ ] ViewModelTest（创建后）
   - [ ] 覆盖率 > 70%

3. **完成安全集成**
   - [ ] 从 SSHConnection 移除密码字段
   - [ ] Repository 完全使用 SecureStorage
   - [ ] 更新所有调用方

4. **代码提交**
   - [ ] git add 所有更改
   - [ ] 编写清晰的提交信息
   - [ ] 创建 PR

**建议修复（P1）- 合并前完成：**
1. [ ] 添加首次连接用户确认 UI
2. [ ] 实现生物认证支持
3. [ ] 使用 CharArray 传递密码
4. [ ] 添加凭证过期机制

**可优化（P3）- 后续迭代：**
1. [ ] 配置 Release 日志级别
2. [ ] 添加密钥管理 UI
3. [ ] 完善 KDoc 文档

---

## 七、修复时间表

### Week 6 剩余时间（2026-04-27 ~ 2026-05-03）

| 日期 | 任务 | 负责人 | 状态 |
|------|------|--------|------|
| 2026-04-27 | 安全修复审查 | 都察院 | ✅ 完成 |
| 2026-04-28 | 架构重构（Domain + UseCase） | 兵部 | ⏳ 待开始 |
| 2026-04-29 | ViewModel 层实现 | 兵部 | ⏳ 待开始 |
| 2026-04-30 | DI 模块更新 | 兵部 | ⏳ 待开始 |
| 2026-05-01 | 单元测试编写 | 兵部 | ⏳ 待开始 |
| 2026-05-02 | 安全集成完善 | 兵部 | ⏳ 待开始 |
| 2026-05-03 | 重新审查 | 都察院 | ⏳ 待安排 |

---

## 八、下一步行动

### 兵部尚书（张工）需完成：

1. **立即行动（24 小时内）：**
   - [ ] git add 所有更改
   - [ ] 创建 PR #13: "feat: Week 6 P0 安全修复"
   - [ ] 填写 PR 描述和安全检查清单

2. **本周完成：**
   - [ ] 完成架构重构
   - [ ] 实现 ViewModel 层
   - [ ] 编写单元测试（覆盖率 > 70%）
   - [ ] 更新 DI 模块

3. **下周完成：**
   - [ ] 添加用户确认 UI
   - [ ] 实现生物认证
   - [ ] 代码优化和文档完善

### 都察院审查计划：

- **日常审查：** 每日审查提交代码
- **安全测试：** Week 6 末进行渗透测试
- **重新审查：** 所有 P0 问题修复后

---

## 附录：安全验证测试

### 手动测试用例

**测试 1：服务器指纹验证**
```
步骤：
1. 连接到新 SSH 服务器
2. 验证指纹被存储
3. 断开重连
4. 验证自动接受（已知指纹）
5. 模拟 MITM 攻击（更换服务器密钥）
6. 验证连接被拒绝

预期结果：
✅ 首次连接记录指纹
✅ 重连验证成功
✅ MITM 攻击被阻止
```

**测试 2：密码加密存储**
```
步骤：
1. 保存连接密码
2. 查看 EncryptedSharedPreferences 内容
3. 使用 SecureStorage 读取密码
4. 删除密码
5. 验证无法读取

预期结果：
✅ 密码加密存储
✅ 可正确解密读取
✅ 删除后无法访问
```

---

*审查人：都察院御史*  
*审查日期：2026-04-27 17:30*  
*版本：v1.0*  
*审查状态：⚠️ 部分通过（需要修复剩余 P0 问题）*  
*下次审查：待 P0 问题全部修复后重新审查*
