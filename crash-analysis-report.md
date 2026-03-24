# SSH Pad 加号按钮崩溃分析报告

**日期**: 2026-03-24  
**问题**: 用户点击加号按钮，整个程序闪退  
**测试状态**: ❌ 无法执行实际测试（无 Android 设备/模拟器）  
**分析方法**: 代码审查 + 静态分析

---

## 1. 崩溃原因分析

### 1.1 根本原因

**Dependency Injection 失败** - Koin 无法注入 `SavedStateHandle` 参数

### 1.2 详细分析

在 `AppNavigation.kt` 中，添加新连接的导航代码：

```kotlin
// AppNavigation.kt - Line 47-48
composable(route = Screen.ConnectionEdit.routeAdd) {
    val viewModel: ConnectionEditViewModel = getViewModel()  // ❌ 问题在这里
    ConnectionEditScreen(
        viewModel = viewModel,
        connectionId = null,
        ...
    )
}
```

在 `AppModule.kt` 中，`ConnectionEditViewModel` 的定义：

```kotlin
// AppModule.kt - Line 48-54
viewModel { (handle: SavedStateHandle) ->
    ConnectionEditViewModel(
        savedStateHandle = handle,
        getSSHConnectionsUseCase = get(),
        createSSHConnectionUseCase = get()
    ) 
}
```

**问题**：`ConnectionEditViewModel` 的 ViewModel 定义需要 `SavedStateHandle` 作为参数，但 `getViewModel()` 没有传递这个参数，导致 Koin 无法创建 ViewModel 实例，抛出 `MissingParameterException` 或类似的依赖注入异常。

### 1.3 预期崩溃日志

```
FATAL EXCEPTION: main
java.lang.RuntimeException: Unable to start activity ...
Caused by: org.koin.core.error.InstanceCreationException: Could not create instance for '[Single:...ConnectionEditViewModel]'
Caused by: org.koin.core.error.NoParameterFoundException: No parameter found for class 'SavedStateHandle'
```

---

## 2. 修复方案

### 方案 A：使用 `hiltViewModel()` 或正确的 Koin 参数传递（推荐）

修改 `AppNavigation.kt`，使用 Koin 的 `injectedParameters`：

```kotlin
// AppNavigation.kt
import org.koin.androidx.compose.inject

// Add mode: no connectionId
composable(route = Screen.ConnectionEdit.routeAdd) {
    val viewModel: ConnectionEditViewModel = inject { parametersOf(it) }
    ConnectionEditScreen(
        viewModel = viewModel,
        connectionId = null,
        onSave = { navController.popBackStack() },
        onCancel = { navController.popBackStack() }
    )
}
```

### 方案 B：简化 ViewModel 构造函数（更简单）

修改 `ConnectionEditViewModel`，不依赖 `SavedStateHandle`，直接使用 `MutableStateFlow` 管理表单状态：

```kotlin
// ConnectionEditViewModel.kt
class ConnectionEditViewModel(
    private val getSSHConnectionsUseCase: GetSSHConnectionsUseCase,
    private val createSSHConnectionUseCase: CreateSSHConnectionUseCase
) : ViewModel() {
    
    // 移除 SavedStateHandle 相关代码
    // 使用 UI State 管理表单数据
    
    private val _uiState = MutableStateFlow(ConnectionEditUiState())
    val uiState: StateFlow<ConnectionEditUiState> = _uiState.asStateFlow()
    
    // ... 其他代码保持不变
}
```

然后更新 `AppModule.kt`：

```kotlin
viewModel { 
    ConnectionEditViewModel(
        getSSHConnectionsUseCase = get(),
        createSSHConnectionUseCase = get()
    ) 
}
```

### 方案 C：使用 Navigation 的 SavedStateHandle

在 `AppNavigation.kt` 中正确传递 `SavedStateHandle`：

```kotlin
import androidx.lifecycle.SavedStateHandle
import org.koin.androidx.compose.getViewModel

composable(route = Screen.ConnectionEdit.routeAdd) { backStackEntry ->
    val handle = backStackEntry.savedStateHandle
    val viewModel: ConnectionEditViewModel = getViewModel { parametersOf(handle) }
    ConnectionEditScreen(
        viewModel = viewModel,
        connectionId = null,
        onSave = { navController.popBackStack() },
        onCancel = { navController.popBackStack() }
    )
}
```

---

## 3. 推荐修复（方案 C）

### 修改文件：`AppNavigation.kt`

```kotlin
// 添加模式：无 connectionId
composable(route = Screen.ConnectionEdit.routeAdd) { backStackEntry ->
    val handle = backStackEntry.savedStateHandle
    val viewModel: ConnectionEditViewModel = getViewModel { parametersOf(handle) }
    ConnectionEditScreen(
        viewModel = viewModel,
        connectionId = null,
        onSave = { navController.popBackStack() },
        onCancel = { navController.popBackStack() }
    )
}

// 编辑模式：带 connectionId
composable(
    route = Screen.ConnectionEdit.routeWithArgs,
    arguments = Screen.ConnectionEdit.arguments
) { backStackEntry ->
    val connectionId = backStackEntry.arguments?.getString("connectionId")
    val handle = backStackEntry.savedStateHandle
    val viewModel: ConnectionEditViewModel = getViewModel { parametersOf(handle) }
    ConnectionEditScreen(
        viewModel = viewModel,
        connectionId = connectionId,
        onSave = { navController.popBackStack() },
        onCancel = { navController.popBackStack() }
    )
}
```

**注意**：编辑模式也需要修复，使用相同的 `parametersOf(handle)` 语法。

---

## 4. 验证步骤

修复后，按以下步骤验证：

1. **安装 APK**：
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

2. **启动应用**：
   ```bash
   adb shell am start -n com.sshpad.app/.MainActivity
   ```

3. **点击加号按钮** - 应打开新建连接页面，不再崩溃

4. **检查日志**：
   ```bash
   adb logcat | grep -E "(SSH|FATAL|Exception)"
   ```

---

## 5. 附加发现

### 5.1 路由冲突风险

在 `AppNavigation.kt` 中存在两个相似的路由：
- `connection/edit` (添加模式)
- `connection/edit/{connectionId}` (编辑模式)

由于 `connectionId` 定义为 `nullable = true`，导航到 `"connection/edit"` 时可能匹配到带参数的路由，但传递 `null` 值。建议将添加模式路由改为：

```kotlin
sealed class Screen(val route: String) {
    object ConnectionEdit : Screen("connection/edit") {
        val routeAdd = "connection/add"  // 改为唯一路由
        val routeWithArgs = "connection/edit/{connectionId}"
        // ...
    }
}
```

### 5.2 测试环境缺失

当前环境没有可用的 Android 设备或模拟器：
- 没有物理设备连接
- 没有 Android 模拟器安装
- 建议安装 Android Studio 或使用 `avdmanager` 创建模拟器

---

## 6. 总结

| 项目 | 详情 |
|------|------|
| **崩溃类型** | Dependency Injection 失败 |
| **异常位置** | `ConnectionEditViewModel` 创建时 |
| **根本原因** | Koin 缺少 `SavedStateHandle` 参数 |
| **修复难度** | ⭐⭐ 简单（修改 1-2 个文件） |
| **建议方案** | 方案 C - 正确传递 `SavedStateHandle` |
| **预计修复时间** | 10-15 分钟 |

---

**下一步行动**：
1. 按方案 C 修改 `AppNavigation.kt`
2. 重新编译 APK
3. 在有设备的环境中测试
