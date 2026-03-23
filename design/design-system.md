# 设计系统规范 v1.0

## 文档信息
- **版本**: v1.0
- **创建日期**: 2026-04-05
- **适用项目**: Android SSH Client (SSH Pad)
- **UI 框架**: Jetpack Compose + Material Design 3

---

## 1. 色彩系统

### 1.1 主色调 (Primary Colors)

```kotlin
// Material 3 Color Scheme
val Blue = Color(0xFF2563EB)        // Primary - 主色调
val Green = Color(0xFF10B981)       // Secondary - 成功状态
val Red = Color(0xFFEF4444)         // Error - 错误状态
```

| 颜色名称 | Hex | 用途 |
|----------|-----|------|
| Primary Blue | #2563EB | 主按钮、链接、选中状态 |
| Success Green | #10B981 | 连接成功、执行成功 |
| Error Red | #EF4444 | 连接失败、错误提示 |

### 1.2 深色主题 (Dark Theme)

```kotlin
// 深色主题背景
val Background = Color(0xFF0F0F0F)   // 主背景
val Surface = Color(0xFF1E1E1E)      // 卡片、对话框
val TerminalBg = Color(0xFF000000)   // 终端背景（纯黑 for OLED）

// 文本颜色
val OnBackground = Color(0xFFF8F8F2)  // 主文本
val OnSurface = Color(0xFFE5E5E5)     // 卡片文本
val DisabledText = Color(0xFF6B7280)  // 禁用文本
```

| 颜色名称 | Hex | 用途 |
|----------|-----|------|
| Background | #0F0F0F | 应用主背景 |
| Surface | #1E1E1E | 卡片、列表项、对话框 |
| Terminal Background | #000000 | 终端模拟器背景 |
| On Background | #F8F8F2 | 主文本颜色 |
| On Surface | #E5E5E5 | 卡片上文本 |
| Disabled | #6B7280 | 禁用状态文本 |

### 1.3 浅色主题 (Light Theme)

```kotlin
// 浅色主题背景
val Background = Color(0xFFFFFFFF)   // 主背景
val Surface = Color(0xFFF3F4F6)      // 卡片、对话框
val TerminalBg = Color(0xFF1E1E1E)   // 终端背景（深色）

// 文本颜色
val OnBackground = Color(0xFF1F2937)  // 主文本
val OnSurface = Color(0xFF374151)     // 卡片文本
val DisabledText = Color(0xFF9CA3AF)  // 禁用文本
```

### 1.4 终端配色 (Dracula 主题)

```kotlin
// 终端 ANSI 颜色
val TerminalBlack = Color(0xFF21222C)
val TerminalRed = Color(0xFFFF5555)
val TerminalGreen = Color(0xFF50FA7B)
val TerminalYellow = Color(0xFFF1FA8C)
val TerminalBlue = Color(0xFFBD93F9)
val TerminalMagenta = Color(0xFFFF79C6)
val TerminalCyan = Color(0xFF8BE9FD)
val TerminalWhite = Color(0xFFF8F8F2)
```

| 颜色 | Hex | ANSI | 用途 |
|------|-----|------|------|
| Black | #21222C | 0 | 普通文本 |
| Red | #FF5555 | 1 | 错误、删除操作 |
| Green | #50FA7B | 2 | 成功、执行、添加 |
| Yellow | #F1FA8C | 3 | 警告、注意 |
| Blue | #BD93F9 | 4 | 链接、路径 |
| Magenta | #FF79C6 | 5 | 关键字、变量 |
| Cyan | #8BE9FD | 6 | 提示、信息 |
| White | #F8F8F2 | 7 | 高亮、强调 |

---

## 2. 字体系统

### 2.1 字体家族

```kotlin
// 字体定义
val Roboto = FontFamily.Default  // 英文默认字体
val NotoSansSC = FontFamily(...) // 中文字体 (Noto Sans SC)
val JetBrainsMono = FontFamily(...) // 终端等宽字体
```

| 字体 | 用途 | 字重 |
|------|------|------|
| Roboto | 英文界面文本 | Regular, Medium, Bold |
| Noto Sans SC | 中文界面文本 | Regular (400), Medium (500), Bold (700) |
| JetBrains Mono | 终端内容、代码 | Regular, Medium |

### 2.2 字体大小

```kotlin
// Typography (Material 3)
val displayLarge = TextStyle(fontSize = 57.sp, fontWeight = FontWeight.Bold)
val displayMedium = TextStyle(fontSize = 45.sp, fontWeight = FontWeight.Bold)
val displaySmall = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold)

val headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.SemiBold)
val headlineMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
val headlineSmall = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold)

val titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
val titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
val titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

val bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Regular)
val bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Regular)
val bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Regular)

val labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
val labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)
val labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium)

// 终端专用
val terminalFont = TextStyle(
    fontSize = 14.sp,
    fontFamily = JetBrainsMono,
    lineHeight = 20.sp
)
```

| 样式名 | 字号 | 字重 | 行高 | 用途 |
|--------|------|------|------|------|
| displayLarge | 57sp | Bold | 64sp | 超大标题 |
| headlineLarge | 32sp | SemiBold | 40sp | 页面标题 |
| headlineSmall | 24sp | SemiBold | 32sp | 章节标题 |
| titleLarge | 22sp | SemiBold | 28sp | 卡片标题 |
| titleMedium | 16sp | SemiBold | 24sp | 列表标题 |
| bodyLarge | 16sp | Regular | 24sp | 正文 |
| bodyMedium | 14sp | Regular | 20sp | 辅助文本 |
| labelLarge | 14sp | Medium | 20sp | 按钮文本 |
| terminal | 14sp | Regular | 20sp | 终端内容 |

### 2.3 终端字体配置

```kotlin
// 可调节字体大小范围
val terminalFontSizeRange = 10.sp..24.sp
val terminalFontSizeStep = 2.sp
```

---

## 3. 间距系统

### 3.1 4px 网格

```kotlin
// 标准间距
val spacing4 = 4.dp
val spacing8 = 8.dp
val spacing12 = 12.dp
val spacing16 = 16.dp
val spacing20 = 20.dp
val spacing24 = 24.dp
val spacing32 = 32.dp
val spacing40 = 40.dp
val spacing48 = 48.dp
val spacing64 = 64.dp
```

| 间距 | DP | 用途 |
|------|----|------|
| xs | 4dp | 图标和文本间距、内边距 |
| sm | 8dp | 列表项内边距、元素间距 |
| md | 12dp | 卡片内边距 |
| lg | 16dp | 页面内边距、组件间距 |
| xl | 24dp | 大组件间距 |
| 2xl | 32dp | 页面边距、大组件 |

### 3.2 页面边距

```kotlin
// 页面布局
val pagePadding = 16.dp           // 页面左右边距
val pageTopPadding = 24.dp        // 页面顶部边距
val pageBottomPadding = 32.dp     // 页面底部边距
```

### 3.3 组件间距

```kotlin
// 列表项
val listItemPadding = 16.dp       // 列表项左右内边距
val listItemVerticalPadding = 12.dp // 列表项上下内边距

// 卡片
val cardPadding = 16.dp           // 卡片内边距
val cardElevation = 2.dp          // 卡片阴影 (深色主题可为 0)

// 按钮
val buttonHorizontalPadding = 24.dp
val buttonVerticalPadding = 12.dp
val buttonMinHeight = 48.dp       // Material 最小点击区域
```

---

## 4. 圆角系统

```kotlin
// 圆角半径
val cornerXS = 4.dp    // 小标签、图标
val cornerSM = 8.dp    // 按钮、输入框
val cornerMD = 12.dp   // 卡片
val cornerLG = 16.dp   // 大卡片、对话框
val cornerXL = 24.dp   // 特大卡片
val cornerFull = 9999.dp // 圆形（头像、开关）
```

| 圆角 | DP | 用途 |
|------|----|------|
| xs | 4dp | 小标签、徽章 |
| sm | 8dp | 按钮、输入框、小图标 |
| md | 12dp | 卡片、列表项 |
| lg | 16dp | 对话框、底部弹窗 |
| xl | 24dp | 大卡片、封面图 |
| full | 9999dp | 圆形头像、开关、圆形按钮 |

---

## 5. 阴影系统

### 5.1 深色主题阴影

```kotlin
// 深色主题使用更微妙的阴影
val elevation1 = BoxShadow(
    color = Color.Black.copy(alpha = 0.2f),
    blurRadius = 4.dp,
    offset = Offset(0f, 2f)
)
val elevation2 = BoxShadow(
    color = Color.Black.copy(alpha = 0.3f),
    blurRadius = 8.dp,
    offset = Offset(0f, 4f)
)
```

### 5.2 浅色主题阴影

```kotlin
// 浅色主题使用更明显的阴影
val elevation1 = BoxShadow(
    color = Color.Black.copy(alpha = 0.05f),
    blurRadius = 4.dp,
    offset = Offset(0f, 2f)
)
val elevation2 = BoxShadow(
    color = Color.Black.copy(alpha = 0.1f),
    blurRadius = 8.dp,
    offset = Offset(0f, 4f)
)
```

---

## 6. 组件规范

### 6.1 按钮

#### 主按钮 (Filled Button)
```kotlin
// 样式
height = 48.dp
minWidth = 120.dp
cornerRadius = 8.dp
backgroundColor = Primary Blue
contentColor = White
```

#### 次要按钮 (Outlined Button)
```kotlin
// 样式
height = 48.dp
cornerRadius = 8.dp
borderColor = Primary Blue (alpha 0.5)
contentColor = Primary Blue
```

#### 文本按钮 (Text Button)
```kotlin
// 样式
height = 40.dp
cornerRadius = 8.dp
backgroundColor = Transparent
contentColor = Primary Blue
```

### 6.2 输入框

```kotlin
// 样式
height = 56.dp
cornerRadius = 8.dp
borderColor = Surface (默认) / Primary Blue (聚焦)
backgroundColor = Surface
```

### 6.3 卡片

```kotlin
// 连接列表卡片
padding = 16.dp
cornerRadius = 12.dp
backgroundColor = Surface
```

### 6.4 对话框

```kotlin
// 样式
padding = 24.dp
cornerRadius = 16.dp
backgroundColor = Surface
maxWidth = 560.dp
```

---

## 7. 交互规范

### 7.1 手势

| 手势 | 功能 | 实现 |
|------|------|------|
| 单击 | 选择/打开 | `clickable {}` |
| 长按 | 上下文菜单 | `combinedClickable(onLongClick = {})` |
| 双指缩放 | 终端字体大小 | `detectTransformGestures` |
| 三指左滑 | 切换会话 | `detectGesture` (自定义) |
| 双击 | 复制/粘贴 | `pointerInput` |
| 拖拽 | 调整终端 | `detectDragGestures` |

### 7.2 动画

```kotlin
// 标准动画时长
val animationFast = 150.ms  // 快速反馈
val animationNormal = 300.ms // 标准过渡
val animationSlow = 500.ms   // 慢速强调
```

| 场景 | 时长 | 缓动 |
|------|------|------|
| 按钮点击反馈 | 150ms | FastOutSlowIn |
| 页面切换 | 300ms | FastOutSlowIn |
| 列表项展开 | 300ms | LinearOutSlowIn |
| 状态变化 | 200ms | FastOutSlowIn |

### 7.3 状态反馈

| 状态 | 视觉反馈 |
|------|----------|
| 可点击 | 涟漪效果 (Ripple) |
| 加载中 | 进度条 + 禁用 |
| 成功 | 绿色对勾 + Toast |
| 错误 | 红色提示 + Snackbar |
| 空状态 | 插图 + 提示文本 |

---

## 8. 平板适配

### 8.1 双栏布局

```kotlin
// 平板 (宽度 >= 600dp)
if (width >= 600.dp) {
    // 双栏：左侧列表 (250dp) + 右侧内容 (自适应)
    Row {
        ListColumn(modifier = Modifier.width(250.dp))
        ContentColumn(modifier = Modifier.weight(1f))
    }
} else {
    // 手机：单栏
    SingleColumn()
}
```

### 8.2 分屏支持

```kotlin
// 支持多窗口模式
// 最小宽度：320dp
// 长宽比：自由
```

### 8.3 键盘快捷键

| 快捷键 | 功能 | Compose 实现 |
|--------|------|-------------|
| Ctrl+T | 新标签页 | `Modifier.keyboardShortcut(Key.T, CTRL)` |
| Ctrl+W | 关闭标签页 | `Modifier.keyboardShortcut(Key.W, CTRL)` |
| Ctrl+Tab | 切换标签页 | 自定义 |
| Ctrl+Shift+T | 新连接 | `Modifier.keyboardShortcut(Key.T, CTRL + SHIFT)` |

---

## 9. 可访问性

### 9.1 对比度要求

| 内容类型 | 最小对比度 | 标准 |
|----------|-----------|------|
| 普通文本 | 4.5:1 | WCAG AA |
| 大文本 (>18sp) | 3:1 | WCAG AA |
| UI 组件 | 3:1 | WCAG AA |

### 9.2 点击区域

```kotlin
// 最小点击区域 48x48dp
Modifier
    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
    .clickable { }
```

### 9.3 内容描述

```kotlin
// 所有图标按钮必须有 contentDescription
IconButton(onClick = {}, modifier = Modifier.semantics {
    contentDescription = "新建连接"
})
```

---

## 10. 资源交付

### 10.1 颜色 XML

```xml
<!-- res/values/colors.xml -->
<color name="primary_blue">#2563EB</color>
<color name="success_green">#10B981</color>
<color name="error_red">#EF4444</color>
<color name="background_dark">#0F0F0F</color>
<color name="surface_dark">#1E1E1E</color>
```

### 10.2 主题定义

```xml
<!-- res/values/themes.xml -->
<style name="Theme.SSHPad" parent="android:Theme.Material.Light.NoActionBar">
    <item name="android:colorPrimary">@color/primary_blue</item>
    <item name="android:colorAccent">@color/primary_blue</item>
</style>
```

### 10.3 图标资源

格式：SVG (源文件) + PNG (多分辨率)
- mdpi: 1x (基准)
- hdpi: 1.5x
- xhdpi: 2x
- xxhdpi: 3x
- xxxhdpi: 4x

---

## 11. Figma 标注说明

### 11.1 标注规范

- 所有尺寸使用 DP
- 颜色使用 Hex + Alpha
- 字体使用 sp
- 间距使用 4px 网格

### 11.2 导出设置

- 设计稿尺寸：1920x1080 (平板基准)
- 导出格式：PNG @2x, @3x
- SVG 用于图标

---

*文档版本：v1.0*
*创建时间：2026-04-05*
*负责人：UI/UX 设计师*
*状态：✅ 已完成*
