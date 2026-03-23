# Android SSH 客户端技术方案

## 1. 技术栈选择

### 1.1 开发语言
**选择：Kotlin**

**理由：**
- Android 官方首选语言，与 Android SDK 无缝集成
- 协程（Coroutines）支持，简化异步 SSH 连接处理
- 空安全特性，减少运行时崩溃
- 比 Java 更简洁，开发效率更高
- 社区活跃，SSH 相关库的 Kotlin 支持良好

**备选：** 纯 Java（仅当团队 Kotlin 经验不足时考虑）

### 1.2 UI 框架
**选择：Jetpack Compose**

**理由：**
- 声明式 UI，代码更简洁可维护
- 与 Kotlin 深度集成，支持状态驱动
- 更好的平板和大屏适配能力（Material 3 Adaptive）
- 实时更新，无需 `findViewById` 等样板代码
- Google 官方长期支持方向

**备选：** XML + ViewBinding（仅当需要兼容 Android 5.0 以下设备）

### 1.3 SSH 库
**选择：Apache MINA sshd**

**对比分析：**

| 库 | 优点 | 缺点 | 推荐度 |
|---|---|---|---|
| **Apache MINA sshd** | 纯 Java/Kotlin、功能完整、活跃维护、支持 SFTP/SCP | 体积稍大 | ⭐⭐⭐⭐⭐ |
| JSch | 轻量、历史悠久 | 已停止维护、API 老旧 | ⭐⭐ |
| JNR-SSH | 基于 native、性能好 | 依赖 JNI、跨平台复杂 | ⭐⭐⭐ |
| SSHJ | 现代 API、活跃维护 | 文档较少 | ⭐⭐⭐⭐ |

**最终推荐：Apache MINA sshd**

```kotlin
// 依赖配置
dependencies {
    implementation("org.apache.sshd:sshd-core:2.12.0")
    implementation("org.apache.sshd:sshd-sftp:2.12.0")
}
```

### 1.4 终端模拟方案
**选择：JackPal Android Terminal Emulator + 自定义渲染**

**方案：**
- 基于开源项目 [JackPal's Terminal](https://github.com/jackpal/Android-Terminal-Emulator) 进行二次开发
- 自定义 TermView 支持：
  - ANSI 转义序列解析
  - VT100/VT220 命令支持
  - 256 色支持
  - 字体缩放
  - 触摸光标控制

**核心组件：**
```kotlin
class TerminalView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs) {
    private val termBuffer = TerminalBuffer()
    private val ansiParser = AnsiParser()
    
    // 渲染循环
    private val renderThread = RenderThread()
}
```

**备选：** 使用 [Termux:Float](https://github.com/termux/termux-float) 的终端库（更完整但体积大）

### 1.5 数据库
**选择：Room + SQLite**

**理由：**
- Android 官方推荐，Kotlin 协程支持
- 编译时 SQL 验证
- 支持数据迁移（Migration）
- 与 LiveData/Flow 无缝集成

**数据实体设计：**
```kotlin
@Entity(tableName = "ssh_connections")
data class SshConnection(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val host: String,
    val port: Int = 22,
    val username: String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val encryptedPassword: ByteArray?,
    val authType: AuthType, // PASSWORD, KEY, AGENT
    val privateKeyId: Long?,
    val createdAt: Long = System.currentTimeMillis(),
    val lastConnectedAt: Long? = null,
    val settingsJson: String = "{}"
)

@Entity(tableName = "ssh_keys")
data class SshKey(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val keyType: KeyType, // RSA, ED25519, ECDSA
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val encryptedKeyData: ByteArray,
    val fingerprint: String,
    val createdAt: Long = System.currentTimeMillis()
)
```

---

## 2. 架构设计

### 2.1 整体架构
**选择：Clean Architecture + MVVM**

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐  │
│  │   Screens   │  │  ViewModels │  │    Compose      │  │
│  │  (UI State) │  │  (Business  │  │   Components    │  │
│  │             │  │   Logic)    │  │                 │  │
│  └─────────────┘  └─────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                      Domain Layer                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐  │
│  │  Use Cases  │  │  Entities   │  │   Interfaces    │  │
│  │             │  │             │  │   (Repository)  │  │
│  └─────────────┘  └─────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                       Data Layer                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐  │
│  │ Repositories│  │    DAOs     │  │  SSH Services   │  │
│  │   (Impl)    │  │  (Room)     │  │  (MINA sshd)    │  │
│  └─────────────┘  └─────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### 2.2 模块划分

```
app/
├── :app                      # 主应用模块
├── :core                     # 核心模块
│   ├── :core:common          # 通用工具、扩展函数
│   ├── :core:ui              # 共享 UI 组件、主题
│   ├── :core:data            # 数据库、数据源
│   └── :core:network         # SSH 网络层封装
├── :feature                  # 功能模块
│   ├── :feature:connections  # 连接管理
│   ├── :feature:terminal     # 终端模拟
│   ├── :feature:file-transfer# 文件传输
│   ├── :feature:keys         # 密钥管理
│   └── :feature:settings     # 设置
└── :shared                   # 共享模块
    ├── :shared:ssh           # SSH 核心逻辑（可复用）
    └── :shared:terminal      # 终端模拟核心
```

### 2.3 数据流设计

```kotlin
// 使用 Kotlin Flow 进行响应式数据流
sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String, val retry: () -> Unit) : UiState<Nothing>
}

// ViewModel 数据流
class ConnectionViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository,
    private val sshService: SshService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState<List<Connection>>>(Loading)
    val uiState: StateFlow<UiState<List<Connection>>> = _uiState.asStateFlow()
    
    private val _terminalEvents = MutableSharedFlow<TerminalEvent>()
    val terminalEvents: SharedFlow<TerminalEvent> = _terminalEvents.asSharedFlow()
    
    fun connect(connectionId: Long) {
        viewModelScope.launch {
            _uiState.value = Loading
            sshService.connect(connectionId)
                .catch { e -> _uiState.value = Error(e.message, { connect(connectionId) }) }
                .collect { event ->
                    _terminalEvents.emit(event)
                }
        }
    }
}
```

---

## 3. 核心功能实现方案

### 3.1 SSH 连接管理

**连接管理器设计：**
```kotlin
interface SshConnectionManager {
    suspend fun connect(config: SshConfig): SshSession
    suspend fun disconnect(sessionId: String)
    suspend fun keepAlive(sessionId: String)
    fun getSession(sessionId: String): SshSession?
    fun getActiveSessions(): List<SshSession>
}

class SshConnectionManagerImpl @Inject constructor(
    private val sshClientFactory: SshClientFactory,
    private val sessionRepository: SessionRepository
) : SshConnectionManager {
    
    private val activeSessions = ConcurrentHashMap<String, SshSession>()
    private val keepAliveScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    override suspend fun connect(config: SshConfig): SshSession {
        val client = sshClientFactory.createClient(config)
        val session = client.connect()
        activeSessions[session.id] = session
        
        // 启动心跳
        startKeepAlive(session.id)
        
        return session
    }
    
    private fun startKeepAlive(sessionId: String) {
        keepAliveScope.launch {
            while (isActive && activeSessions.containsKey(sessionId)) {
                delay(30_000) // 30 秒心跳
                activeSessions[sessionId]?.sendKeepAlive()
            }
        }
    }
}
```

**连接配置：**
```kotlin
data class SshConfig(
    val host: String,
    val port: Int = 22,
    val username: String,
    val authMethod: AuthMethod,
    val timeout: Duration = Duration.ofSeconds(30),
    val keepAliveInterval: Duration = Duration.ofSeconds(30),
    val hostKeyVerification: HostKeyVerification = HostKeyVerification.STRICT,
    val compression: Boolean = false,
    val terminalType: String = "xterm-256color"
)

sealed interface AuthMethod {
    data class Password(val password: String) : AuthMethod
    data class PrivateKey(val keyId: Long) : AuthMethod
    object Agent : AuthMethod
}
```

### 3.2 终端模拟

**终端缓冲区：**
```kotlin
class TerminalBuffer(
    private val rows: Int = 24,
    private val cols: Int = 80
) {
    private val screen = Array(rows) { CharArray(cols) { ' ' } }
    private val attributes = Array(rows) { Array(cols) { CharAttribute() } }
    
    var cursorX = 0
    var cursorY = 0
    
    fun write(char: Char) {
        when (char) {
            '\n' -> moveCursorLineDown()
            '\r' -> moveCursorToColumn(0)
            '\b' -> moveCursorLeft()
            else -> {
                screen[cursorY][cursorX] = char
                moveCursorRight()
            }
        }
    }
    
    fun processAnsiSequence(sequence: String) {
        // 解析 ANSI 转义序列
        // ESC[<params><command>
    }
}
```

**渲染引擎：**
```kotlin
class TerminalRenderer @Inject constructor(
    private val buffer: TerminalBuffer,
    private val textStyle: TextStyle
) {
    fun render(canvas: Canvas) {
        canvas.drawColor(Color.BLACK)
        
        for (row in 0 until buffer.rows) {
            for (col in 0 until buffer.cols) {
                val char = buffer.screen[row][col]
                val attr = buffer.attributes[row][col]
                
                val paint = Paint().apply {
                    color = attr.foregroundColor
                    backgroundColor = attr.backgroundColor
                    textSize = textStyle.size
                    typeface = textStyle.typeface
                }
                
                canvas.drawText(char.toString(), col * textStyle.charWidth, 
                    row * textStyle.lineHeight, paint)
            }
        }
        
        // 绘制光标
        drawCursor(canvas)
    }
}
```

**输入处理：**
```kotlin
class TerminalInputHandler @Inject constructor(
    private val sshSession: SshSession
) : GestureDetector.OnGestureListener {
    
    override fun onDown(e: MotionEvent): Boolean = true
    
    override fun onSingleTapUp(e: MotionEvent): Boolean {
        // 显示虚拟光标
        showCursorSelector(e.x, e.y)
        return true
    }
    
    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        // 滚动终端缓冲区
        buffer.scroll(distanceY.toInt())
        return true
    }
    
    fun handleKeyEvent(event: KeyEvent): Boolean {
        val keySequence = when (event.keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> "\u001b[A"  // 上箭头
            KeyEvent.KEYCODE_DPAD_DOWN -> "\u001b[B"  // 下箭头
            KeyEvent.KEYCODE_DPAD_RIGHT -> "\u001b[C" // 右箭头
            KeyEvent.KEYCODE_DPAD_LEFT -> "\u001b[D"  // 左箭头
            KeyEvent.KEYCODE_TAB -> "\t"
            KeyEvent.KEYCODE_ENTER -> "\r"
            KeyEvent.KEYCODE_DEL -> "\u007f"
            // ... 更多特殊键
            else -> event.unicodeChar.toChar().toString()
        }
        
        sshSession.send(keySequence)
        return true
    }
}
```

### 3.3 文件传输（SFTP/SCP）

**SFTP 服务封装：**
```kotlin
interface SftpClient {
    suspend fun listRemoteDirectory(path: String): List<SftpFile>
    suspend fun downloadFile(remotePath: String, localPath: String, progress: (Long, Long) -> Unit)
    suspend fun uploadFile(localPath: String, remotePath: String, progress: (Long, Long) -> Unit)
    suspend fun deleteFile(remotePath: String)
    suspend fun createDirectory(remotePath: String)
    suspend fun renameFile(oldPath: String, newPath: String)
}

class SftpClientImpl @Inject constructor(
    private val sshSession: SshSession
) : SftpClient {
    
    private val sftpChannel: SftpClient = sshSession.openSftpClient()
    
    override suspend fun listRemoteDirectory(path: String): List<SftpFile> = withContext(Dispatchers.IO) {
        sftpChannel.ls(path).map { entry ->
            SftpFile(
                name = entry.filename,
                path = entry.path,
                size = entry.attrs.size,
                isDirectory = entry.attrs.isDirectory,
                modifiedTime = entry.attrs.mtime,
                permissions = entry.attrs.permissions
            )
        }
    }
    
    override suspend fun downloadFile(
        remotePath: String,
        localPath: String,
        progress: (Long, Long) -> Unit
    ) = withContext(Dispatchers.IO) {
        val remoteFile = sftpChannel.read(remotePath)
        val localFile = File(localPath)
        
        val totalSize = remoteFile.attrs.size
        var downloadedSize = 0L
        
        localFile.outputStream().use { output ->
            remoteFile.use { input ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    downloadedSize += bytesRead
                    progress(downloadedSize, totalSize)
                }
            }
        }
    }
}
```

**文件传输 UI：**
```kotlin
@Composable
fun FileTransferScreen(viewModel: FileTransferViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column {
        // 本地文件浏览器
        LocalFileBrowser(
            currentPath = viewModel.localPath,
            onNavigate = { viewModel.navigateLocal(it) },
            onSelectFile = { viewModel.selectLocalFile(it) }
        )
        
        // 传输进度
        if (viewModel.isTransferring) {
            TransferProgressBar(
                fileName = viewModel.currentFile,
                progress = viewModel.transferProgress,
                speed = viewModel.transferSpeed,
                onCancel = { viewModel.cancelTransfer() }
            )
        }
        
        // 远程文件浏览器
        RemoteFileBrowser(
            currentPath = viewModel.remotePath,
            onNavigate = { viewModel.navigateRemote(it) },
            onSelectFile = { viewModel.selectRemoteFile(it) }
        )
        
        // 操作按钮
        Row {
            Button(onClick = { viewModel.upload() }) { Text("上传") }
            Button(onClick = { viewModel.download() }) { Text("下载") }
        }
    }
}
```

### 3.4 密钥管理

**密钥存储方案：**
```kotlin
interface KeyStoreManager {
    suspend fun generateKey(type: KeyType, bits: Int, passphrase: String?): SshKey
    suspend fun importKey(name: String, keyData: String, passphrase: String?): SshKey
    suspend fun exportKey(keyId: Long, passphrase: String): String
    suspend fun deleteKey(keyId: Long)
    suspend fun getKey(keyId: Long): SshKey?
    suspend fun getKeys(): List<SshKey>
}

class KeyStoreManagerImpl @Inject constructor(
    private val keyDao: KeyDao,
    private val androidKeyStore: android.security.keystore.KeyStore
) : KeyStoreManager {
    
    override suspend fun generateKey(
        type: KeyType,
        bits: Int,
        passphrase: String?
    ): SshKey = withContext(Dispatchers.IO) {
        // 生成密钥对
        val keyPairGenerator = KeyPairGenerator.getInstance(type.algorithm, "AndroidKeyStore")
        keyPairGenerator.initialize(
            KeyGenParameterSpec.Builder(bits, KeyProperties.PURPOSE_SIGN)
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setUserAuthenticationRequired(false)
                .build()
        )
        val keyPair = keyPairGenerator.generateKeyPair()
        
        // 转换为 SSH 格式
        val sshKeyData = convertToSshFormat(keyPair, type)
        
        // 加密存储
        val encryptedData = if (passphrase != null) {
            encryptWithPassphrase(sshKeyData, passphrase)
        } else {
            encryptWithAndroidKeyStore(sshKeyData)
        }
        
        val key = SshKey(
            name = "Generated_${type.name}_${System.currentTimeMillis()}",
            keyType = type,
            encryptedKeyData = encryptedData,
            fingerprint = calculateFingerprint(sshKeyData)
        )
        
        keyDao.insert(key)
        key
    }
    
    private fun encryptWithAndroidKeyStore(data: ByteArray): ByteArray {
        // 使用 Android KeyStore 进行硬件级加密
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateAndroidKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(data)
        return iv + encrypted // IV + ciphertext
    }
}
```

**密钥类型支持：**
```kotlin
enum class KeyType(val algorithm: String, val recommended: Boolean) {
    RSA("RSA", false),           // 兼容性好，但逐渐淘汰
    ED25519("Ed25519", true),    // 推荐，安全高效
    ECDSA("EC", true)            // 推荐，NIST P-256
}
```

### 3.5 会话保持

**后台服务：**
```kotlin
@HiltAndroidApp
class SshService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val binder = LocalBinder()
    
    inner class LocalBinder : Binder() {
        fun getService(): SshService = this@SshService
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    fun startSession(connectionId: Long) {
        serviceScope.launch {
            val session = connectionManager.connect(connectionId)
            activeSessions[connectionId] = session
            
            // 创建前台通知
            startForeground(NOTIFICATION_ID, createNotification(connectionId))
        }
    }
    
    private fun createNotification(connectionId: Long): Notification {
        return NotificationCompat.Builder(this, SSH_CHANNEL_ID)
            .setContentTitle("SSH 连接中")
            .setContentText(getConnectionName(connectionId))
            .setSmallIcon(R.drawable.ic_ssh)
            .setOngoing(true)
            .addAction(R.drawable.ic_disconnect, "断开", createDisconnectIntent(connectionId))
            .build()
    }
}
```

**WakeLock 管理：**
```kotlin
class ConnectionWakeLock @Inject constructor(
    private val context: Context
) {
    private val wakeLock: PowerManager.WakeLock by lazy {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "ssh:connection:wakelock"
        ).apply {
            setReferenceCounted(false)
        }
    }
    
    fun acquire(timeout: Long = 30 * 60 * 1000L) { // 30 分钟
        if (!wakeLock.isHeld) {
            wakeLock.acquire(timeout)
        }
    }
    
    fun release() {
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }
}
```

**网络变化处理：**
```kotlin
class NetworkMonitor @Inject constructor(
    private val context: Context,
    private val connectionManager: SshConnectionManager
) : ConnectivityManager.NetworkCallback() {
    
    fun register() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(this)
    }
    
    override fun onLost(network: Network) {
        // 网络断开，标记所有会话需要重连
        connectionManager.markAllSessionsForReconnect()
    }
    
    override fun onAvailable(network: Network) {
        // 网络恢复，尝试重连
        connectionManager.attemptReconnect()
    }
}
```

---

## 4. 平板适配方案

### 4.1 大屏优化

**Material 3 Adaptive Layout：**
```kotlin
@Composable
fun SshAppLayout(
    windowSizeClass: WindowSizeClass,
    navController: NavHostController
) {
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // 手机布局：单栏
            PhoneLayout(navController = navController)
        }
        WindowWidthSizeClass.Medium, WindowWidthSizeClass.Expanded -> {
            // 平板布局：双栏
            TabletLayout(navController = navController)
        }
    }
}

@Composable
fun TabletLayout(navController: NavHostController) {
    Row {
        // 左侧：连接列表（固定）
        NavigationPane(
            modifier = Modifier
                .fillMaxHeight()
                .width(320.dp),
            navController = navController
        )
        
        // 右侧：终端内容（可切换）
        DetailPane(
            modifier = Modifier.weight(1f),
            navController = navController
        )
    }
}
```

**响应式布局计算：**
```kotlin
@Composable
fun calculateWindowSizeClass(activity: Activity): WindowSizeClass {
    val windowSizeCalculator = WindowSizeCalculator(activity)
    return windowSizeCalculator.calculateWindowSizeClass()
}

data class WindowSizeClass(
    val widthSizeClass: WindowWidthSizeClass,
    val heightSizeClass: WindowHeightSizeClass
)

enum class WindowWidthSizeClass { Compact, Medium, Expanded }
enum class WindowHeightSizeClass { Compact, Medium, Expanded }
```

### 4.2 分屏支持

**多窗口支持：**
```kotlin
class SshMainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 监听窗口配置变化
        val metrics = windowManager.currentWindowMetrics
        setupMultiWindowSupport(metrics)
    }
    
    private fun setupMultiWindowSupport(metrics: WindowMetrics) {
        val bounds = metrics.bounds
        val isInMultiWindowMode = resources.configuration.windowConfiguration
            ?.let { it.isInMultiWindowMode } ?: false
        
        if (isInMultiWindowMode) {
            // 分屏模式：优化布局
            enableCompactMode()
        }
    }
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        
        // 处理分屏/折叠屏状态变化
        if (newConfig.smallestScreenWidthDp >= 600) {
            // 切换到平板布局
        }
    }
}
```

**多实例支持：**
```xml
<!-- AndroidManifest.xml -->
<activity
    android:name=".MainActivity"
    android:configChanges="orientation|screenSize|screenLayout|smallestScreenSize"
    android:resizeableActivity="true"
    android:supportsPictureInPicture="true"
    android:launchMode="standard">
    
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
    
    <!-- 支持多窗口元数据 -->
    <meta-data
        android:name="android.supports_size_changes"
        android:value="true" />
</activity>
```

### 4.3 键盘快捷键

**物理键盘支持：**
```kotlin
class KeyboardShortcutHandler @Inject constructor() {
    
    fun handleShortcut(keyEvent: KeyEvent): Boolean {
        val isCtrlPressed = keyEvent.isCtrlPressed
        val isAltPressed = keyEvent.isAltPressed
        
        return when {
            isCtrlPressed && keyEvent.keyCode == KeyEvent.KEYCODE_C -> {
                // Ctrl+C: 中断
                sendEscapeSequence("\u0003")
                true
            }
            isCtrlPressed && keyEvent.keyCode == KeyEvent.KEYCODE_Z -> {
                // Ctrl+Z: 挂起
                sendEscapeSequence("\u001a")
                true
            }
            isCtrlPressed && keyEvent.keyCode == KeyEvent.KEYCODE_L -> {
                // Ctrl+L: 清屏
                sendEscapeSequence("\u000c")
                true
            }
            isAltPressed && keyEvent.keyCode == KeyEvent.KEYCODE_1 -> {
                // Alt+1: 切换到会话 1
                switchSession(1)
                true
            }
            keyEvent.keyCode == KeyEvent.KEYCODE_F1 -> {
                // F1: 显示帮助
                showHelpOverlay()
                true
            }
            keyEvent.keyCode == KeyEvent.KEYCODE_F11 -> {
                // F11: 全屏切换
                toggleFullScreen()
                true
            }
            else -> false
        }
    }
}

// Compose 快捷键处理
@Composable
fun TerminalWithShortcuts(
    modifier: Modifier = Modifier,
    onShortcut: (KeyboardShortcut) -> Unit
) {
    Box(
        modifier = modifier
            .focusable()
            .onKeyEvent { keyEvent ->
                val shortcut = parseShortcut(keyEvent)
                if (shortcut != null) {
                    onShortcut(shortcut)
                    true
                } else {
                    false
                }
            }
    ) {
        // 终端内容
    }
}
```

**快捷键映射表：**
```kotlin
object KeyboardShortcuts {
    val defaultShortcuts = mapOf(
        "Ctrl+C" to ShortcutAction.INTERRUPT,
        "Ctrl+Z" to ShortcutAction.SUSPEND,
        "Ctrl+D" to ShortcutAction.EOF,
        "Ctrl+L" to ShortcutAction.CLEAR,
        "Ctrl+A" to ShortcutAction.HOME,
        "Ctrl+E" to ShortcutAction.END,
        "Ctrl+U" to ShortcutAction.CLEAR_LINE,
        "Ctrl+K" to ShortcutAction.CLEAR_TO_END,
        "Ctrl+W" to ShortcutAction.DELETE_WORD,
        "Ctrl+R" to ShortcutAction.SEARCH_HISTORY,
        "Tab" to ShortcutAction.COMPLETE,
        "Shift+Tab" to ShortcutAction.COMPLETE_REVERSE,
        "Alt+B" to ShortcutAction.WORD_BACKWARD,
        "Alt+F" to ShortcutAction.WORD_FORWARD,
        "F1" to ShortcutAction.HELP,
        "F11" to ShortcutAction.TOGGLE_FULLSCREEN,
        "Ctrl+Shift+T" to ShortcutAction.NEW_TAB,
        "Ctrl+W" to ShortcutAction.CLOSE_TAB,
        "Ctrl+Tab" to ShortcutAction.NEXT_TAB,
        "Ctrl+Shift+Tab" to ShortcutAction.PREVIOUS_TAB,
    )
}
```

### 4.4 触控优化

**触控手势：**
```kotlin
@Composable
fun TerminalWithGestures(
    modifier: Modifier = Modifier,
    onScroll: (Float, Float) -> Unit,
    onScale: (Float) -> Unit,
    onDoubleTap: () -> Unit
) {
    val scrollableState = rememberScrollableState { delta ->
        onScroll(0f, -delta)
        delta
    }
    
    val gestureState = remember {
        detectTransformGestures { _, _, scale, _ ->
            onScale(scale)
        }
    }
    
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onDoubleTap() }
                )
            }
            .pointerInput(Unit) { gestureState }
            .scrollable(
                orientation = Orientation.Vertical,
                state = scrollableState
            )
    ) {
        // 终端内容
    }
}
```

**虚拟键盘工具栏：**
```kotlin
@Composable
fun TerminalKeyboardToolbar(
    onSpecialKey: (SpecialKey) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color.DarkGray),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(onClick = { onSpecialKey(SpecialKey.CTRL) }) {
            Text("Ctrl", color = Color.White)
        }
        IconButton(onClick = { onSpecialKey(SpecialKey.ALT) }) {
            Text("Alt", color = Color.White)
        }
        IconButton(onClick = { onSpecialKey(SpecialKey.ESC) }) {
            Text("Esc", color = Color.White)
        }
        IconButton(onClick = { onSpecialKey(SpecialKey.TAB) }) {
            Text("Tab", color = Color.White)
        }
        IconButton(onClick = { onSpecialKey(SpecialKey.ARROW_UP) }) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "上", tint = Color.White)
        }
        IconButton(onClick = { onSpecialKey(SpecialKey.ARROW_DOWN) }) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "下", tint = Color.White)
        }
        IconButton(onClick = { onSpecialKey(SpecialKey.ARROW_LEFT) }) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "左", tint = Color.White)
        }
        IconButton(onClick = { onSpecialKey(SpecialKey.ARROW_RIGHT) }) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "右", tint = Color.White)
        }
    }
}
```

**触控光标控制：**
```kotlin
@Composable
fun TerminalWithCursorSelector(
    modifier: Modifier = Modifier
) {
    var showCursorSelector by remember { mutableStateOf(false) }
    var selectorPosition by remember { mutableStateOf(Offset.Zero) }
    
    Box(modifier = modifier) {
        TerminalView(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            selectorPosition = offset
                            showCursorSelector = true
                        }
                    )
                }
        )
        
        if (showCursorSelector) {
            CursorSelector(
                position = selectorPosition,
                onDismiss = { showCursorSelector = false },
                onMoveCursor = { dx, dy ->
                    // 移动光标
                }
            )
        }
    }
}
```

---

## 5. 技术风险评估

### 5.1 可能的技术难点

| 风险 | 等级 | 描述 | 影响 |
|---|---|---|---|
| **终端 ANSI 兼容性** | 🔴 高 | 完整支持 VT100/VT220/ANSI 复杂序列 | 终端显示异常、命令乱码 |
| **SSH 连接稳定性** | 🔴 高 | 网络切换、后台保活、断线重连 | 连接频繁断开 |
| **大文件传输性能** | 🟡 中 | SFTP 大文件传输内存占用 | OOM、传输中断 |
| **密钥安全存储** | 🔴 高 | Android KeyStore 兼容性差异 | 密钥泄露风险 |
| **平板布局适配** | 🟡 中 | 多种屏幕尺寸、分屏、折叠屏 | UI 错乱 |
| **输入法兼容性** | 🟡 中 | 第三方输入法特殊键处理 | 输入异常 |
| **后台服务保活** | 🔴 高 | 各厂商 ROM 后台限制 | 服务被杀 |

### 5.2 备选方案

#### 5.2.1 终端模拟备选

**方案 A：使用 Termux 终端库**
```kotlin
// 优点：功能完整、社区活跃
// 缺点：体积大（~5MB）、依赖多
implementation("com.termux:termux-shared:0.118.0")
```

**方案 B：使用 VTE 封装**
```kotlin
// 优点：GNOME 终端核心、兼容性好
// 缺点：需要 JNI、跨平台复杂
```

**推荐：** 方案 A（Termux）作为备选，主方案仍为自研轻量终端

#### 5.2.2 SSH 库备选

**方案 A：SSHJ**
```kotlin
// 优点：现代 API、活跃维护
// 缺点：文档少、社区小
implementation("com.hierynomus:sshj:0.38.0")
```

**方案 B：封装原生 OpenSSH**
```kotlin
// 优点：100% 兼容
// 缺点：需要 NDK、体积大、安全审查复杂
```

**推荐：** SSHJ 作为备选

#### 5.2.3 后台保活备选

**方案 A：WorkManager + Foreground Service**
```kotlin
val workRequest = OneTimeWorkRequestBuilder<SshKeepAliveWorker>()
    .setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    )
    .build()
WorkManager.getInstance(context).enqueue(workRequest)
```

**方案 B：使用各厂商白名单**
```kotlin
// 针对小米、华为、OPPO 等厂商的特殊保活 API
// 需要单独适配
```

**推荐：** 方案 A 为主，方案 B 作为补充

#### 5.2.4 密钥安全备选

**方案 A：纯软件加密 + 用户密码**
```kotlin
// 使用 Argon2 派生密钥
// 优点：跨设备兼容
// 缺点：安全性低于硬件加密
```

**方案 B：Android KeyStore + Biometric**
```kotlin
// 结合生物识别
val biometricPrompt = BiometricPrompt(activity, callback)
```

**推荐：** 方案 B 为主，方案 A 作为导出/导入兼容

### 5.3 风险缓解措施

```kotlin
// 1. 终端 ANSI 测试套件
class AnsiCompatibilityTest {
    @Test
    fun testAllAnsiSequences() {
        // 覆盖所有常用 ANSI 序列
    }
}

// 2. 连接健康检查
class ConnectionHealthMonitor {
    fun monitor(session: SshSession) {
        // 定期发送心跳
        // 检测网络状态
        // 自动重连
    }
}

// 3. 内存管理
class MemoryManager {
    fun limitBufferSize(maxSize: Int) {
        // 限制终端缓冲区大小
        // 分页加载历史记录
    }
}

// 4. 安全审计
class SecurityAudit {
    fun auditKeyStorage() {
        // 定期审计密钥存储
        // 检测异常访问
    }
}
```

---

## 6. 开发里程碑

### Phase 1：核心功能（4 周）
- [ ] 项目搭建、架构设计
- [ ] SSH 连接基础功能
- [ ] 终端模拟基础（ANSI 解析）
- [ ] 本地数据库设计

### Phase 2：功能完善（4 周）
- [ ] SFTP 文件传输
- [ ] 密钥管理
- [ ] 会话保持
- [ ] 后台服务

### Phase 3：平板适配（2 周）
- [ ] Material 3 Adaptive
- [ ] 键盘快捷键
- [ ] 触控优化
- [ ] 分屏支持

### Phase 4：测试优化（2 周）
- [ ] 单元测试、集成测试
- [ ] 性能优化
- [ ] 安全审计
- [ ] 发布准备

---

## 7. 技术决策总结

| 决策点 | 选择 | 理由 |
|---|---|---|
| 开发语言 | Kotlin | 官方首选、协程支持 |
| UI 框架 | Jetpack Compose | 声明式、平板友好 |
| SSH 库 | Apache MINA sshd | 功能完整、活跃维护 |
| 终端模拟 | 自研 + JackPal 参考 | 轻量可控 |
| 数据库 | Room | 官方推荐、协程支持 |
| 架构 | Clean + MVVM | 分层清晰、可测试 |
| 依赖注入 | Hilt | Android 官方、Composable 支持 |

---

**方案版本：** v1.0  
**制定日期：** 2026-03-23  
**制定者：** 兵部尚书
