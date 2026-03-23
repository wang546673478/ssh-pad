# 设计资源包 v1.0

## 文档信息
- **版本**: v1.0
- **创建日期**: 2026-04-05
- **交付日期**: 2026-04-26
- **状态**: 🔄 准备中

---

## 1. 颜色资源 (Colors.kt)

```kotlin
// app/src/main/java/com/sshpark/design/theme/Color.kt

package com.sshpad.design.theme

import androidx.compose.ui.graphics.Color

// ========== Primary Colors ==========
val PrimaryBlue = Color(0xFF2563EB)
val SuccessGreen = Color(0xFF10B981)
val ErrorRed = Color(0xFFEF4444)

// ========== Dark Theme ==========
val DarkBackground = Color(0xFF0F0F0F)
val DarkSurface = Color(0xFF1E1E1E)
val DarkTerminalBg = Color(0xFF000000)
val DarkOnBackground = Color(0xFFF8F8F2)
val DarkOnSurface = Color(0xFFE5E5E5)
val DarkDisabledText = Color(0xFF6B7280)

// ========== Light Theme ==========
val LightBackground = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFF3F4F6)
val LightTerminalBg = Color(0xFF1E1E1E)
val LightOnBackground = Color(0xFF1F2937)
val LightOnSurface = Color(0xFF374151)
val LightDisabledText = Color(0xFF9CA3AF)

// ========== Terminal Colors (Dracula Theme) ==========
val TerminalBlack = Color(0xFF21222C)
val TerminalRed = Color(0xFFFF5555)
val TerminalGreen = Color(0xFF50FA7B)
val TerminalYellow = Color(0xFFF1FA8C)
val TerminalBlue = Color(0xFFBD93F9)
val TerminalMagenta = Color(0xFFFF79C6)
val TerminalCyan = Color(0xFF8BE9FD)
val TerminalWhite = Color(0xFFF8F8F2)

// ========== State Colors ==========
val ConnectedColor = SuccessGreen
val ConnectingColor = PrimaryBlue
val DisconnectedColor = ErrorRed
val SelectedBackground = PrimaryBlue.copy(alpha = 0.1f)
val HoverBackground = Color.White.copy(alpha = 0.05f)
```

---

## 2. 主题定义 (Theme.kt)

```kotlin
// app/src/main/java/com/sshpark/design/theme/Theme.kt

package com.sshpad.design.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    secondary = SuccessGreen,
    error = ErrorRed,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = SuccessGreen,
    error = ErrorRed,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onError = Color.White
)

@Composable
fun SSHPadTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

---

## 3. 字体系统 (Type.kt)

```kotlin
// app/src/main/java/com/sshpark/design/theme/Type.kt

package com.sshpad.design.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sshpad.R

// Font Families
val Roboto = FontFamily.Default
val NotoSansSC = FontFamily(
    Font(R.font.noto_sans_sc_regular, FontWeight.Normal),
    Font(R.font.noto_sans_sc_medium, FontWeight.Medium),
    Font(R.font.noto_sans_sc_bold, FontWeight.Bold)
)
val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// Terminal Font
val TerminalTypography = TextStyle(
    fontFamily = JetBrainsMono,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.sp
)
```

---

## 4. 间距系统 (Spacing.kt)

```kotlin
// app/src/main/java/com/sshpark/design/theme/Spacing.kt

package com.sshpad.design.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Spacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 24.dp
    val xxxl: Dp = 32.dp
    val page: Dp = 16.dp
    val listItem: Dp = 16.dp
    val card: Dp = 16.dp
    val button: Dp = 24.dp
}
```

---

## 5. 图标资源清单

### 5.1 Material Icons (使用官方库)

```kotlin
// 依赖：implementation "androidx.compose.material:material-icons-extended"

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*

// 图标列表
val Icons.Filled.Menu          // ≡ 汉堡菜单
val Icons.Filled.Settings      // ⚙️ 设置
val Icons.Filled.Add           // + 新建
val Icons.Filled.ArrowBack     // ← 返回
val Icons.Filled.MoreVert      // ⋮ 更多
val Icons.Filled.Keyboard      // ⌨️ 键盘
val Icons.Filled.ContentPaste  // 📋 剪贴板
val Icons.Filled.Person        // 👤 账户
val Icons.Filled.Palette       // 🎨 外观
val Icons.Filled.Lock          // 🔒 安全
val(Icons.Filled.Info)         // 📞 关于
val Icons.Filled.Check         // ✓ 确认
val Icons.Filled.Close         // ✕ 关闭
val Icons.Outlined.Cloud       // ☁️ 云同步
val Icons.Filled.Fingerprint   // 👆 生物识别
```

### 5.2 自定义图标 (SVG 文件)

```
app/src/main/res/drawable/
├── ic_connection_connected.svg     // 连接成功状态
├── ic_connection_disconnected.svg  // 连接断开状态
├── ic_server.svg                   // 服务器图标
├── ic_terminal.svg                 // 终端图标
├── ic_ssh.svg                      // SSH 图标
└── ic_empty_state.svg              // 空白状态插图
```

### 5.3 图标导出规格

```bash
# 使用 Figma 导出或 Android Studio Vector Asset
# 尺寸规范 (基准 24x24dp)

ic_launcher_foreground.svg    # 108x108dp
ic_notification.svg          # 24x24dp

# 多分辨率导出 (如使用 PNG)
drawable-mdpi/      # 1x (24x24px)
drawable-hdpi/      # 1.5x (36x36px)
drawable-xhdpi/     # 2x (48x48px)
drawable-xxhdpi/    # 3x (72x72px)
drawable-xxxhdpi/   # 4x (96x96px)
```

---

## 6. 字符串资源 (strings.xml)

```xml
<!-- app/src/main/res/values/strings.xml -->

<resources>
    <!-- App Name -->
    <string name="app_name">SSH Pad</string>
    
    <!-- Main Screen -->
    <string name="main_title">SSH Pad</string>
    <string name="connections_title">连接</string>
    <string name="add_connection">添加连接</string>
    <string name="no_connections">暂无连接</string>
    <string name="no_connections_hint">点击右上角 + 新建连接</string>
    <string name="sessions_count">%d 个会话</string>
    <string name="status_connected">已连接</string>
    <string name="status_connecting">连接中</string>
    <string name="status_disconnected">已断开</string>
    
    <!-- New Connection -->
    <string name="new_connection_title">新建连接</string>
    <string name="connection_name">名称</string>
    <string name="connection_name_hint">如：aws-prod</string>
    <string name="connection_host">主机</string>
    <string name="connection_host_hint">IP 地址或域名</string>
    <string name="connection_port">端口</string>
    <string name="connection_username">用户名</string>
    <string name="auth_method">认证方式</string>
    <string name="auth_password">密码</string>
    <string name="auth_key">密钥</string>
    <string name="password">密码</string>
    <string name="select_key_file">选择 PEM 文件</string>
    <string name="save_password">保存密码</string>
    <string name="cancel">取消</string>
    <string name="save_and_connect">保存并连接</string>
    
    <!-- Settings -->
    <string name="settings_title">设置</string>
    <string name="account_section">账户</string>
    <string name="cloud_sync">云同步</string>
    <string name="cloud_sync_off">离线</string>
    <string name="appearance_section">外观</string>
    <string name="theme">主题</string>
    <string name="theme_follow_system">跟随系统</string>
    <string name="theme_light">浅色</string>
    <string name="theme_dark">深色</string>
    <string name="terminal_theme">终端主题</string>
    <string name="font_size">字体大小</string>
    <string name="keyboard_section">键盘</string>
    <string name="keyboard_shortcuts">快捷键映射</string>
    <string name="virtual_keyboard_toolbar">虚拟键盘工具栏</string>
    <string name="security_section">安全</string>
    <string name="biometric_lock">生物识别锁</string>
    <string name="auto_lock">自动锁定</string>
    <string name="about_section">关于</string>
    <string name="version">版本 %s</string>
    
    <!-- Terminal -->
    <string name="terminal_toolbar_ctrl">Ctrl</string>
    <string name="terminal_toolbar_alt">Alt</string>
    <string name="terminal_toolbar_tab">Tab</string>
    <string name="terminal_toolbar_esc">ESC</string>
    <string name="terminal_copy">复制</string>
    <string name="terminal_paste">粘贴</string>
    
    <!-- Common -->
    <string name="ok">确定</string>
    <string name="delete">删除</string>
    <string name="edit">编辑</string>
    <string name="save">保存</string>
    <string name="loading">加载中…</string>
    <string name="error">错误</string>
    <string name="success">成功</string>
</resources>
```

---

## 7. 组件使用示例

### 7.1 连接列表项

```kotlin
@Composable
fun ConnectionListItem(
    name: String,
    host: String,
    isConnected: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .height(64.dp)
            .clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = if (isSelected) 
                PrimaryBlue.copy(alpha = 0.1f) else Color.Transparent
        ),
        leadingContent = {
            Box(
                modifier = Modifier.size(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = if (isConnected) SuccessGreen else Color.Gray,
                            shape = CircleShape
                        )
                )
            }
        },
        headlineContent = {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        supportingContent = {
            Text(
                text = host,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    )
}
```

### 7.2 终端工具栏按钮

```kotlin
@Composable
fun TerminalToolbarButton(
    text: String,
    onClick: () -> Unit
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
```

---

## 8. 字体文件清单

```
app/src/main/res/font/
├── noto_sans_sc_regular.ttf      # Noto Sans SC Regular
├── noto_sans_sc_medium.ttf       # Noto Sans SC Medium
├── noto_sans_sc_bold.ttf         # Noto Sans SC Bold
├── jetbrains_mono_regular.ttf    # JetBrains Mono Regular
└── jetbrains_mono_medium.ttf     # JetBrains Mono Medium
```

**字体下载链接:**
- Noto Sans SC: https://fonts.google.com/noto/specimen/Noto+Sans+SC
- JetBrains Mono: https://www.jetbrains.com/lp/mono/

---

## 9. 交付清单

| 文件 | 路径 | 状态 |
|------|------|------|
| Color.kt | design/theme/Color.kt | ✅ 已交付 |
| Theme.kt | design/theme/Theme.kt | ✅ 已交付 |
| Type.kt | design/theme/Type.kt | ✅ 已交付 |
| Spacing.kt | design/theme/Spacing.kt | ✅ 已交付 |
| strings.xml | design/strings.xml | ✅ 已交付 |
| icons/ | design/assets/icons/ | 🔄 准备中 |
| fonts/ | design/assets/fonts/ | 🔄 准备中 |

---

*资源版本：v1.0*
*创建时间：2026-04-05*
*负责人：UI/UX 设计师*
*状态：🔄 准备中*
