# Week 6 代码审查报告

## 审查概况

**审查时间：** 2026-04-27  
**审查范围：** Week 5-6 提交代码（MVP 开发阶段）  
**审查人：** 都察院御史  
**审查对象：** Android SSH 客户端 v0.1.0-alpha

### 代码统计

| 指标 | 数量 |
|------|------|
| Kotlin 文件数 | 14 |
| 总代码行数 | ~1,550 |
| 数据层文件 | 4 |
| 展示层文件 | 6 |
| SSH 核心文件 | 1 |
| 依赖注入文件 | 1 |
| 服务文件 | 1 |
| 测试文件 | 0 ❌ |

---

## 一、代码规范审查

### 1.1 命名规范 ✅

**评分：95/100**

**优点：**
- 类名遵循 PascalCase：`SSHConnection`, `TerminalSession`, `SSHClientWrapper`
- 函数名遵循 camelCase：`connect()`, `authenticateWithPassword()`, `getAllConnections()`
- 常量使用 UPPER_SNAKE_CASE：`CONNECTIONS_KEY`, `NOTIFICATION_ID`
- 包名规范：`com.sshpad.app.data.model`, `com.sshpad.app.presentation.screens`

**问题：**
```kotlin
// ⚠️ 问题：枚举值混用风格
enum class AuthType {
    PASSWORD,      // ✅ UPPER_SNAKE_CASE
    PRIVATE_KEY    // ✅ UPPER_SNAKE_CASE
}

enum class TerminalTheme {
    DARK,          // ✅ PascalCase
    LIGHT,         // ✅ PascalCase
    SOLARIZED_DARK // ✅ PascalCase
}
// 建议：统一使用一种风格（推荐 UPPER_SNAKE_CASE）
```

**改进建议：**
```kotlin
enum class TerminalTheme {
    DARK,
    LIGHT,
    SOLARIZED_DARK,
    SOLARIZED_LIGHT,
    MONOKAI
}
// →
enum class TerminalTheme {
    DARK,
    LIGHT,
    SOLARIZED_DARK,
    SOLARIZED_LIGHT,
    MONOKAI
}
// 实际保持一致，无需修改，但建议文档说明风格选择
```

### 1.2 代码格式化 ✅

**评分：98/100**

**优点：**
- 缩进统一使用 4 空格
- 操作符前后有空格
- 花括号前有空格
- 单行代码不超过 120 字符

**问题：**
```kotlin
// ⚠️ ConnectionListScreen.kt: 导入顺序可优化
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
// ... 中间无空行
import androidx.compose.material3.*

// 建议：按 Import 分组规范添加空行
```

### 1.3 文档和注释 ✅

**评分：90/100**

**优点：**
- 公共类有 KDoc 注释
- 函数有清晰的用途说明
- 枚举值有注释

**问题：**
```kotlin
// ❌ 缺少 KDoc
class SSHPadApplication : Application() {
    override fun onCreate() { ... }
}

// ✅ 建议添加
/**
 * SSH Pad Application Class
 * 
 * Initializes dependency injection and app-wide components
 */
class SSHPadApplication : Application() { ... }
```

**TODO 注释缺失：**
```kotlin
// ❌ 当前代码
// TODO: Load connection from Repository

// ✅ 建议格式
// TODO(张工): Load connection from Repository - Issue #45
```

### 1.4 代码结构 ✅

**评分：95/100**

**优点：**
- 包结构清晰：`data`, `presentation`, `ssh`, `di`, `service`
- 文件内函数组织合理
- 相关类和接口放在同一文件（如 `TerminalSession` 和 `TerminalBuffer`）

---

## 二、架构审查

### 2.1 Clean Architecture 分层 ⚠️

**评分：75/100**

**当前架构：**
```
app/
├── data/
│   ├── model/
│   │   ├── SSHConnection.kt
│   │   └── TerminalSession.kt
│   └── repository/
│       ├── SSHConnectionRepository.kt (接口)
│       └── impl/
│           └── SSHConnectionRepositoryImpl.kt
├── presentation/
│   ├── MainActivity.kt
│   ├── navigation/
│   │   └── AppNavigation.kt
│   ├── screens/
│   │   ├── ConnectionListScreen.kt
│   │   ├── ConnectionEditScreen.kt
│   │   └── TerminalScreen.kt
│   └── ui/theme/
│       └── Theme.kt
├── ssh/
│   └── SSHClientWrapper.kt
├── di/
│   └── AppModule.kt
├── service/
│   └── SSHConnectionService.kt
└── SSHPadApplication.kt
```

**问题：**

1. **缺少 Domain 层** ❌
   - 无 Use Case 封装
   - 业务逻辑分散在 Repository 和 ViewModel（待实现）
   - 建议：创建 `domain/` 模块，包含 Use Case 和领域模型

2. **SSH 层位置不明确** ⚠️
   - `ssh/` 包在根目录，应归属 Data 层
   - 建议：移至 `data/ssh/` 或 `data/remote/`

3. **Service 层职责不清** ⚠️
   - `SSHConnectionService` 混合了业务逻辑和 Android Service 生命周期
   - 建议：Service 仅处理生命周期，业务逻辑委托给 Repository/UseCase

**改进建议架构：**
```
app/
├── data/
│   ├── model/           # 数据模型
│   ├── repository/      # Repository 实现
│   ├── local/           # 本地数据源（DataStore）
│   └── remote/          # 远程数据源（SSH）
├── domain/
│   ├── model/           # 领域模型（可选，可与 data.model 合并）
│   ├── repository/      # Repository 接口
│   └── usecase/         # Use Cases
│       ├── GetConnections.kt
│       ├── AddConnection.kt
│       ├── DeleteConnection.kt
│       └── ConnectToHost.kt
├── presentation/
│   ├── model/           # UI 模型（State, Event）
│   ├── ViewModel/       # ViewModels（待实现）
│   ├── screens/         # Composable 屏幕
│   ├── navigation/      # 导航图
│   └── ui/              # 主题、组件
└── di/                  # 依赖注入模块
```

### 2.2 MVVM 模式 ⚠️

**评分：50/100**

**当前状态：**
- ❌ 缺少 ViewModel 层
- ✅ UI 使用 Compose
- ⚠️ 屏幕直接操作数据（Mock 数据）

**问题代码：**
```kotlin
// ❌ ConnectionListScreen.kt: 直接在 Composable 中持有数据
val connections = remember {
    mutableStateOf(listOf(
        SSHConnection(...) // Mock 数据
    ))
}

// ✅ 应该使用 ViewModel
@Composable
fun ConnectionListScreen(
    viewModel: ConnectionListViewModel = hiltViewModel(),
    onConnectionClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    // 使用 uiState.connections
}
```

**Week 6 待办：**
- [ ] 创建 `ConnectionListViewModel`
- [ ] 创建 `ConnectionEditViewModel`
- [ ] 创建 `TerminalViewModel`
- [ ] 定义 UI State 和 Event 类
- [ ] 集成 Repository

### 2.3 依赖注入 ✅

**评分：85/100**

**优点：**
- 使用 Koin 进行 DI
- Module 定义清晰
- 使用 `single` / `factory` 正确

**问题：**
```kotlin
// ⚠️ AppModule.kt: 注释说明 ViewModels 待添加
// ViewModels will be added in Week 6

// ❌ 使用 androidContext() 可能过度
single<SSHConnectionRepository> { SSHConnectionRepositoryImpl(androidContext()) }
// 建议：Repository 不应依赖 Context，应传入 DataStore 实例

// ✅ 改进建议
val appModule = module {
    // DataStore
    single { 
        PreferenceDataStoreFactory.create(
            produceFile = { context.filesDir.resolve("ssh_connections.preferences_pb") }
        ) 
    }
    
    // Repository
    single<SSHConnectionRepository> { 
        SSHConnectionRepositoryImpl(get()) // 注入 DataStore
    }
    
    // SSH Client
    single { SSHClientWrapper() }
    
    // ViewModels
    viewModel { ConnectionListViewModel(get()) }
    viewModel { ConnectionEditViewModel(get(), get()) }
    viewModel { TerminalViewModel(get(), get()) }
}
```

---

## 三、安全检查

### 3.1 密钥存储安全 ❌

**评分：20/100**

**高危问题：**

1. **密码明文存储在数据模型中**
```kotlin
// ❌ SSHConnection.kt
data class SSHConnection(
    val password: String? = null,           // ❌ 明文
    val privateKeyPassphrase: String? = null // ❌ 明文
)
```

**修复建议：**
- 使用 EncryptedSharedPreferences 存储敏感字段
- 或仅存储加密后的引用 ID

2. **私钥路径明文**
```kotlin
// ❌ 存储文件路径
val privateKeyPath: String? = null

// ✅ 应存储安全引用
val privateKeyId: String? = null // Android Keystore 中的 ID
```

### 3.2 网络通信安全 ❌

**评分：40/100**

**严重问题：**

1. **AcceptAllServerKeyVerifier**
```kotlin
// ❌ SSHClientWrapper.kt:28
sshClient.serverKeyVerifier = AcceptAllServerKeyVerifier.INSTANCE
// 风险：接受任何服务器密钥，中间人攻击风险
```

**修复代码：**
```kotlin
// ✅ 应实现指纹验证
class FingerprintServerKeyVerifier(
    private val knownHosts: Set<String>
) : ServerKeyVerifier {
    override fun verifyServerKey(
        session: ClientSession,
        remoteAddress: SocketAddress,
        serverKey: PublicKey
    ): Boolean {
        val fingerprint = serverKey.fingerprint()
        return knownHosts.contains(fingerprint)
    }
}
```

2. **缺少 SSH 协议版本限制**
```kotlin
// 建议明确配置
sshClient.version = "SSH-2.0"
```

3. **未配置加密算法白名单**
```kotlin
// 建议配置
sshClient.securityProviderFactory = SecurityProviderFactory().apply {
    addCipherFactories(
        "AES256-GCM",
        "ChaCha20-Poly1305"
    )
}
```

### 3.3 权限最小化 ✅

**评分：80/100**

**优点：**
- 未申请过度权限
- Service 使用 Foreground

**待改进：**
- [ ] `AndroidManifest.xml` 未提供，需检查 `exported` 属性
- [ ] Service 应设置 `android:exported="false"`

### 3.4 日志安全 ⚠️

**评分：60/100**

**问题：**
```kotlin
// ⚠️ 当前无明显日志泄露，但需预防
// 建议：添加日志规范文档，Release 禁用 Debug 日志
```

---

## 四、代码质量

### 4.1 函数设计 ✅

**评分：90/100**

**优点：**
- 函数职责单一
- 长度适中

**问题：**
```kotlin
// ⚠️ SSHClientWrapper.connect() 略长（~50 行）
// 建议：拆分为更小的私有函数
```

### 4.2 空安全 ✅

**评分：95/100**

**优点：**
- 无 `!!` 操作符滥用
- 使用 `?.`, `?:` 处理 null

**小问题：**
```kotlin
// ⚠️ SSHClientWrapper.kt:131
val error = errOutputStream.toString(Charsets.UTF_8)
if (error.isNotEmpty()) {
    Result.failure(Exception(error))
}
// 建议：即使 error 为空，也应返回输出
```

### 4.3 异常处理 ✅

**评分：85/100**

**优点：**
- 使用 `Result` 封装
- 捕获具体异常

**问题：**
```kotlin
// ⚠️ 空 catch 块
} catch (e: Exception) {
    // Ignore cleanup errors
}

// 建议：至少记录日志
} catch (e: Exception) {
    Log.w("SSH", "Cleanup error", e)
}
```

### 4.4 协程使用 ✅

**评分：90/100**

**优点：**
- 使用 `suspend` 函数
- 使用 `Flow` 进行状态流

**待改进：**
```kotlin
// ⚠️ SSHConnectionService.kt: 使用 CoroutineScope 但未处理取消
private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

// ✅ 应使用 lifecycleScope 或正确处理取消
override fun onDestroy() {
    super.onDestroy()
    serviceScope.cancel() // ✅ 已有，良好
}
```

### 4.5 资源管理 ✅

**评分：95/100**

**优点：**
- `disconnect()` 清理资源
- `stop()` 关闭 SSH 客户端

---

## 五、测试覆盖 ❌

**评分：0/100**

**严重问题：**
- ❌ 无单元测试文件
- ❌ 无集成测试
- ❌ 无 UI 测试

**Week 6 必须完成：**
- [ ] `SSHConnectionRepositoryTest`
- [ ] `ConnectionListViewModelTest`
- [ ] `SSHClientWrapperTest`（Mock SSH 服务器）
- [ ] 覆盖率 > 70%

---

## 六、发现的问题汇总

### 🔴 阻塞性问题（必须修复）

| 编号 | 问题 | 文件 | 优先级 |
|------|------|------|--------|
| S-001 | AcceptAllServerKeyVerifier 中间人攻击风险 | SSHClientWrapper.kt:28 | 🔴 P0 |
| S-002 | 密码明文存储 | SSHConnection.kt | 🔴 P0 |
| S-003 | 无单元测试 | test/ 目录为空 | 🔴 P0 |
| A-001 | 缺少 ViewModel 层 | presentation/ | 🔴 P0 |
| A-002 | 缺少 Domain 层 | 无 domain/ 目录 | 🔴 P0 |

### 🟡 建议性问题（应该修复）

| 编号 | 问题 | 文件 | 优先级 |
|------|------|------|--------|
| S-004 | 私钥路径明文存储 | SSHConnection.kt | 🟡 P1 |
| S-005 | 未配置 SSH 加密算法白名单 | SSHClientWrapper.kt | 🟡 P1 |
| A-003 | Repository 依赖 Context | SSHConnectionRepositoryImpl.kt | 🟡 P1 |
| A-004 | Service 混合业务逻辑 | SSHConnectionService.kt | 🟡 P1 |
| C-001 | 缺少 KDoc | SSHPadApplication.kt | 🟡 P2 |
| C-002 | TODO 注释无责任人 | 多处 | 🟡 P2 |

### 🟢 优化性问题（可改进）

| 编号 | 问题 | 文件 | 优先级 |
|------|------|------|--------|
| C-003 | 枚举风格不统一 | TerminalTheme | 🟢 P3 |
| C-004 | Import 顺序可优化 | 多个文件 | 🟢 P3 |
| C-005 | 空 catch 块未记录日志 | SSHClientWrapper.kt | 🟢 P3 |

---

## 七、审查结论

### 总体评分

| 维度 | 得分 | 评级 |
|------|------|------|
| 代码规范 | 94/100 | ✅ 优秀 |
| 安全检查 | 35/100 | ❌ 不通过 |
| 架构分层 | 70/100 | ⚠️ 待改进 |
| 单测覆盖 | 0/100 | ❌ 不通过 |

**综合评分：50/100** ❌ **不通过**

### 合并建议

**❌ 不建议合并到主分支**

**原因：**
1. 存在严重安全漏洞（AcceptAllServerKeyVerifier）
2. 缺少 ViewModel 层，架构不完整
3. 无任何单元测试
4. 密码明文存储风险

### 修复要求

**必须修复（P0）：**
1. 实现服务器指纹验证
2. 实现 ViewModel 层
3. 编写单元测试（覆盖率 > 70%）
4. 加密存储密码

**建议修复（P1）：**
1. 创建 Domain 层
2. 重构 Repository 依赖
3. Service 职责分离

---

## 八、Week 6 优先任务

1. **安全修复**（2 天）
   - [ ] 替换 AcceptAllServerKeyVerifier
   - [ ] 实现 EncryptedSharedPreferences
   - [ ] 添加服务器指纹管理

2. **架构完善**（2 天）
   - [ ] 创建 Domain 层
   - [ ] 实现 Use Cases
   - [ ] 创建 ViewModels
   - [ ] 集成 Repository

3. **测试编写**（2 天）
   - [ ] Repository 单元测试
   - [ ] ViewModel 单元测试
   - [ ] SSH 客户端 Mock 测试
   - [ ] 覆盖率达到 70%

4. **代码优化**（1 天）
   - [ ] 补充 KDoc
   - [ ] 规范化 TODO 注释
   - [ ] 代码格式化

---

## 附录：审查工具使用

- **ktlint**: 未集成（建议 Week 6 添加）
- **detekt**: 未集成（建议 Week 6 添加）
- **Jacoco**: 未配置（建议 Week 6 配置）

---

*审查人：都察院御史*  
*审查日期：2026-04-27*  
*版本：v1.0*  
*下次审查：待 P0 问题修复后重新审查*
