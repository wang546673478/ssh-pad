# SSH-Pad 代码审查报告

**审查日期**: 2026-03-24  
**审查范围**: 全部源代码文件 (34 个 Kotlin/KTS 文件)  
**审查执行**: 都察院

---

## 📊 审查摘要

| 审查类别 | 问题数 | 严重程度 |
|---------|--------|---------|
| 🔴 安全审查 (P0) | 2 | 高 |
| 📐 架构审查 (P1) | 3 | 中 |
| ✅ 代码质量 (P2) | 4 | 低 |
| 🧪 测试覆盖 (P2) | 3 | 中 |

---

## 🔴 安全审查 (P0)

### ✅ SecureStorage 加密存储 - 通过

**审查结果**: 符合安全标准

**亮点**:
- ✅ 使用 Android Keystore + EncryptedSharedPreferences
- ✅ AES-256-GCM 加密 values，AES-256-SIV 加密 keys
- ✅ 支持硬件级安全（当可用时）
- ✅ 密钥别名格式正确 (`password:$connectionId`)
- ✅ 提供完整的 CRUD 操作（save/get/delete/clear）

**代码示例** (`SecureStorage.kt:32-41`):
```kotlin
private val sharedPreferences: SharedPreferences by lazy {
    EncryptedSharedPreferences.create(
        context,
        "ssh_credentials_encrypted",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}
```

---

### ⚠️ StrictHostKeyVerifier 主机密钥验证 - 存在风险

**审查结果**: TOFU 流程需要改进

**问题 P0-1**: 首次连接时自动接受服务器密钥，未强制用户确认

**风险描述**:
在 `StrictHostKeyVerifier.kt:88-96` 中，首次连接 (TOFU) 时直接返回 `true` 接受服务器密钥，虽然代码注释提到 "In production, this should prompt the user"，但实际未实现用户确认流程。

```kotlin
// First time connecting - store and accept (TOFU)
storedFingerprint == null -> {
    pendingVerification[hostKey] = ServerFingerprint(...)
    // In production, this should prompt the user
    // For now, we'll auto-accept but mark as pending
    true  // ⚠️ 风险：自动接受
}
```

**修复建议**:
1. 在 `SSHClientWrapper.connect()` 中检测到新主机时，暂停连接流程
2. 通过 UI 向用户展示指纹信息并要求确认
3. 用户确认后调用 `acceptHostKey()` 再继续连接

**参考实现**:
```kotlin
// 在 SSHClientWrapper 中添加回调
suspend fun connect(
    connection: SSHConnection,
    password: String?,
    passphrase: String?,
    onHostKeyVerificationRequired: (ServerFingerprint) -> Boolean
): Result<ClientSession>
```

---

### ✅ SSHConnection 模型敏感字段分离 - 通过

**审查结果**: 符合安全最佳实践

**亮点**:
- ✅ `SSHConnection` 模型不包含 `password` 和 `privateKeyPassphrase` 字段
- ✅ 使用 `SSHConnectionWithCredentials` 作为临时传输对象
- ✅ 注释明确说明敏感字段存储位置

**代码示例** (`SSHConnection.kt:11-17`):
```kotlin
// Note: password is intentionally excluded from this model
// Store in SecureStorage using connection.id as key
val privateKeyPath: String? = null,
// Note: privateKeyPassphrase is intentionally excluded from this model
// Store in SecureStorage using connection.id as key
```

---

### ⚠️ 日志安全 - 需要加强

**审查结果**: 基本符合，但存在潜在风险

**问题 P0-2**: 缺少明确的日志安全策略，依赖开发者自觉

**现状**:
- 当前代码中未发现直接记录密码/密钥的日志语句
- 但未建立日志安全规范或工具类来防止意外泄露

**风险场景**:
```kotlin
// 未来可能的风险代码
android.util.Log.d("SSH", "Connecting with password: $password") // ⚠️ 危险
```

**修复建议**:
1. 创建 `SafeLogger` 工具类，自动过滤敏感信息
2. 在 Release 构建中禁用调试日志
3. 添加代码审查检查清单，防止敏感日志提交

**示例实现**:
```kotlin
object SafeLogger {
    private const val REDACTED = "*****"
    
    fun d(tag: String, message: String) {
        val sanitized = message
            .replace(Regex("password=[^&]*"), "password=$REDACTED")
            .replace(Regex("passphrase=[^&]*"), "passphrase=$REDACTED")
        android.util.Log.d(tag, sanitized)
    }
}
```

---

## 📐 架构审查 (P1)

### ✅ Clean Architecture 分层合规性 - 通过

**审查结果**: 架构分层清晰

**架构结构**:
```
📦 data/
  ├── model/         (数据模型)
  └── repository/    (数据层实现)
📦 domain/
  └── usecase/       (业务逻辑)
📦 presentation/
  ├── viewmodel/     (ViewModel)
  ├── screens/       (UI Composable)
  └── navigation/    (导航)
📦 service/          (后台服务)
📦 ssh/              (SSH 客户端封装)
📦 security/         (安全模块)
📦 di/               (依赖注入)
📦 util/             (工具类)
```

**优点**:
- ✅ 分层明确，职责清晰
- ✅ 数据流单向：UI → ViewModel → UseCase → Repository → DataStore
- ✅ Domain 层不依赖 Android 框架

---

### ⚠️ UseCase 单一职责 - 部分符合

**审查结果**: 基本符合，但 `ConnectToServerUseCase` 职责过重

**问题 P1-1**: `ConnectToServerUseCase` 同时处理连接和认证逻辑

**现状** (`ConnectToServerUseCase.kt:23-32`):
```kotlin
suspend operator fun invoke(connectionId: String): Result<Unit> {
    val connection = repository.getConnectionById(connectionId)
        ?: return Result.failure(Exception("Connection not found"))
    
    return sshClientWrapper.connect(connection)
        .map { /* Connection successful */ Unit }
}
```

**分析**:
- 该 UseCase 同时协调 Repository 和 SSHClientWrapper
- 严格来说，这违反了单一职责原则（应该只协调 Repository）
- 但在实际 CQRS 模式中，这种模式可接受

**修复建议** (可选):
```kotlin
// 方案 1: 拆分为两个 UseCase
class GetConnectionUseCase(private val repository: SSHConnectionRepository)
class EstablishSSHConnectionUseCase(private val sshClientWrapper: SSHClientWrapper)

// 方案 2: 保持现状，在代码中明确说明设计理由
```

---

### ✅ ViewModel 依赖 UseCase 而非 Repository - 通过

**审查结果**: 完全符合

**亮点** (`SSHConnectionViewModel.kt:19-24`):
```kotlin
class SSHConnectionViewModel(
    private val getSSHConnectionsUseCase: GetSSHConnectionsUseCase,
    private val createSSHConnectionUseCase: CreateSSHConnectionUseCase,
    private val deleteSSHConnectionUseCase: DeleteSSHConnectionUseCase,
    private val connectToServerUseCase: ConnectToServerUseCase
) : ViewModel()
```

**优点**:
- ✅ ViewModel 不直接依赖 Repository
- ✅ 所有操作通过 UseCase 封装
- ✅ 便于单元测试和复用

---

### ⚠️ 依赖注入配置 - 需要改进

**审查结果**: 基本正确，但缺少作用域管理

**问题 P1-2**: `SSHClientWrapper` 作为 singleton 可能导致状态混乱

**现状** (`AppModule.kt:31`):
```kotlin
single { SSHClientWrapper(androidContext()) }
```

**风险**:
- SSHClientWrapper 维护连接状态 (`currentSession`, `currentChannel`)
- 作为 singleton，多连接场景下状态可能冲突
- ViewModel 销毁时，连接状态未清理

**问题 P1-3**: SecureStorage 未明确生命周期管理

**修复建议**:
1. 为 `SSHClientWrapper` 添加 `scopedByViewModel()` 或改为工厂模式
2. 在 `SSHConnectionService` 中管理连接生命周期
3. 明确 `SecureStorage` 的清理时机（应用卸载时自动清理）

---

## ✅ 代码质量 (P2)

### ✅ Kotlin 代码规范 - 通过

**审查结果**: 代码风格一致，符合 Kotlin 规范

**亮点**:
- ✅ 命名规范：PascalCase (类), camelCase (方法/属性), UPPER_SNAKE_CASE (常量)
- ✅ 空安全：合理使用 `?`, `!!`, `let`, `also`
- ✅ 数据类：使用 `data class` 表示数据传输对象
- ✅ 密封类：使用 `sealed class ConnectionState` 表示状态

**示例** (`AppConstants.kt`):
```kotlin
object AppConstants {
    const val SSH_DEFAULT_PORT = 22
    const val SSH_CONNECT_TIMEOUT_MS = 30_000L
    const val SPACING_LARGE = 16
}
```

---

### ⚠️ 命名约定 - 小问题

**问题 P2-1**: 部分命名不够清晰

**示例**:
```kotlin
// 建议改进
val _uiState = MutableStateFlow(TerminalUiState())  // ✅ 好
val _outputBuffer = MutableStateFlow<String>("")    // ⚠️ 可改为 _terminalOutput
```

**问题 P2-2**: 魔法字符串未提取为常量

```kotlin
// TerminalScreen.kt
Text("SSH Pad Terminal v0.1.0")  // ⚠️ 应提取为常量
Text("Welcome to Ubuntu 22.04.3 LTS...")  // ⚠️ 硬编码
```

**修复建议**:
```kotlin
// 在 AppConstants 中添加
const val TERMINAL_WELCOME_TITLE = "SSH Pad Terminal v0.1.0"
const val TERMINAL_WELCOME_MESSAGE = "Welcome to Ubuntu..."
```

---

### ✅ 注释完整性 - 通过

**审查结果**: 注释充分，KDoc 规范

**亮点**:
- ✅ 所有公开 API 都有 KDoc 注释
- ✅ 包含 `@param`, `@return` 等标准标签
- ✅ 关键安全逻辑有详细说明

**示例** (`SecureStorage.kt:9-21`):
```kotlin
/**
 * Secure storage for sensitive SSH credentials using Android Keystore
 * 
 * Security features:
 * - Uses Android Keystore for key generation and storage
 * - AES-256-GCM encryption for values
 * - AES-256-SIV encryption for keys
 * - Hardware-backed security when available
 * - Automatic key rotation support
 * 
 * Usage:
 * ```
 * val secureStorage = SecureStorage(context)
 * secureStorage.savePassword(connectionId, "mySecretPassword")
 * ```
 */
```

---

### ⚠️ 代码复用 - 可改进

**问题 P2-3**: UI 代码存在重复

**示例**:
```kotlin
// ConnectionEditScreen.kt 和 TerminalScreen.kt 都有类似的 Scaffold 结构
// 可以提取为可复用的组件
```

**修复建议**:
```kotlin
// 提取通用的 AppScaffold 组件
@Composable
fun AppScaffold(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
)
```

---

## 🧪 测试覆盖 (P2)

### ⚠️ 当前测试文件检查 - 部分通过

**审查结果**: 测试结构完整，但覆盖率不足

**现有测试文件**:
```
✅ domain/usecase/ConnectToServerUseCaseTest.kt (8 个测试)
✅ domain/usecase/CreateSSHConnectionUseCaseTest.kt
✅ domain/usecase/DeleteSSHConnectionUseCaseTest.kt
✅ domain/usecase/GetSSHConnectionsUseCaseTest.kt
✅ security/SecureStorageTest.kt (8 个占位符测试)
✅ ssh/verifier/StrictHostKeyVerifierTest.kt (7 个测试)
```

**问题 P2-4**: SecureStorageTest 和 StrictHostKeyVerifierTest 大量占位符测试

**示例** (`SecureStorageTest.kt:36-44`):
```kotlin
@Test
fun `savePassword should store password securely`() {
    // Given
    val connectionId = "test-connection-1"
    val password = "testPassword123"

    // When
    // secureStorage.savePassword(connectionId, password)

    // Then
    // verify { secureStorage.getPassword(connectionId) == password }
    assertTrue(true) // Placeholder ⚠️
}
```

**原因分析**:
- SecureStorage 依赖 EncryptedSharedPreferences，需要 Android 环境
- 当前测试使用 MockK 但无法模拟 Android 框架

**修复建议**:
1. 添加 Android Instrumentation Tests (`androidTest/`) 测试实际加密功能
2. 使用 Robolectric 进行 JVM 上的 Android 框架模拟
3. 对可测试的逻辑部分编写纯单元测试

---

### 覆盖率评估

| 模块 | 测试覆盖率 (估计) | 状态 |
|------|------------------|------|
| UseCases | ~80% | ✅ 良好 |
| SecureStorage | ~10% (仅占位符) | ⚠️ 不足 |
| StrictHostKeyVerifier | ~30% | ⚠️ 不足 |
| Repository | ~0% | ❌ 缺失 |
| ViewModel | ~0% | ❌ 缺失 |
| SSHClientWrapper | ~0% | ❌ 缺失 |

**缺失测试识别**:

**必须添加的测试** (P2):
1. `SSHConnectionRepositoryImplTest` - 测试 DataStore 持久化
2. `SSHConnectionViewModelTest` - 测试 UI 状态管理
3. `TerminalViewModelTest` - 测试终端会话管理
4. `SSHClientWrapperTest` - 测试 SSH 连接逻辑（可 Mock）
5. `AppModuleTest` - 测试依赖注入配置

**建议添加的测试** (P3):
1. `SSHConnectionTest` - 测试数据模型方法
2. `AppConstantsTest` - 测试常量值正确性
3. `ConnectionCredentialsTest` - 测试凭据对象

---

## 📋 问题汇总

### P0 高优先级 (必须修复)

| 编号 | 问题 | 文件 | 建议 |
|------|------|------|------|
| P0-1 | TOFU 流程未强制用户确认 | StrictHostKeyVerifier.kt | 添加 UI 确认弹窗 |
| P0-2 | 缺少日志安全机制 | 全局 | 创建 SafeLogger 工具类 |

### P1 中优先级 (建议修复)

| 编号 | 问题 | 文件 | 建议 |
|------|------|------|------|
| P1-1 | ConnectToServerUseCase 职责过重 | ConnectToServerUseCase.kt | 拆分或说明设计理由 |
| P1-2 | SSHClientWrapper singleton 状态管理 | AppModule.kt | 改为 ViewModel 作用域 |
| P1-3 | SecureStorage 生命周期不明确 | SecureStorage.kt | 添加文档说明 |

### P2 低优先级 (可选改进)

| 编号 | 问题 | 文件 | 建议 |
|------|------|------|------|
| P2-1 | 部分命名不够清晰 | TerminalViewModel.kt | 改进变量命名 |
| P2-2 | 魔法字符串未提取 | TerminalScreen.kt | 提取为常量 |
| P2-3 | UI 代码重复 | 多个 Screen | 提取可复用组件 |
| P2-4 | 测试覆盖率不足 | SecureStorageTest 等 | 添加 Instrumentation Tests |

---

## 🎯 总体评价

### 优点

1. ✅ **安全意识强**: 使用 EncryptedSharedPreferences 存储敏感数据，SSHConnection 模型分离敏感字段
2. ✅ **架构清晰**: Clean Architecture 分层合理，UseCase 封装业务逻辑
3. ✅ **代码规范**: 遵循 Kotlin 最佳实践，注释完整
4. ✅ **测试意识**: 建立了测试框架，UseCase 层有较完整的单元测试

### 改进方向

1. 🔴 **安全加固**: 实现 TOFU 用户确认流程，建立日志安全机制
2. 📐 **架构优化**: 明确组件生命周期，改进状态管理
3. 🧪 **测试补全**: 添加 Instrumentation Tests 和 ViewModel/Repository 测试

### 推荐优先级

```
第一阶段 (发布前必须):
  - 修复 P0-1: TOFU 用户确认
  - 修复 P0-2: 日志安全机制

第二阶段 (下一版本):
  - 修复 P1-2: SSHClientWrapper 状态管理
  - 添加 ViewModel/Repository 测试

第三阶段 (持续改进):
  - 代码重构 (P2 问题)
  - 提高测试覆盖率至 80%+
```

---

## 📝 结论

SSH-Pad 项目整体代码质量**良好**，架构设计合理，安全意识较强。主要问题集中在：

1. **TOFU 流程安全性** - 需要实现用户确认机制
2. **测试覆盖率不足** - 特别是 Instrumentation Tests
3. **状态管理** - SSHClientWrapper 的 singleton 模式需要重新考虑

建议团队优先修复 P0 问题后再发布生产版本。

---

**审查完成时间**: 2026-03-24 10:45 GMT+8  
**审查工具**: OpenClaw 都察院  
**审查标准**: 安全 > 架构 > 质量 > 测试
