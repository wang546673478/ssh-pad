# Week 6 测试覆盖率报告

**报告时间：** 2026-04-27  
**报告人：** 都察院御史  
**报告对象：** Android SSH 客户端 Week 6 P0 修复  
**测试状态：** ❌ **无测试代码**

---

## 一、测试覆盖现状

### 1.1 测试目录结构

```
app/src/
├── main/
│   └── java/com/sshpad/app/
│       ├── data/
│       │   ├── model/
│       │   │   ├── SSHConnection.kt
│       │   │   └── TerminalSession.kt
│       │   └── repository/
│       │       ├── SSHConnectionRepository.kt
│       │       └── impl/SSHConnectionRepositoryImpl.kt
│       ├── security/
│       │   └── SecureStorage.kt                    # ✅ 新增
│       ├── ssh/
│       │   ├── SSHClientWrapper.kt
│       │   └── verifier/StrictHostKeyVerifier.kt   # ✅ 新增
│       ├── presentation/
│       │   ├── screens/
│       │   ├── navigation/
│       │   └── ui/
│       ├── di/
│       │   └── AppModule.kt
│       └── service/
│           └── SSHConnectionService.kt
├── test/
│   └── (空目录)                                    ❌ 无测试
└── androidTest/
    └── (空目录)                                    ❌ 无测试
```

### 1.2 测试覆盖率统计

| 模块 | 文件数 | 代码行数 | 测试文件数 | 测试行数 | 覆盖率 |
|------|--------|----------|------------|----------|--------|
| **data/** | 4 | ~400 | 0 | 0 | 0% |
| **security/** | 1 | ~140 | 0 | 0 | 0% |
| **ssh/** | 2 | ~350 | 0 | 0 | 0% |
| **presentation/** | 6 | ~600 | 0 | 0 | 0% |
| **di/** | 1 | ~20 | 0 | 0 | 0% |
| **service/** | 1 | ~150 | 0 | 0 | 0% |
| **总计** | **15** | **~1660** | **0** | **0** | **0%** |

**目标覆盖率：** 70%  
**当前覆盖率：** 0%  
**缺口：** 70%

---

## 二、测试缺口分析

### 2.1 单元测试缺口

#### SecureStorage（新增模块）

**文件：** `app/src/main/java/com/sshpad/app/security/SecureStorage.kt`  
**代码行数：** 140 行  
**测试文件：** 无  
**优先级：** 🔴 P0

**缺失测试：**
```kotlin
// SecureStorageTest.kt - 应包含的测试用例

// ✅ 密码存储测试
@Test fun savePassword_encryptsAndStores()
@Test fun getPassword_decryptsCorrectly()
@Test fun deletePassword_removesSecurely()
@Test fun hasPassword_returnsTrueAfterSave()

// ✅ Passphrase 存储测试
@Test fun savePassphrase_encryptsAndStores()
@Test fun getPassphrase_decryptsCorrectly()
@Test fun deletePassphrase_removesSecurely()

// ✅ 批量操作测试
@Test fun deleteCredentials_removesBothPasswordAndPassphrase()
@Test fun clearAll_removesAllCredentials()

// ✅ 边界条件测试
@Test fun getPassword_nonExistent_returnsNull()
@Test fun savePassword_emptyConnectionId_throwsException()
@Test fun savePassword_nullPassword_deletesExisting()
```

**预估测试代码量：** ~200 行

---

#### StrictHostKeyVerifier（新增模块）

**文件：** `app/src/main/java/com/sshpad/app/ssh/verifier/StrictHostKeyVerifier.kt`  
**代码行数：** 230 行  
**测试文件：** 无  
**优先级：** 🔴 P0

**缺失测试：**
```kotlin
// StrictHostKeyVerifierTest.kt - 应包含的测试用例

// ✅ 首次连接测试（TOFU）
@Test fun verifyServerKey_firstConnection_acceptsAndStores()
@Test fun verifyServerKey_firstConnection_addsToPending()

// ✅ 已知主机测试
@Test fun verifyServerKey_knownHost_matchingFingerprint_accepts()
@Test fun verifyServerKey_knownHost_storedFingerprintUsed()

// ✅ MITM 防护测试
@Test fun verifyServerKey_knownHost_mismatchedFingerprint_rejects()
@Test fun verifyServerKey_knownHost_mismatchedFingerprint_logsSecurityEvent()

// ✅ 用户确认测试
@Test fun acceptHostKey_pendingHost_movesToKnownHosts()
@Test fun rejectHostKey_pendingHost_removesFromPending()
@Test fun acceptHostKey_noPendingHost_returnsFailure()

// ✅ 主机管理测试
@Test fun removeHostKey_knownHost_removesFromStorage()
@Test fun removeHostKey_unknownHost_returnsFailure()
@Test fun getKnownHosts_returnsAllKnownHosts()
@Test fun isHostKnown_true_forKnownHost()
@Test fun isHostKnown_false_forUnknownHost()

// ✅ 指纹解析测试
@Test fun extractFingerprint_generatesSHA256Format()
@Test fun parseFingerprint_validData_parsesCorrectly()
@Test fun parseFingerprint_invalidData_returnsNull()
```

**预估测试代码量：** ~350 行

---

#### SSHConnectionRepository（核心模块）

**文件：** `app/src/main/java/com/sshpad/app/data/repository/impl/SSHConnectionRepositoryImpl.kt`  
**代码行数：** 200 行  
**测试文件：** 无  
**优先级：** 🔴 P0

**缺失测试：**
```kotlin
// SSHConnectionRepositoryImplTest.kt - 应包含的测试用例

// ✅ 连接管理测试
@Test fun getAllConnections_emptyList_returnsEmptyFlow()
@Test fun getAllConnections_withConnections_emitsList()
@Test fun getConnectionById_existingId_returnsConnection()
@Test fun getConnectionById_nonExistingId_returnsNull()

// ✅ CRUD 操作测试
@Test fun addConnection_validConnection_savesSuccessfully()
@Test fun addConnection_duplicateId_addsAnyway()
@Test fun updateConnection_existingConnection_updatesFields()
@Test fun updateConnection_nonExistingConnection_doesNothing()
@Test fun deleteConnection_existingConnection_removes()
@Test fun deleteConnection_nonExistingConnection_doesNothing()

// ✅ 凭证管理测试（新增）
@Test fun saveCredentials_withPassword_storesInSecureStorage()
@Test fun saveCredentials_withPassphrase_storesInSecureStorage()
@Test fun getCredentials_existingId_retrievesFromSecureStorage()
@Test fun deleteCredentials_existingId_removesFromSecureStorage()

// ✅ 特殊功能测试
@Test fun getRecentConnections_returnsMostRecent()
@Test fun getRecentConnections_withLimit_limitsResults()
@Test fun searchConnections_byName_returnsMatching()
@Test fun searchConnections_byHost_returnsMatching()
@Test fun searchConnections_noMatch_returnsEmpty()

// ✅ 数据持久化测试
@Test fun addConnection_processRestart_dataPersists()
@Test fun updateConnection_processRestart_dataPersists()
@Test fun deleteConnection_processRestart_dataDeleted()

// ✅ 异常处理测试
@Test fun addConnection_dataStoreFailure_returnsFailure()
@Test fun updateConnection_serializationError_returnsFailure()
@Test fun getAllConnections_corruptedData_returnsEmpty()
```

**预估测试代码量：** ~400 行

---

#### SSHClientWrapper（核心模块）

**文件：** `app/src/main/java/com/sshpad/app/ssh/SSHClientWrapper.kt`  
**代码行数：** 250 行  
**测试文件：** 无  
**优先级：** 🔴 P0

**缺失测试：**
```kotlin
// SSHClientWrapperTest.kt - 应包含的测试用例

// ✅ 连接测试
@Test fun connect_validCredentials_connectsSuccessfully()
@Test fun connect_invalidPassword_authenticationFails()
@Test fun connect_invalidHost_connectionFails()
@Test fun connect_timeout_connectionTimeout()

// ✅ 服务器密钥验证测试（新增）
@Test fun connect_usesStrictHostKeyVerifier()
@Test fun connect_unknownServer_storesFingerprint()
@Test fun connect_knownServer_verifiesFingerprint()
@Test fun connect_mitmAttack_rejectsConnection()

// ✅ 认证测试
@Test fun authenticateWithPassword_validPassword_succeeds()
@Test fun authenticateWithPassword_nullPassword_fails()
@Test fun authenticateWithPrivateKey_validKey_succeeds()
@Test fun authenticateWithPrivateKey_invalidPath_fails()

// ✅ Shell 会话测试
@Test fun startShell_connected_startsSuccessfully()
@Test fun startShell_disconnected_returnsFailure()
@Test fun executeCommand_validCommand_returnsOutput()
@Test fun executeCommand_invalidCommand_returnsError()

// ✅ 资源管理测试
@Test fun disconnect_activeConnection_closesResources()
@Test fun disconnect_disconnectedConnection_noOp()
@Test fun stop_callsDisconnect()
@Test fun stop_stopsSshClient()

// ✅ 状态管理测试
@Test fun connect_updatesStateToConnecting()
@Test fun connect_success_updatesStateToConnected()
@Test fun connect_failure_updatesStateToError()
@Test fun disconnect_updatesStateToDisconnected()
```

**预估测试代码量：** ~400 行

---

#### SSHConnection（数据模型）

**文件：** `app/src/main/java/com/sshpad/app/data/model/SSHConnection.kt`  
**代码行数：** 80 行  
**测试文件：** 无  
**优先级：** 🟡 P1

**缺失测试：**
```kotlin
// SSHConnectionTest.kt - 应包含的测试用例

// ✅ 辅助方法测试
@Test fun getConnectionString_returnsUserAtHostPort()
@Test fun getDisplayName_returnsNameWithHost()

// ✅ 默认值测试
@Test fun defaultPort_is22()
@Test fun defaultAuthType_isPASSWORD()
@Test fun defaultKeepAliveInterval_is60()

// ✅ 枚举测试
@Test fun authType_PASSWORD_exists()
@Test fun authType_PRIVATE_KEY_exists()
@Test fun status_values_exist()
```

**预估测试代码量：** ~80 行

---

### 2.2 集成测试缺口

**测试文件：** 无  
**优先级：** 🟡 P1

**缺失测试：**
```kotlin
// SSHIntegrationTest.kt - 应包含的集成测试

// ✅ 完整连接流程
@Test fun fullConnectionFlow_connectInteractDisconnect()

// ✅ Repository + SecureStorage 集成
@Test fun repositoryIntegration_saveAndRetrieveCredentials()

// ✅ StrictHostKeyVerifier + SSHClientWrapper 集成
@Test fun hostKeyVerification_completeFlow()
```

**预估测试代码量：** ~150 行

---

### 2.3 UI 测试缺口

**测试文件：** 无  
**优先级：** 🟢 P2

**缺失测试：**
```kotlin
// ConnectionListScreenTest.kt
@Test fun connectionListScreen_displaysConnections()
@Test fun connectionListScreen_emptyState_showsEmptyMessage()
@Test fun connectionListScreen_clickNavigatesToDetail()

// ConnectionEditScreenTest.kt
@Test fun connectionEditScreen_saveValidData_succeeds()
@Test fun connectionEditScreen_saveInvalidData_showsError()
```

**预估测试代码量：** ~200 行

---

## 三、测试覆盖率目标

### 3.1 覆盖率分解

| 模块 | 当前覆盖率 | Week 6 目标 | Week 7 目标 | 最终目标 |
|------|------------|------------|------------|----------|
| **security/** | 0% | 90% | 95% | 95% |
| **ssh/** | 0% | 85% | 90% | 90% |
| **data/** | 0% | 80% | 85% | 85% |
| **presentation/** | 0% | 0% | 70% | 80% |
| **service/** | 0% | 0% | 60% | 70% |
| **di/** | 0% | 0% | 50% | 50% |
| **总体** | **0%** | **70%** | **80%** | **80%** |

### 3.2 优先级排序

**P0 - 必须完成（Week 6）：**
1. ✅ SecureStorage 测试（90% 覆盖率）
2. ✅ StrictHostKeyVerifier 测试（90% 覆盖率）
3. ✅ SSHConnectionRepository 测试（80% 覆盖率）
4. ✅ SSHClientWrapper 测试（80% 覆盖率）

**P1 - 应该完成（Week 7）：**
1. SSHConnection 测试（85% 覆盖率）
2. TerminalSession 测试（80% 覆盖率）
3. 集成测试（70% 覆盖率）

**P2 - 可以延后（Week 8+）：**
1. UI 测试（70% 覆盖率）
2. Service 测试（60% 覆盖率）
3. DI 测试（50% 覆盖率）

---

## 四、测试实施计划

### 4.1 测试环境配置

**build.gradle.kts 需要添加：**
```kotlin
dependencies {
    // 单元测试
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("com.google.truth:truth:1.1.5")
    
    // Android 测试
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("io.mockk:mockk-android:1.13.8")
    
    // 覆盖率
    implementation("org.jacoco:org.jacoco.core:0.8.11")
}

android {
    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }
    
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}
```

### 4.2 测试实施步骤

#### 步骤 1：配置测试环境（0.5 天）
- [ ] 添加测试依赖
- [ ] 配置 Jacoco 覆盖率
- [ ] 创建测试目录结构

#### 步骤 2：编写 SecureStorage 测试（0.5 天）
- [ ] 创建 SecureStorageTest.kt
- [ ] 编写密码存储测试
- [ ] 编写 passphrase 存储测试
- [ ] 编写边界条件测试
- [ ] 运行测试，确保 90%+ 覆盖率

#### 步骤 3：编写 StrictHostKeyVerifier 测试（1 天）
- [ ] 创建 StrictHostKeyVerifierTest.kt
- [ ] 编写 TOFU 测试
- [ ] 编写指纹验证测试
- [ ] 编写 MITM 防护测试
- [ ] 编写用户确认测试
- [ ] 运行测试，确保 90%+ 覆盖率

#### 步骤 4：编写 Repository 测试（1 天）
- [ ] 创建 SSHConnectionRepositoryImplTest.kt
- [ ] 编写 CRUD 测试
- [ ] 编写凭证管理测试
- [ ] 编写持久化测试
- [ ] 编写异常处理测试
- [ ] 运行测试，确保 80%+ 覆盖率

#### 步骤 5：编写 SSHClientWrapper 测试（1 天）
- [ ] 创建 SSHClientWrapperTest.kt
- [ ] 编写连接测试
- [ ] 编写服务器密钥验证测试
- [ ] 编写认证测试
- [ ] 编写资源管理测试
- [ ] 运行测试，确保 80%+ 覆盖率

#### 步骤 6：编写数据模型测试（0.5 天）
- [ ] 创建 SSHConnectionTest.kt
- [ ] 编写辅助方法测试
- [ ] 编写默认值测试
- [ ] 运行测试，确保 85%+ 覆盖率

#### 步骤 7：编写集成测试（1 天）
- [ ] 创建 SSHIntegrationTest.kt
- [ ] 编写完整流程测试
- [ ] 编写模块集成测试
- [ ] 运行测试，确保 70%+ 覆盖率

#### 步骤 8：生成覆盖率报告（0.5 天）
- [ ] 运行覆盖率任务
- [ ] 生成 HTML 报告
- [ ] 分析覆盖率缺口
- [ ] 补充缺失测试

**总预估时间：** 5 天

---

## 五、测试质量要求

### 5.1 测试命名规范

```kotlin
// ✅ 推荐格式
@Test fun methodName_condition_expectedResult()

// 示例
@Test fun savePassword_validPassword_encryptsAndStores()
@Test fun verifyServerKey_mismatchedFingerprint_rejectsConnection()
@Test fun addConnection_dataStoreFailure_returnsFailure()
```

### 5.2 测试结构规范（AAA 模式）

```kotlin
@Test
fun savePassword_validPassword_encryptsAndStores() {
    // Arrange（准备）
    val secureStorage = SecureStorage(context)
    val connectionId = "test-connection"
    val password = "test-password"
    
    // Act（执行）
    secureStorage.savePassword(connectionId, password)
    
    // Assert（断言）
    val retrievedPassword = secureStorage.getPassword(connectionId)
    assertThat(retrievedPassword).isEqualTo(password)
}
```

### 5.3 测试独立性要求

```kotlin
// ✅ 每个测试独立
@Test
fun test1() { /* 不依赖其他测试 */ }

@Test
fun test2() { /* 不依赖 test1 的结果 */ }

// ✅ 使用 @Before 和 @After 清理
@Before
fun setup() {
    // 每个测试前执行
}

@After
fun cleanup() {
    // 每个测试后清理
}
```

### 5.4 测试覆盖率要求

| 覆盖率类型 | 要求 | 说明 |
|------------|------|------|
| 行覆盖率 | > 80% | 代码行执行比例 |
| 分支覆盖率 | > 75% | 条件分支覆盖比例 |
| 类覆盖率 | > 90% | 类被测试比例 |
| 方法覆盖率 | > 85% | 方法被测试比例 |

---

## 六、测试执行结果（待填写）

### 6.1 单元测试执行

```
执行时间：待填写
总测试数：待填写
通过数：待填写
失败数：待填写
跳过数：待填写

执行报告：
--------------------------------------------------
| 测试类                    | 测试数 | 通过率 | 覆盖率 |
|--------------------------|--------|--------|--------|
| SecureStorageTest        |   --   |  --%   |  --%   |
| StrictHostKeyVerifierTest|   --   |  --%   |  --%   |
| SSHConnectionRepositoryTest|  --   |  --%   |  --%   |
| SSHClientWrapperTest     |   --   |  --%   |  --%   |
| SSHConnectionTest        |   --   |  --%   |  --%   |
--------------------------------------------------
总覆盖率：--%
```

### 6.2 集成测试执行

```
执行时间：待填写
总测试数：待填写
通过数：待填写
失败数：待填写

执行报告：
--------------------------------------------------
| 测试类                    | 测试数 | 通过率 | 覆盖率 |
|--------------------------|--------|--------|--------|
| SSHIntegrationTest       |   --   |  --%   |  --%   |
--------------------------------------------------
```

### 6.3 覆盖率汇总

```
代码覆盖率汇总（Jacoco）
--------------------------------------------------
| 模块          | 行覆盖率 | 分支覆盖率 | 类覆盖率 |
|--------------|----------|------------|----------|
| security/    |   --%    |    --%     |   --%    |
| ssh/         |   --%    |    --%     |   --%    |
| data/        |   --%    |    --%     |   --%    |
| presentation/|   --%    |    --%     |   --%    |
| service/     |   --%    |    --%     |   --%    |
--------------------------------------------------
总体覆盖率：--%
目标覆盖率：70%
状态：❌ 未达标 / ✅ 达标
```

---

## 七、结论与建议

### 7.1 当前状态

**测试代码：** ❌ **无**  
**测试覆盖率：** **0%**  
**目标覆盖率：** **70%**  
**状态：** ❌ **严重不达标**

### 7.2 风险评估

| 风险 | 级别 | 说明 |
|------|------|------|
| 回归风险 | 🔴 高 | 无测试保护，重构可能导致回归 |
| 质量风险 | 🔴 高 | 无法验证代码正确性 |
| 维护风险 | 🔴 高 | 未来修改无测试保障 |
| 安全风险 | 🔴 高 | 安全功能未验证 |

### 7.3 建议行动

**立即行动（24 小时内）：**
1. [ ] 添加测试依赖到 build.gradle.kts
2. [ ] 配置 Jacoco 覆盖率
3. [ ] 创建第一个测试文件（SecureStorageTest）

**本周完成（Week 6）：**
1. [ ] 完成所有 P0 模块测试
2. [ ] 达到 70% 覆盖率目标
3. [ ] 生成覆盖率报告
4. [ ] 修复失败测试

**下周完成（Week 7）：**
1. [ ] 补充 P1 模块测试
2. [ ] 编写集成测试
3. [ ] 达到 80% 覆盖率目标
4. [ ] 建立 CI 测试流程

### 7.4 测试优先级矩阵

```
           重要性
           高    中    低
        ┌─────────────────┐
     高 │ SecureStorage  │ 数据模型
  难    │ StrictHostKey  │ SSHConnection
  度    │ Repository     │ TerminalSession
        ├─────────────────┤
     低 │ SSHClientWrapper│ DI 模块
        │ 集成测试        │ Service
        └─────────────────┘
        
        优先完成左上象限
```

---

## 八、附录：测试模板

### 8.1 单元测试模板

```kotlin
package com.sshpad.app.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class SecureStorageTest {
    
    private lateinit var context: Context
    private lateinit var secureStorage: SecureStorage
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        secureStorage = SecureStorage(context)
    }
    
    @Test
    fun savePassword_validPassword_encryptsAndStores() {
        // Arrange
        val connectionId = "test-connection"
        val password = "test-password"
        
        // Act
        secureStorage.savePassword(connectionId, password)
        
        // Assert
        val retrieved = secureStorage.getPassword(connectionId)
        assertEquals(password, retrieved)
    }
    
    @Test
    fun getPassword_nonExistent_returnsNull() {
        // Arrange
        val connectionId = "non-existent"
        
        // Act
        val result = secureStorage.getPassword(connectionId)
        
        // Assert
        assertNull(result)
    }
}
```

### 8.2 测试清单

**测试前检查：**
- [ ] 测试依赖已添加
- [ ] 测试目录已创建
- [ ] 测试运行器已配置

**测试后检查：**
- [ ] 所有测试通过
- [ ] 覆盖率达到目标
- [ ] 测试命名规范
- [ ] 测试独立可重复
- [ ] 测试有明确断言

---

*报告人：都察院御史*  
*报告日期：2026-04-27 18:30*  
*版本：v1.0*  
*状态：❌ 待实施*  
*下次更新：待测试代码完成后*
