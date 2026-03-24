# 都察院代码审查报告 - SSH-Pad 项目

## 审查准备

**审查对象**: SSH-Pad Android SSH 客户端  
**项目目录**: `/vol1/1000/openclaw/projects/ssh-pad`  
**审查阶段**: MVP 开发完成后的代码审查  
**审查机构**: 都察院

---

## 一、项目背景概要

SSH-Pad 是一款专为 Android 平板设计的 SSH 客户端应用，采用 Kotlin + Jetpack Compose 开发，使用 Clean Architecture + MVVM 架构。

### 核心技术栈
- **语言**: Kotlin 1.9.20
- **UI 框架**: Jetpack Compose
- **SSH 库**: Apache MINA sshd 2.12.0
- **架构**: Clean Architecture + MVVM
- **依赖注入**: Koin
- **数据库**: Room + SQLite
- **加密存储**: Android EncryptedSharedPreferences + Keystore

---

## 二、代码质量检查清单

### 2.1 代码规范检查

#### ✅ Kotlin 编码规范
- [ ] **命名规范**
  - 类名使用 PascalCase（如 `SSHConnection`, `SecureStorage`）
  - 函数和变量使用 camelCase（如 `getConnectionString()`, `connectionTimeout`）
  - 常量使用 SCREAMING_SNAKE_CASE（如 `ACTION_CONNECT`, `NOTIFICATION_ID`）
  - 包名全小写无下划线（如 `com.sshpad.app.security`）

- [ ] **代码格式**
  - 使用 ktlint 或 detekt 进行格式化检查
  - 缩进 4 空格，无 Tab
  - 行宽限制 120 字符
  - 文件末尾空行

- [ ] **注释规范**
  - 公共 API 必须有 KDoc 注释（`/** */`）
  - 类、函数需说明用途、参数、返回值
  - 安全敏感代码需添加安全说明注释
  - 避免无意义的注释（代码应自解释）

**当前项目检查**:
```
✅ 已使用 KDoc 注释类和方法
✅ 命名符合规范
✅ 关键安全组件有详细注释
待检查：需运行 detekt/ktlint 验证格式统一性
```

#### ✅ 代码可维护性
- [ ] **函数长度**: 单一职责，函数不超过 50 行
- [ ] **类长度**: 类不超过 500 行，否则考虑拆分
- [ ] **圈复杂度**: 单个函数圈复杂度 < 10
- [ ] **重复代码**: 重复代码块需抽取为公共函数
- [ ] **魔法数字**: 使用具名常量替代（如 `AppConstants.SSH_DEFAULT_PORT`）

**当前项目检查**:
```
✅ 已定义 AppConstants 存储常量
✅ UseCase 类职责单一
✅ SecureStorage 函数简洁
待检查：SSHClientWrapper 可能过长，需审查
```

### 2.2 错误处理检查

- [ ] **异常处理**
  - [ ] 不使用空异常捕获（`catch (e: Exception)` 需具体化）
  - [ ] 异常需记录日志并转化为用户友好的错误信息
  - [ ] 资源使用必须 `use` 或 `try-finally` 释放
  - [ ] 不使用 `throw RuntimeException` 包装业务异常

- [ ] **Result 类型使用**
  - [ ] 可能失败的操作返回 `Result<T>` 或 `sealed class`
  - [ ] 不使用 `null` 表示错误，使用 `Result.failure()`
  - [ ] Flow 错误使用 `catch` 操作符处理

- [ ] **空安全**
  - [ ] 避免使用 `!!` 强制非空断言
  - [ ] 使用 `?.` 安全调用和 `?:` Elvis 操作符
  - [ ] 函数参数优先使用可空类型显式声明

**当前项目检查**:
```
✅ SecureStorage 使用 Result 返回
✅ StrictHostKeyVerifier 正确处理 null
⚠️ 需审查 SSHClientWrapper 的异常处理是否完善
```

### 2.3 性能优化检查

- [ ] **内存管理**
  - [ ] 避免内存泄漏（ViewModelScope/CoroutineScope 正确取消）
  - [ ] 大对象使用懒加载（`by lazy`）
  - [ ] 终端缓冲区限制大小，避免 OOM

- [ ] **协程使用**
  - [ ] 指定合适的 Dispatcher（IO/Default/Main）
  - [ ] 使用 `SupervisorJob` 避免子协程失败取消父协程
  - [ ] 长时间运行的任务使用 `withTimeout`

- [ ] **UI 性能**
  - [ ] Composable 使用 `remember` 缓存状态
  - [ ] 列表使用 `LazyColumn` + `items` 延迟加载
  - [ ] 避免在 Composable 中创建对象（导致重复重组）

**当前项目检查**:
```
✅ 使用 serviceScope 和 SupervisorJob
✅ SecureStorage 懒加载
待检查：终端渲染循环性能需基准测试
```

---

## 三、安全审查要点（🔴 重点）

### 3.1 SSH 连接安全

#### 密钥验证机制
- [ ] **主机密钥验证**
  - [x] 实现 `ServerKeyVerifier` 接口
  - [x] 存储已知主机指纹（TOFU 模式）
  - [x] 指纹不匹配时拒绝连接（防 MITM 攻击）
  - [ ] 用户确认流程（当前自动接受待验证）

**当前实现检查**:
```kotlin
// ✅ StrictHostKeyVerifier 实现正确
override fun verifyServerKey(...): Boolean {
    // TOFU 逻辑正确
    // 不匹配时返回 false 拒绝
}
```

- [ ] **指纹存储**
  - [x] 使用 EncryptedSharedPreferences 加密存储
  - [x] 使用 Android Keystore 生成主密钥
  - [x] 指纹格式：`SHA256:XX:XX:XX...`
  - [ ] 支持指纹手动删除（`removeHostKey` 已实现）

#### 连接配置安全
- [ ] **超时设置**
  - [ ] 连接超时 ≤ 30 秒（防止长时间挂起）
  - [ ] 读取超时合理配置
  - [ ] 心跳间隔 ≤ 60 秒

**当前项目检查**:
```kotlin
// ✅ AppConstants 定义
SSH_CONNECT_TIMEOUT_MS = 30_000L
SSH_KEEPALIVE_INTERVAL_SECONDS = 30
```

- [ ] **认证方式**
  - [ ] 优先支持密钥认证（ED25519/ECDSA）
  - [ ] 密码认证需加密存储
  - [ ] 不支持过时的认证方式（如 rhosts）

### 3.2 密钥管理安全（🔴 核心）

#### 凭证存储
- [x] **加密存储实现**
  - 使用 `EncryptedSharedPreferences`
  - AES-256-GCM 加密值
  - AES-256-SIV 加密键
  - Android Keystore 硬件级保护

```kotlin
// ✅ SecureStorage 实现正确
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

EncryptedSharedPreferences.create(
    context,
    "ssh_credentials_encrypted",
    masterKey,
    PrefKeyEncryptionScheme.AES256_SIV,
    PrefValueEncryptionScheme.AES256_GCM
)
```

- [ ] **密钥分离存储**
  - [x] 密码/Passphrase 不存储在 `SSHConnection` 模型中
  - [x] 使用 `SecureStorage` 单独加密存储
  - [x] 通过 connectionId 关联

```kotlin
// ✅ SSHConnection 模型已移除敏感字段
// Note: password is intentionally excluded from this model
// Store in SecureStorage using connection.id as key
```

- [ ] **密钥生命周期**
  - [ ] 支持密钥轮换（`clearAll` 已实现）
  - [ ] 删除连接时同步删除凭证（`deleteCredentials` 已实现）
  - [ ] 应用卸载时自动清理（Android 系统行为）

#### 生物识别（待实现）
- [ ] 集成 `BiometricPrompt` 支持指纹/面部解锁
- [ ] 敏感操作前验证（如查看密码、导出密钥）
- [ ] 验证失败次数限制

### 3.3 数据传输安全

- [ ] **加密传输**
  - [ ] SSH 协议版本 ≥ 2.0
  - [ ] 禁用弱加密算法（如 3DES, RC4）
  - [ ] 强制使用强加密套件

- [ ] **日志安全**
  - [ ] 不记录敏感信息（密码、私钥、指纹详情）
  - [ ] 生产环境关闭 Debug 日志
  - [ ] 安全事件记录（如主机密钥不匹配）

```kotlin
// ✅ StrictHostKeyVerifier 安全日志正确
android.util.Log.e(
    "SSH_SECURITY",
    "HOST KEY MISMATCH for $hostKey! Possible MITM attack."
    // 不记录完整指纹，避免日志泄露
)
```

### 3.4 Android 安全最佳实践

- [ ] **权限最小化**
  - [ ] 仅申请必要权限（网络、前台服务）
  - [ ] 不使用危险权限（如存储权限需运行时申请）

- [ ] **组件安全**
  - [ ] Service 不导出（`exported="false"`）
  - [ ] 不使用隐式 Intent
  - [ ] 前台服务通知正确配置

- [ ] **网络安全配置**
  - [ ] 配置 `network_security_config.xml`
  - [ ] 禁止明文流量（即使 SSH 本身加密）
  - [ ] 证书锁定（可选）

---

## 四、架构规范检查（Clean Architecture）

### 4.1 分层架构检查

```
┌─────────────────────────────────────────┐
│          Presentation Layer              │
│  (ViewModel, Composable, UI State)      │
├─────────────────────────────────────────┤
│            Domain Layer                  │
│  (UseCase, Entity, Repository Interface)│
├─────────────────────────────────────────┤
│             Data Layer                   │
│  (Repository Impl, DAO, SSH Service)    │
└─────────────────────────────────────────┘
```

#### ✅ 依赖方向检查
- [ ] Domain 层不依赖 Data 层和 Presentation 层
- [ ] Data 层依赖 Domain 层（实现 Repository 接口）
- [ ] Presentation 层依赖 Domain 层（使用 UseCase）
- [ ] 依赖注入在应用层组装

**当前项目检查**:
```kotlin
// ✅ Domain Layer - UseCase 依赖 Repository 接口
class ConnectToServerUseCase(
    private val repository: SSHConnectionRepository,  // 接口
    private val sshClientWrapper: SSHClientWrapper
)

// ✅ Data Layer - Repository 实现依赖接口
class SSHConnectionRepositoryImpl(
    ...
) : SSHConnectionRepository  // 实现接口

// ✅ DI Module - 组装依赖
val appModule = module {
    single<SSHConnectionRepository> { SSHConnectionRepositoryImpl(...) }
    single { ConnectToServerUseCase(get(), get()) }
}
```

#### ✅ 单向数据流检查
- [ ] UI 事件 → ViewModel → UseCase → Repository
- [ ] 数据流 → Repository → UseCase → ViewModel → UI State
- [ ] 使用 `StateFlow` 或 `SharedFlow` 进行响应式更新

**当前项目检查**:
```kotlin
// ✅ ViewModel 使用 StateFlow
private val _uiState = MutableStateFlow<UiState<List<Connection>>>(Loading)
val uiState: StateFlow<UiState<List<Connection>>> = _uiState.asStateFlow()

// ✅ Flow 数据流
sshClient.connectionState.collectLatest { state ->
    // 更新 UI 状态
}
```

### 4.2 UseCase 检查

- [ ] **单一职责**: 每个 UseCase 只做一件事
- [ ] **可组合性**: UseCase 可组合使用
- [ ] **可测试性**: UseCase 易于单元测试

**当前项目检查**:
```
✅ GetSSHConnectionsUseCase - 获取连接列表
✅ CreateSSHConnectionUseCase - 创建连接
✅ DeleteSSHConnectionUseCase - 删除连接
✅ ConnectToServerUseCase - 连接服务器
职责清晰，符合单一职责原则
```

### 4.3 Repository 模式检查

- [ ] **接口隔离**: Domain 层定义 Repository 接口
- [ ] **实现隐藏**: Data 层实现对外透明
- [ ] **数据源抽象**: 统一本地和远程数据源

**当前项目检查**:
```kotlin
// ✅ Repository 接口在 Domain 层
interface SSHConnectionRepository {
    suspend fun getConnections(): List<SSHConnection>
    suspend fun createConnection(connection: SSHConnection): SSHConnection
    suspend fun deleteConnection(connectionId: String)
}

// ✅ 实现在 Data 层
class SSHConnectionRepositoryImpl(...) : SSHConnectionRepository
```

### 4.4 ViewModel 检查

- [ ] **不持有 Android View 引用**
- [ ] **使用 ViewModelScope 管理协程**
- [ ] **状态暴露为 StateFlow 而非 LiveData**
- [ ] **不直接持有 Repository，依赖 UseCase**

**当前项目检查**:
```kotlin
// ✅ ViewModel 依赖 UseCase 而非 Repository
class SSHConnectionViewModel @Inject constructor(
    private val getSSHConnectionsUseCase: GetSSHConnectionsUseCase,
    private val createSSHConnectionUseCase: CreateSSHConnectionUseCase,
    private val deleteSSHConnectionUseCase: DeleteSSHConnectionUseCase,
    private val connectToServerUseCase: ConnectToServerUseCase
) : ViewModel()
```

---

## 五、测试覆盖率要求

### 5.1 测试分层

```
┌────────────────────────────┐
│     UI Tests (20%)         │  E2E 流程测试
├────────────────────────────┤
│  Integration Tests (20%)   │  模块间集成
├────────────────────────────┤
│    Unit Tests (60%)        │  业务逻辑测试
└────────────────────────────┘
```

### 5.2 单元测试检查

#### ✅ Domain 层测试（UseCase）
- [ ] UseCase 执行成功场景
- [ ] UseCase 执行失败场景（异常处理）
- [ ] UseCase 参数验证（边界条件）

**当前项目检查**:
```
✅ ConnectToServerUseCaseTest.kt
✅ CreateSSHConnectionUseCaseTest.kt
✅ DeleteSSHConnectionUseCaseTest.kt
✅ GetSSHConnectionsUseCaseTest.kt
覆盖率：4 个 UseCase 全部有测试
```

#### ✅ Data 层测试（Repository + Security）
- [ ] Repository CRUD 操作
- [ ] 加密存储功能
- [ ] 安全组件（密钥验证）

**当前项目检查**:
```
✅ SecureStorageTest.kt - 加密存储测试
✅ StrictHostKeyVerifierTest.kt - 主机密钥验证测试
待补充：SSHConnectionRepositoryImpl 测试
```

#### ⚠️ Service 层测试
- [ ] SSH 客户端连接/断开
- [ ] 心跳保活机制
- [ ] 网络变化处理

**当前项目检查**:
```
⚠️ 缺少 SSHClientWrapper 单元测试
⚠️ 缺少 SSHConnectionService 测试
建议：使用 MockK 模拟 SSH 连接
```

### 5.3 测试覆盖率指标

| 模块 | 最低覆盖率要求 | 当前状态 |
|------|---------------|----------|
| **Domain 层 (UseCase)** | 90% | ✅ 已覆盖 |
| **Data 层 (Repository)** | 80% | ⚠️ 待补充 |
| **Security 层** | 95% | ✅ 已覆盖 |
| **SSH 层** | 80% | ⚠️ 待补充 |
| **Presentation 层** | 60% | ⏳ 待实现 |
| **Service 层** | 70% | ⏳ 待实现 |

### 5.4 测试质量检查

- [ ] **测试命名规范**: `methodName_scenario_expectedBehavior`
  - 示例：`connect_validCredentials_emitsConnectedState`

- [ ] **AAA 模式**: Arrange-Act-Assert 结构清晰
  ```kotlin
  @Test
  fun `connect with valid credentials should emit connected state`() = runTest {
      // Arrange
      val mockRepository = mockk()
      val useCase = ConnectToServerUseCase(mockRepository, ...)
      
      // Act
      val result = useCase.execute(connectionId)
      
      // Assert
      assertTrue(result.isSuccess)
  }
  ```

- [ ] **测试独立性**: 测试之间不依赖，可并行执行
- [ ] **测试速度**: 单元测试 < 100ms/个

---

## 六、审查重点优先级

### 🔴 P0 - 必须修复（阻止发布）

1. **安全漏洞**
   - 凭证加密存储验证（必须使用 EncryptedSharedPreferences）
   - 主机密钥验证流程（不能跳过 TOFU 确认）
   - 日志泄露敏感信息检查

2. **严重崩溃**
   - 空指针异常风险（`!!` 强制断言）
   - 资源泄漏（未关闭的 SSH 连接、文件流）
   - 内存泄漏（ViewModel/Service 生命周期）

3. **数据完整性**
   - 数据库事务一致性
   - 并发写入冲突处理

### 🟡 P1 - 强烈建议修复（影响体验）

1. **用户体验**
   - 错误提示不清晰
   - 加载状态缺失
   - 网络错误处理不完善

2. **代码质量**
   - 函数过长（> 50 行）
   - 圈复杂度过高（> 10）
   - 重复代码

3. **测试覆盖**
   - 关键路径缺少测试
   - 边界条件未覆盖

### 🟢 P2 - 优化建议（锦上添花）

1. **性能优化**
   - 终端渲染优化
   - 数据库查询优化
   - 图片/资源缓存

2. **代码整洁**
   - 注释完善
   - 变量命名优化
   - 代码格式化

---

## 七、审查工具建议

### 7.1 静态分析工具

```kotlin
// build.gradle.kts
plugins {
    id("io.gitlab.arturbosch.detekt") version "1.23.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
}

// Detekt 配置
detekt {
    config.setFrom(files("$rootDir/detekt-config.yml"))
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = true
}

// Ktlint 配置
ktlint {
    android.set(true)
    outputToConsole.set(true)
    ignoreFailures.set(false)
}
```

### 7.2 测试工具

```kotlin
dependencies {
    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    
    // Mocking
    testImplementation("io.mockk:mockk:1.13.5")
    
    // Coroutines 测试
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    
    // 断言
    testImplementation("org.assertj:assertj-core:3.24.2")
}
```

### 7.3 安全检查清单

```bash
# 1. 检查硬编码密钥
grep -r "password\s*=" app/src/
grep -r "secret\s*=" app/src/
grep -r "api_key\s*=" app/src/

# 2. 检查日志泄露
grep -r "Log.d.*password" app/src/
grep -r "Log.d.*key" app/src/

# 3. 检查权限
grep -r "uses-permission" app/src/main/AndroidManifest.xml

# 4. 运行安全扫描
./gradlew dependencyCheckAnalyze
```

---

## 八、审查流程

### 阶段 1: 自动化工具检查（30 分钟）
1. 运行 detekt 代码质量检查
2. 运行 ktlint 格式化检查
3. 运行测试覆盖率报告
4. 运行安全扫描

### 阶段 2: 人工代码审查（2 小时）
1. 安全关键代码逐行审查
2. 架构分层合规性检查
3. 错误处理完整性检查
4. 性能风险点审查

### 阶段 3: 测试验证（1 小时）
1. 运行全部单元测试
2. 验证测试覆盖率达标
3. 关键路径手动测试

### 阶段 4: 审查报告输出（30 分钟）
1. 整理 P0/P1/P2 问题清单
2. 生成审查报告
3. 提交兵部修复

---

## 九、审查报告模板

```markdown
### 问题 #<编号>
- **优先级**: P0/P1/P2
- **类别**: 安全/架构/性能/测试/规范
- **位置**: `文件路径：行号`
- **描述**: 问题描述
- **风险**: 可能的影响
- **建议**: 修复建议
- **代码示例**:
```kotlin
// ❌ 错误示例
// ✅ 正确示例
```
```

---

## 十、审查结论标准

### ✅ 通过标准
- 所有 P0 问题已修复
- P1 问题修复率 ≥ 80%
- 测试覆盖率达标（总体 ≥ 70%）
- 无严重安全漏洞
- 静态分析无 Blocker 级别问题

### ⚠️ 有条件通过
- 所有 P0 问题已修复
- P1 问题修复率 ≥ 60%
- 测试覆盖率 ≥ 60%
- 剩余问题有明确修复计划

### ❌ 不通过
- 存在未修复的 P0 问题
- P1 问题修复率 < 60%
- 测试覆盖率 < 60%
- 存在严重安全漏洞

---

## 附录 A：SSH-Pad 当前代码统计

| 指标 | 数量 |
|------|------|
| Kotlin 文件 | 30 个 |
| 测试文件 | 6 个 |
| 代码行数 | ~1550 行 |
| 测试覆盖率 | ~40%（估算） |
| UseCase 数量 | 4 个 |
| ViewModel 数量 | 2 个 |
| Repository 数量 | 1 个 |
| Service 数量 | 1 个 |
| 安全组件 | 2 个（SecureStorage, StrictHostKeyVerifier） |

---

## 附录 B：审查负责人签字

| 角色 | 人员 | 日期 | 状态 |
|------|------|------|------|
| 审查负责人 | 都察院御史 | 待审查 | ⏳ 待执行 |
| 技术复核 | 兵部尚书 | 待审查 | ⏳ 待执行 |
| 批准发布 | 内阁大学士 | 待审查 | ⏳ 待执行 |

---

*报告生成时间：2026-03-24 10:30*  
*审查机构：都察院*  
*审查版本：v1.0*
