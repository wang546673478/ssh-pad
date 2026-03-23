# 开发设计答疑 Q&A

## 文档信息
- **版本**: v1.0
- **创建日期**: 2026-04-05
- **适用对象**: Android 开发团队
- **更新方式**: 随时补充开发过程中的问题

---

## 常见问题 (FAQ)

### 色彩相关

#### Q1: 深色主题和浅色主题如何切换？

**A:** 使用 Material 3 的 `dynamicColorScheme`,跟随系统主题:

```kotlin
@Composable
fun SSHPadTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) 
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    // ...
}
```

**终端背景特殊处理:**
- 深色主题：纯黑 `#000000` (OLED 优化)
- 浅色主题：深灰 `#1E1E1E` (保持终端可读性)

#### Q2: Primary Blue 在深色主题下是否需要调整？

**A:** 不需要。`#2563EB` 在深色和浅色主题下都适用。Material 3 会自动调整透明度以保证对比度。

---

### 字体相关

#### Q3: 终端字体如何支持动态缩放？

**A:** 使用 `Modifier.pointerInput` 检测双指缩放手势:

```kotlin
var fontSize by remember { mutableStateOf(14.sp) }

Box(
    modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTransformGestures { _, _, scale, _ ->
                fontSize = (fontSize * scale)
                    .coerceIn(10.sp, 24.sp)
            }
        }
) {
    Text(
        text = terminalContent,
        fontFamily = JetBrainsMono,
        fontSize = fontSize,
        lineHeight = fontSize + 6.sp
    )
}
```

#### Q4: 中文字体需要特殊配置吗？

**A:** 需要。在 `Theme.kt`中指定`Noto Sans SC`:

```kotlin
val NotoSansSC = FontFamily(
    Font(R.font.noto_sans_sc_regular, FontWeight.Normal),
    Font(R.font.noto_sans_sc_medium, FontWeight.Medium),
    Font(R.font.noto_sans_sc_bold, FontWeight.Bold)
)
```

**下载:** https://fonts.google.com/noto/specimen/Noto+Sans+SC

---

### 布局相关

#### Q5: 平板双栏布局如何判断？

**A:** 使用 `WindowWidthSizeClass` 或最小宽度限定:

```kotlin
@Composable
fun MainLayout() {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    if (screenWidth >= 600.dp) {
        // 平板双栏布局
        Row {
            SideBar(modifier = Modifier.width(250.dp))
            MainContent(modifier = Modifier.weight(1f))
        }
    } else {
        // 手机单栏布局
        Column {
            // ...
        }
    }
}
```

**推荐:** 使用 Material 3 的 `WindowSizeClass`:

```kotlin
val windowSize = calculateWindowSizeClass(activity)

when (windowSize.widthSizeClass) {
    WindowWidthSizeClass.Expanded -> { /* 平板 */ }
    WindowWidthSizeClass.Medium -> { /* 折叠屏 */ }
    WindowWidthSizeClass.Compact -> { /* 手机 */ }
}
```

#### Q6: 侧边栏 250dp 是固定值吗？

**A:** 是的，固定 250dp。这是连接列表的最佳宽度:
- 能完整显示服务器名称 (约 20 字符)
- 能显示主机地址 (约 15 字符)
- 不会占用过多终端区域

---

### 交互相关

#### Q7: 如何 Material 3 风格涟漪效果？

**A:** 使用 `clickable` modifier 或 `IconButton`:

```kotlin
// 方式 1: clickable
Box(
    modifier = Modifier
        .clickable(
            indication = LocalIndication.current,
            interactionSource = remember { MutableInteractionSource() }
        ) { /* onClick */ }
)

// 方式 2: IconButton (推荐)
IconButton(onClick = { /* onClick */ }) {
    Icon(Icons.Filled.Menu, "菜单")
}
```

#### Q8: 长按上下文菜单如何实现？

**A:** 使用 `combinedClickable`:

```kotlin
ListItem(
    modifier = Modifier
        .combinedClickable(
            onClick = { /* 单击 */ },
            onLongClick = { /* 长按，显示上下文菜单 */ }
        )
)
```

**上下文菜单示例:**

```kotlin
var showMenu by remember { mutableStateOf(false) }

if (showMenu) {
    DropdownMenu(
        expanded = true,
        onDismissRequest = { showMenu = false }
    ) {
        DropdownMenuItem(
            text = { Text("编辑") },
            onClick = { /* 编辑 */; showMenu = false }
        )
        DropdownMenuItem(
            text = { Text("删除") },
            onClick = { /* 删除 */; showMenu = false }
        )
    }
}
```

---

### 组件相关

#### Q9: Material 3 的 Card 如何使用？

**A:** 使用`Card` composable:

```kotlin
Card(
    modifier = Modifier
        .fillMaxWidth()
        .height(64.dp),
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
) {
    // 内容
}
```

**深色主题阴影处理:**
深色主题下建议降低阴影或省略,使用背景色区分层级:

```kotlin
val isDark = isSystemInDarkTheme()
val elevation = if (isDark) 0.dp else 2.dp

Card(
    elevation = CardDefaults.cardElevation(defaultElevation = elevation)
)
```

#### Q10: OutlinedTextField 如何自定义边框颜色？

**A:** 使用 `colors` 参数:

```kotlin
OutlinedTextField(
    value = text,
    onValueChange = { text = it },
    modifier = Modifier.fillMaxWidth(),
    colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.surface,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface
    )
)
```

---

### 动画相关

#### Q11: 页面切换动画如何实现？

**A:** 使用 Navigation Compose 的默认动画:

```kotlin
val navController = rememberNavController()

NavHost(navController, startDestination = "main") {
    composable("main") { MainScreen() }
    composable("new-connection") { NewConnectionScreen() }
    composable("settings") { SettingsScreen() }
}
```

**自定义动画:**

```kotlin
composable(
    route = "new-connection",
    enterTransition = {
        slideInHorizontally(initialOffsetX = { it }) +
        fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing))
    },
    popEnterTransition = {
        slideOutHorizontally(targetOffsetX = { it }) +
        fadeOut(animationSpec = tween(250, easing = FastOutSlowInEasing))
    }
) { NewConnectionScreen() }
```

#### Q12: 按钮点击反馈动画需要自定义吗？

**A:** 不需要。Material 3 的 `Button` 和 `IconButton`已经内置点击反馈动画 (Ripple)。

---

### 终端相关

#### Q13: 终端 ANSI 颜色如何解析？

**A:** 参考设计系统的 Dracula 主题配色:

```kotlin
val terminalColors = mapOf(
    0 to TerminalBlack,    // 普通文本
    1 to TerminalRed,      // 错误
    2 to TerminalGreen,    // 成功
    3 to TerminalYellow,   // 警告
    4 to TerminalBlue,     // 链接
    5 to TerminalMagenta,  // 关键字
    6 to TerminalCyan,     // 提示
    7 to TerminalWhite     // 高亮
)
```

**ANSI 转义序列解析:**
- `\u001B[30m` - 黑色
- `\u001B[31m` - 红色
- `\u001B[32m` - 绿色
- `\u001B[33m` - 黄色
- `\u001B[34m` - 蓝色
- `\u001B[35m` - 紫色
- `\u001B[36m` - 青色
- `\u001B[37m` - 白色

#### Q14: 终端光标闪烁动画如何实现？

**A:** 使用 `animateFloatAsState`:

```kotlin
var isCursorVisible by remember { mutableStateOf(true) }

LaunchedEffect(Unit) {
    while (true) {
        delay(500)
        isCursorVisible = !isCursorVisible
    }
}

val alpha by animateFloatAsState(
    targetValue = if (isCursorVisible) 1f else 0.2f,
    animationSpec = tween(durationMillis = 100)
)

// 绘制光标
Box(
    modifier = Modifier
        .width(2.dp)
        .height(lineHeight)
        .background(
            color = PrimaryBlue.copy(alpha = alpha)
        )
)
```

---

### 性能相关

#### Q15: 终端 60fps 渲染如何保证？

**A:** 关键优化点:

1. **使用 LazyColumn 渲染长文本:**
```kotlin
LazyColumn {
    items(terminalLines) { line ->
        Text(
            text = line.text,
            color = line.color,
            fontFamily = JetBrainsMono,
            fontSize = fontSize
        )
    }
}
```

2. **避免不必要的重组:**
```kotlin
// 使用 derivedStateOf 减少重组
val visibleLines by derivedStateOf {
    terminalLines.drop(scrollOffset).take(visibleCount)
}
```

3. **使用 remember 缓存计算结果:**
```kotlin
val parsedContent = remember(sshOutput) {
    parseAnsi(sshOutput)
}
```

---

### 可访问性相关

#### Q16: 图标按钮的 contentDescription 如何写？

**A:** 描述功能，而不是图标本身:

```kotlin
// ❌ 不好的写法
IconButton(onClick = {}) {
    Icon(Icons.Filled.Add, "加号图标")
}

// ✅ 好的写法
IconButton(onClick = {}) {
    Icon(Icons.Filled.Add, "新建连接")
}

// ✅ 装饰性图标 (无障碍忽略)
Icon(
    Icons.Filled.Menu,
    contentDescription = null,
    modifier = Modifier.semantics(mergeDescendants = true) {}
)
```

#### Q17: 如何支持 TalkBack?

**A:** Material 3 组件默认支持 TalkBack。额外优化:

```kotlin
// 重要信息使用 semantics
Text(
    text = "已连接",
    modifier = Modifier.semantics {
        contentDescription = "服务器连接状态：已连接"
    }
)

// 进度条
LinearProgressIndicator(
    modifier = Modifier.semantics {
        progressBar()
        stateDescription = "连接中"
    }
)
```

---

## 设计规范快速查询

### 色彩速查

```kotlin
// 主色
PrimaryBlue = #2563EB
SuccessGreen = #10B981
ErrorRed = #EF4444

// 深色主题
DarkBackground = #0F0F0F
DarkSurface = #1E1E1E
DarkTerminalBg = #000000

// 浅色主题
LightBackground = #FFFFFF
LightSurface = #F3F4F6
LightTerminalBg = #1E1E1E
```

### 字体速查

```kotlin
// 字号
headlineLarge = 32.sp
headlineSmall = 24.sp
titleLarge = 22.sp
titleMedium = 16.sp
bodyLarge = 16.sp
bodyMedium = 14.sp
terminal = 14.sp (可调节 10-24.sp)
```

### 间距速查

```kotlin
spacing4 = 4.dp
spacing8 = 8.dp
spacing12 = 12.dp
spacing16 = 16.dp  // 标准内边距
spacing24 = 24.dp  // 字段间距
spacing32 = 32.dp
```

### 圆角速查

```kotlin
cornerSM = 8.dp    // 按钮、输入框
cornerMD = 12.dp   // 卡片
cornerLG = 16.dp   // 对话框
cornerFull = 9999.dp // 圆形
```

---

## 更新日志

### v1.0 (2026-04-05)
- 初始版本
- 添加常见开发问题 17 个
- 补充设计规范速查表

---

## 反馈渠道

如遇到设计相关问题，请联系:

- **飞书群:** Android SSH Client 项目组
- **设计答疑时间:** 每周三下午 14:00-16:00
- **紧急问题:** 随时联系 UI 设计师

---

*文档版本：v1.0*
*创建时间：2026-04-05*
*负责人：UI/UX 设计师*
*状态：✅ 已完成*
