# Week 6 UI 走查报告

## 报告信息
- **版本**: v1.0
- **走查日期**: 2026-04-27
- **走查人**: UI/UX 设计师
- **阶段**: Week 6 (2026-04-27 ~ 2026-05-03)
- **状态**: 🔄 进行中

---

## 一、走查范围

### 1.1 审查对象

| 文件 | 类型 | 实现状态 | 走查状态 |
|------|------|----------|----------|
| ConnectionListScreen.kt | UI 屏幕 | ✅ Week 5 实现 | ✅ 已走查 |
| TerminalScreen.kt | UI 屏幕 | ✅ Week 5 实现 | ✅ 已走查 |
| ConnectionEditScreen.kt | UI 屏幕 | ✅ Week 5 实现 | ⏳ 待走查 |
| Theme.kt | 主题 | ✅ Week 5 实现 | ✅ 已走查 |
| AppNavigation.kt | 导航 | ✅ Week 5 实现 | ⏳ 待走查 |

### 1.2 走查维度

- ✅ 视觉还原度 (对比设计稿 v0.5)
- ✅ 色彩准确性
- ✅ 间距规范性 (4dp 网格)
- ✅ 字体使用
- ✅ 图标清晰度和尺寸
- ✅ 深色模式适配
- ✅ 交互反馈
- ⏳ ANSI 256 色解析 (待 ViewModel 集成)

---

## 二、主界面走查 (ConnectionListScreen)

### 2.1 Top Bar

| 检查项 | 设计标准 | 实际实现 | 状态 | 备注 |
|--------|----------|----------|------|------|
| 高度 | 56dp | ✅ 56dp (默认) | ✅ | Material 3 默认值 |
| 标题字体 | headlineSmall (24sp) | ⚠️ titleLarge (默认) | ⚠️ | 建议显式指定 |
| 左右内边距 | 16dp | ✅ 16dp (默认) | ✅ | - |
| 设置图标 | 24x24dp | ✅ 24x24dp | ✅ | Icons.Filled.Settings |
| 图标颜色 | OnBackground | ✅ 自动适配 | ✅ | - |

**问题:**
- P2: 标题字体应显式指定为 `MaterialTheme.typography.headlineSmall`

**建议:**
```kotlin
TopAppBar(
    title = { 
        Text(
            "SSH Connections",
            style = MaterialTheme.typography.headlineSmall
        ) 
    },
    // ...
)
```

### 2.2 连接列表项 (ConnectionListItem)

| 检查项 | 设计标准 | 实际实现 | 状态 | 备注 |
|--------|----------|----------|------|------|
| 卡片高度 | 64dp | ⚠️ 自适应 | ⚠️ | 建议固定最小高度 |
| 左右内边距 | 16dp | ✅ 16dp | ✅ | - |
| 上下内边距 | 12dp | ⚠️ 16dp | ⚠️ | 略大于标准 |
| 状态图标 | 20x20dp | ✅ 12dp 圆点 | ⚠️ | 尺寸略小 |
| 图标间距 | 12dp | ✅ 12dp | ✅ | - |
| 服务器名称 | titleMedium | ✅ titleMedium | ✅ | - |
| 主机地址 | bodySmall | ✅ bodySmall | ✅ | - |
| 主机地址颜色 | DisabledText (#6B7280) | ⚠️ onSurfaceVariant | ⚠️ | 颜色略浅 |
| 选中背景 | Primary alpha 10% | ❌ 未实现 | ❌ | 缺失 |
| 点击反馈 | Ripple | ✅ 默认 | ✅ | - |
| 项间距 | 0dp (紧密) | ⚠️ 4dp | ⚠️ | 使用了 LazyColumn 的 spacedBy |

**问题:**
- P1: 缺少选中状态背景 (选中连接的高亮)
- P2: 状态图标尺寸应从 12dp 增加到 20dp
- P2: 主机地址颜色应使用 `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)`
- P2: 列表项内边距应调整为上下 12dp
- P3: 建议添加长按上下文菜单 (编辑/删除)

**修复建议:**
```kotlin
@Composable
private fun ConnectionListItem(
    connection: SSHConnection,
    isSelected: Boolean = false,  // 新增参数
    onClick: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)  // 固定高度
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 状态图标 (20x20dp)
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        if (connection.lastConnectedAt != null) 
                            MaterialTheme.colorScheme.secondary 
                        else 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
            )
            // ...
        }
    }
}
```

### 2.3 快速连接区域 (Quick Connect)

| 检查项 | 设计标准 | 实际实现 | 状态 | 备注 |
|--------|----------|----------|------|------|
| 卡片内边距 | 16dp | ✅ 16dp | ✅ | - |
| 卡片圆角 | 12dp | ✅ 12dp (默认) | ✅ | - |
| 卡片阴影 | 2dp | ✅ 2dp | ✅ | - |
| 标题颜色 | Primary | ✅ Primary | ✅ | - |
| 芯片高度 | 32dp | ✅ 32dp (默认) | ✅ | FilterChip |
| 芯片间距 | 8dp | ✅ 8dp | ✅ | - |
| 状态圆点 | 8dp | ✅ 8dp | ✅ | - |

**优点:**
- ✅ 实现符合设计稿
- ✅ 使用 Material 3 FilterChip 组件
- ✅ 快速连接逻辑清晰

### 2.4 FloatingActionButton

| 检查项 | 设计标准 | 实际实现 | 状态 | 备注 |
|--------|----------|----------|------|------|
| 尺寸 | 56x56dp | ✅ 56x56dp | ✅ | - |
| 图标尺寸 | 24x24dp | ✅ 24x24dp | ✅ | - |
| 颜色 | Primary + White | ✅ 自动适配 | ✅ | - |
| 位置 | 右下角 | ✅ 默认 | ✅ | - |
| 边距 | 16dp | ✅ 16dp (默认) | ✅ | - |

**✅ 通过**

---

## 三、终端界面走查 (TerminalScreen)

### 3.1 Top Bar

| 检查项 | 设计标准 | 实际实现 | 状态 | 备注 |
|--------|----------|----------|------|------|
| 高度 | 56dp | ✅ 56dp | ✅ | - |
| 标题字体 | titleMedium (16sp) | ⚠️ 14sp | ⚠️ | 略小于标准 |
| 标题内容 | 会话名称 | ❌ "Terminal" | ❌ | 应显示连接名称 |
| 返回图标 | 24x24dp | ✅ 24x24dp | ✅ | - |
| 更多菜单 | 24x24dp | ✅ 24x24dp | ✅ | - |

**问题:**
- P1: 标题应显示实际连接名称 (如 "Production Server")
- P2: 标题字体应使用 `MaterialTheme.typography.titleMedium`

### 3.2 终端区域

| 检查项 | 设计标准 | 实际实现 | 状态 | 备注 |
|--------|----------|----------|------|------|
| 背景色 | #000000 (纯黑) | ✅ Color.Black | ✅ | OLED 优化 |
| 文本颜色 | #F8F8F2 (Dracula) | ❌ #00FF00 (纯绿) | ❌ | 应使用设计系统颜色 |
| 字体 | JetBrains Mono | ⚠️ FontFamily.Monospace | ⚠️ | 应使用自定义等宽字体 |
| 字号 | 14sp (可调 10-24sp) | ✅ 14f (可调) | ✅ | - |
| 行高 | 20sp | ❌ 默认 | ❌ | 应显式指定 lineHeight |
| 左右内边距 | 12dp | ⚠️ 8dp | ⚠️ | 略小于标准 |
| 上下内边距 | 16dp | ⚠️ 8dp | ⚠️ | 略小于标准 |
| 光标 | Primary Blue 闪烁 | ❌ 未实现 | ❌ | 缺失 |
| 文本选择 | Primary alpha 30% | ❌ 未实现 | ❌ | 缺失 |

**问题:**
- P0: 终端颜色应使用设计系统的 Dracula 配色，而非纯绿
- P1: 应使用 JetBrains Mono 字体 (需添加字体文件)
- P1: 缺少光标闪烁动画
- P2: 文本行高应显式指定为 `lineHeight = fontSize + 6.sp`
- P2: 内边距应调整为 左右 12dp、上下 16dp
- P3: 缺少文本选择和复制功能

**修复建议:**
```kotlin
// 终端文本样式
Text(
    text = terminalOutput,
    color = MaterialTheme.colorScheme.onSurface,  // 或使用终端专用颜色
    fontSize = fontSize.sp,
    fontFamily = JetBrainsMono,  // 需先定义
    lineHeight = (fontSize + 6).sp,
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 16.dp)
)

// 光标闪烁动画
@Composable
fun BlinkingCursor() {
    var isVisible by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            isVisible = !isVisible
        }
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.2f,
        animationSpec = tween(durationMillis = 100)
    )
    
    Box(
        modifier = Modifier
            .width(2.dp)
            .height(20.dp)
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
            )
    )
}
```

### 3.3 字体缩放菜单

| 检查项 | 设计标准 | 实际实现 | 状态 | 备注 |
|--------|----------|----------|------|------|
| 菜单触发 | 更多按钮 | ✅ 三点菜单 | ✅ | - |
| Zoom In | +2sp | ✅ +2f | ✅ | - |
| Zoom Out | -2sp | ✅ -2f | ✅ | - |
| 最小字号 | 10sp | ✅ 10f | ✅ | - |
| 最大字号 | 24sp | ✅ 24f | ✅ | - |
| 清屏功能 | ✅ | ✅ 已实现 | ✅ | - |
| 断开连接 | ✅ | ✅ 已实现 | ✅ | - |

**✅ 通过**

### 3.4 命令输入框

| 检查项 | 设计标准 | 实际实现 | 状态 | 备注 |
|--------|----------|----------|------|------|
| 提示符 | "$ " | ✅ 已实现 | ✅ | - |
| 提示符颜色 | #F8F8F2 | ❌ #00FF00 | ❌ | 应使用设计系统颜色 |
| 输入框背景 | Transparent | ✅ 透明 | ✅ | - |
| 输入框边框 | Transparent | ✅ 透明 | ✅ | - |
| 输入文本颜色 | #F8F8F2 | ✅ White | ⚠️ 应统一使用设计系统 |
| Enter 发送 | ✅ | ✅ 已实现 | ✅ | keyCode 66 |

**问题:**
- P2: 颜色应统一使用设计系统定义

---

## 四、主题实现走查 (Theme.kt)

### 4.1 深色主题配色

| 颜色 | 设计标准 | 实际实现 | 状态 | 备注 |
|------|----------|----------|------|------|
| Primary | #2563EB | ❌ #64B5F6 | ❌ | 颜色偏差较大 |
| Secondary | #10B981 | ❌ #81C784 | ❌ | 颜色偏差较大 |
| Tertiary | - | ❌ #FFB74D | ⚠️ 未定义 | 设计稿未指定 |
| Background | #0F0F0F | ❌ #121212 | ⚠️ | 略浅于标准 |
| Surface | #1E1E1E | ✅ #1E1E1E | ✅ | - |
| OnBackground | #F8F8F2 | ✅ White | ⚠️ | 应使用精确色值 |
| OnSurface | #E5E5E5 | ✅ White | ⚠️ | 应使用精确色值 |

**问题:**
- P0: Primary 和 Secondary 颜色与设计系统严重不符
- P1: 应严格按照设计系统定义的色值

**修复建议:**
```kotlin
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF2563EB),      // 设计标准：Primary Blue
    secondary = Color(0xFF10B981),    // 设计标准：Success Green
    tertiary = Color(0xFFBD93F9),     // Terminal Blue (新增)
    background = Color(0xFF0F0F0F),   // 设计标准
    surface = Color(0xFF1E1E1E),      // 设计标准
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFF8F8F2), // 设计标准
    onSurface = Color(0xFFE5E5E5)     // 设计标准
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2563EB),
    secondary = Color(0xFF10B981),
    tertiary = Color(0xFFBD93F9),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFF3F4F6),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1F2937),
    onSurface = Color(0xFF374151)
)
```

### 4.2 动态颜色

| 检查项 | 状态 | 备注 |
|--------|------|------|
| Android 12+ 支持 | ✅ 已实现 | - |
| 深色模式跟随系统 | ✅ 已实现 | - |
| 状态栏适配 | ✅ 已实现 | - |

**✅ 通过**

---

## 五、深色模式走查

### 5.1 整体对比度

| 屏幕 | 深色模式 | 浅色模式 | 状态 | 备注 |
|------|----------|----------|------|------|
| 主界面 | ✅ 可用 | ⚠️ 未测试 | ⚠️ | 浅色模式需验证 |
| 终端界面 | ✅ 可用 | ⚠️ 未测试 | ⚠️ | 终端浅色模式应为深色背景 |
| 设置界面 | ⏳ 未实现 | ⏳ 未实现 | ⏳ | - |

### 5.2 终端配色 (ANSI 颜色)

**当前实现:** 仅使用纯绿色 (#00FF00)

**设计标准:** Dracula 主题 8 色

| ANSI | 设计色值 | 当前实现 | 状态 |
|------|----------|----------|------|
| 0 Black | #21222C | ❌ #00FF00 | ❌ |
| 1 Red | #FF5555 | ❌ #00FF00 | ❌ |
| 2 Green | #50FA7B | ⚠️ #00FF00 | ⚠️ 接近但需精确 |
| 3 Yellow | #F1FA8C | ❌ #00FF00 | ❌ |
| 4 Blue | #BD93F9 | ❌ #00FF00 | ❌ |
| 5 Magenta | #FF79C6 | ❌ #00FF00 | ❌ |
| 6 Cyan | #8BE9FD | ❌ #00FF00 | ❌ |
| 7 White | #F8F8F2 | ❌ #00FF00 | ❌ |

**问题:**
- P0: 缺少 ANSI 转义序列解析器
- P0: 缺少 256 色支持
- P1: 应实现完整的 Dracula 主题配色

**建议实现方案:**
```kotlin
// 终端颜色映射
object TerminalColors {
    val ansiColors = mapOf(
        0 to Color(0xFF21222C),  // Black
        1 to Color(0xFFFF5555),  // Red
        2 to Color(0xFF50FA7B),  // Green
        3 to Color(0xFFF1FA8C),  // Yellow
        4 to Color(0xFFBD93F9),  // Blue
        5 to Color(0xFFFF79C6),  // Magenta
        6 to Color(0xFF8BE9FD),  // Cyan
        7 to Color(0xFFF8F8F2)   // White
    )
    
    fun getColor(ansiCode: Int): Color {
        return ansiColors[ansiCode] ?: ansiColors[7]!!
    }
}

// ANSI 解析器 (简化版)
fun parseAnsi(text: String): AnnotatedString {
    return buildAnnotatedString {
        var currentIndex = 0
        var currentColor = TerminalColors.getColor(7) // Default white
        
        while (currentIndex < text.length) {
            if (text.startsWith("\u001B[", currentIndex)) {
                // 解析 ANSI 转义序列
                val endIdx = text.indexOf('m', currentIndex)
                if (endIdx != -1) {
                    val code = text.substring(currentIndex + 2, endIdx)
                        .toIntOrNull() ?: 0
                    if (code in 30..37) {
                        currentColor = TerminalColors.getColor(code - 30)
                    }
                    currentIndex = endIdx + 1
                    continue
                }
            }
            
            append(text[currentIndex].toString())
            currentIndex++
        }
    }
}
```

---

## 六、交互细节走查

### 6.1 点击反馈

| 组件 | 预期反馈 | 实际反馈 | 状态 |
|------|----------|----------|------|
| 连接列表项 | Ripple | ✅ 默认 Ripple | ✅ |
| 按钮 | Ripple | ✅ 默认 Ripple | ✅ |
| 图标按钮 | Ripple | ✅ 默认 Ripple | ✅ |
| FAB | Ripple | ✅ 默认 Ripple | ✅ |

**✅ 通过**

### 6.2 加载状态

| 场景 | 设计标准 | 实际实现 | 状态 |
|------|----------|----------|------|
| 连接中 | Spinner + 禁用 | ❌ 未实现 | ❌ |
| 列表加载 | Shimmer | ❌ 未实现 | ❌ |
| 保存中 | Button Loading | ❌ 未实现 | ❌ |

**问题:**
- P2: 缺少连接中状态反馈
- P2: 缺少加载动画

### 6.3 空状态

| 场景 | 设计标准 | 实际实现 | 状态 |
|------|----------|----------|------|
| 无连接 | 插图 + 提示 + 操作按钮 | ❌ 未实现 | ❌ |
| 搜索无结果 | 提示文本 | ❌ 未实现 | ❌ |

**问题:**
- P2: 缺少空状态设计实现

---

## 七、问题汇总

### P0 级别 (必须修复)

| 编号 | 问题 | 位置 | 修复建议 | 工作量 |
|------|------|------|----------|--------|
| P0-01 | 主题颜色严重偏差 | Theme.kt | 使用设计系统标准色值 | 1h |
| P0-02 | 终端颜色使用纯绿 | TerminalScreen | 实现 Dracula 配色 | 2h |
| P0-03 | 缺少 ANSI 解析器 | 新建文件 | 实现 ANSI 转义序列解析 | 4h |

### P1 级别 (重要修复)

| 编号 | 问题 | 位置 | 修复建议 | 工作量 |
|------|------|------|----------|--------|
| P1-01 | 终端标题未显示连接名 | TerminalScreen | 传入 connectionName 参数 | 0.5h |
| P1-02 | 终端字体未使用 JetBrains Mono | Theme.kt | 添加字体文件并引用 | 1h |
| P1-03 | 缺少光标闪烁动画 | TerminalScreen | 实现 BlinkingCursor 组件 | 1h |
| P1-04 | 连接列表缺少选中状态 | ConnectionListScreen | 添加 isSelected 参数 | 1h |
| P1-05 | 缺少 ViewModel 层 | 新建 | 实现 ViewModel | 4h |

### P2 级别 (建议修复)

| 编号 | 问题 | 位置 | 修复建议 | 工作量 |
|------|------|------|----------|--------|
| P2-01 | 主界面标题字体未显式指定 | ConnectionListScreen | 使用 headlineSmall | 0.5h |
| P2-02 | 状态图标尺寸过小 | ConnectionListItem | 从 12dp 改为 20dp | 0.5h |
| P2-03 | 主机地址颜色偏差 | ConnectionListItem | 使用 onSurface.copy(0.6f) | 0.5h |
| P2-04 | 列表项内边距偏差 | ConnectionListItem | 调整为 12dp 上下 | 0.5h |
| P2-05 | 终端内边距偏差 | TerminalScreen | 调整为 12/16dp | 0.5h |
| P2-06 | 终端行高未显式指定 | TerminalScreen | 使用 lineHeight = fontSize+6 | 0.5h |
| P2-07 | 终端标题字体大小偏差 | TerminalScreen | 使用 16sp | 0.5h |
| P2-08 | 缺少加载状态反馈 | 多处 | 添加 Loading 指示器 | 2h |
| P2-09 | 缺少空状态设计 | 多处 | 实现空状态组件 | 2h |

### P3 级别 (可选优化)

| 编号 | 问题 | 位置 | 修复建议 | 工作量 |
|------|------|------|----------|--------|
| P3-01 | 缺少长按上下文菜单 | ConnectionListItem | 添加编辑/删除菜单 | 2h |
| P3-02 | 缺少文本选择功能 | TerminalScreen | 实现文本选择和复制 | 4h |
| P3-03 | 列表项间距偏差 | ConnectionListScreen | 从 4dp 改为 0dp | 0.5h |

---

## 八、修复优先级建议

### 第一阶段 (Week 6 必须完成)

1. ✅ **P0-01**: 修复主题颜色 (Theme.kt)
2. ✅ **P0-02**: 实现终端 Dracula 配色
3. ✅ **P1-01**: 终端标题显示连接名
4. ✅ **P1-05**: 实现 ViewModel 层
5. ✅ **P2-01 ~ P2-07**: 修复 UI 细节问题

**预计工作量:** 16 小时

### 第二阶段 (Week 7 完成)

1. **P0-03**: 实现完整 ANSI 解析器
2. **P1-02**: 添加 JetBrains Mono 字体
3. **P1-03**: 实现光标闪烁
4. **P1-04**: 实现选中状态
5. **P2-08 ~ P2-09**: 加载状态和空状态

**预计工作量:** 12 小时

### 第三阶段 (Week 8 完成)

1. **P3-01 ~ P3-03**: 交互优化

**预计工作量:** 6 小时

---

## 九、设计资源支持

### 9.1 已提供资源

- ✅ Color.kt (颜色定义)
- ✅ Theme.kt (主题定义)
- ✅ Type.kt (字体系统)
- ✅ Spacing.kt (间距系统)
- ✅ strings.xml (字符串资源)
- ✅ ic_ssh.svg (自定义图标)
- ✅ ic_connected.svg (连接状态图标)
- ✅ ic_connecting.svg (连接中图标)
- ✅ ic_disconnected.svg (断开图标)
- ✅ ic_empty_state.svg (空状态插图)

### 9.2 待提供资源

- ⏳ JetBrains Mono 字体文件
- ⏳ Noto Sans SC 字体文件
- ⏳ 完整图标资源包 (40+ 图标)

### 9.3 设计规范查询

- [设计系统规范](../../design/design-system.md)
- [Figma 标注](../../design/annotations/figma-annotations.md)
- [开发答疑 Q&A](../../design/DESIGN_QA.md)

---

## 十、验收标准

### 10.1 视觉还原度

- [ ] 色彩准确率：100% (使用设计系统标准色值)
- [ ] 字体还原率：> 95% (使用指定字体)
- [ ] 间距规范率：> 95% (遵循 4dp 网格)
- [ ] 图标清晰度：多尺寸适配 (1x/2x/3x)

### 10.2 交互验收

- [ ] 点击反馈：Material Ripple 100%
- [ ] 加载状态：所有异步操作有反馈
- [ ] 空状态：所有空场景有引导
- [ ] 长按菜单：列表项支持上下文菜单

### 10.3 深色模式

- [ ] 深色模式：100% 适配
- [ ] 对比度：WCAG AA 标准 (4.5:1)
- [ ] 终端配色：Dracula 主题 8 色 + 256 色

---

## 十一、总结

### 11.1 Week 5 实现亮点

1. ✅ 项目结构清晰，符合 Clean Architecture
2. ✅ Material 3 组件使用正确
3. ✅ 导航结构合理
4. ✅ 基础功能已实现

### 11.2 主要问题

1. ❌ 主题颜色与设计系统偏差较大
2. ❌ 终端视觉效果未遵循设计稿
3. ❌ 缺少 ViewModel 层
4. ❌ UI 细节需优化

### 11.3 改进建议

1. **建立设计审查流程**: 每次 UI 更新后需设计师走查
2. **使用设计资源包**: 直接复制 Color.kt、Theme.kt 等文件
3. **加强沟通**: 遇到问题先查 DESIGN_QA.md
4. **定期对齐**: 每周三下午设计答疑会

---

## 十二、下一步行动

### 开发团队 (Week 6)

- [ ] 修复 P0 问题 (主题颜色、终端配色)
- [ ] 实现 ViewModel 层
- [ ] 修复 P1/P2 UI 细节问题
- [ ] 集成 ANSI 解析器基础版

### 设计团队 (Week 6)

- [x] 输出 UI 走查报告
- [ ] 提供完整图标资源包
- [ ] 支持开发答疑
- [ ] Figma 设计稿完善 (终端 + 设置界面)

### 验收时间

- **中期检查**: 2026-04-30 (周四)
- **最终验收**: 2026-05-03 (周日)

---

*报告版本：v1.0*
*创建时间：2026-04-27*
*负责人：UI/UX 设计师*
*状态：✅ 已完成*
