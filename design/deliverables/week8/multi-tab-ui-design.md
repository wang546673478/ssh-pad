# 多标签会话管理 UI 设计稿 - Week 8

## 文档信息
- **版本**: v1.0
- **创建日期**: 2026-05-11
- **设计师**: UI/UX 设计师
- **适用阶段**: Week 8 (2026-05-11 ~ 2026-05-17)
- **状态**: 📋 设计完成

---

## 一、设计概述

### 1.1 功能目标
支持用户在同一界面中管理多个 SSH 会话，通过标签页快速切换，提升多服务器操作效率。

### 1.2 设计原则
- **直观**: 标签状态清晰可见
- **高效**: 快速切换、新建、关闭
- **一致**: 遵循 Material 3 和设计系统规范
- **平板优化**: 充分利用大屏空间

---

## 二、多标签 UI 设计稿

### 2.1 整体布局

```
┌─────────────────────────────────────────────────────────────────┐
│  [≡] SSH Pad                              [⚙️ 设置]  [+ 新建]  │
├──────────────┬──────────────────────────────────────────────────┤
│  标签页栏     │                                                  │
│  ┌─────┐     │                                                  │
│  │ aws │─────┼──────────────────────────────────────────────────┤
│  └─────┘     │                                                  │
│  ┌─────┐     │              💻 终端内容区                       │
│  │ db  │     │                                                  │
│  └─────┘     │   Last login: Mon May 11 10:30:00               │
│  ┌─────┐     │   admin@aws-server:~$ _                         │
│  │ web │     │                                                  │
│  └─────┘     │                                                  │
│              │                                                  │
│  [+ 新标签]   │                                                  │
├──────────────┴──────────────────────────────────────────────────┤
│  ⌨️ 键盘  |  3 个会话  |  📶 全部已连接  |  [×] 关闭当前        │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 标签页栏设计（左侧垂直布局 - 平板）

#### 样式规格
```kotlin
// 标签容器
width = 120.dp
backgroundColor = Surface (#1E1E1E)
padding = 8.dp

// 单个标签
height = 48.dp
cornerRadius = 8.dp
padding = 12.dp
spacing = 4.dp

// 选中状态
selectedBackgroundColor = Primary Blue (#2563EB, alpha 0.15)
selectedIndicator = 3.dp (左侧竖线)
selectedIndicatorColor = Primary Blue (#2563EB)

// 未选中状态
unselectedBackgroundColor = Transparent
unselectedIndicator = 0.dp

// 文本
selectedTextColor = OnSurface (#E5E5E5)
unselectedTextColor = OnSurface (alpha 0.6)
fontSize = 14.sp
fontWeight = Medium

// 图标
iconSize = 16.dp
iconColor = OnSurface (alpha 0.7)
```

#### 标签状态
| 状态 | 视觉表现 | 说明 |
|------|----------|------|
| 选中 | 蓝色背景 + 左侧蓝条 + 高亮文本 | 当前活跃会话 |
| 未选中 | 透明背景 + 灰色文本 | 后台会话 |
| 连接中 | 黄色呼吸灯图标 | 正在建立连接 |
| 已断开 | 红色图标 + 灰色文本 | 连接断开 |
| 悬停 | 10% 白色背景 | 鼠标悬停反馈 |

### 2.3 标签页栏设计（顶部水平布局 - 手机/小屏）

```
┌─────────────────────────────────────────────────────────┐
│  [≡]  [aws▼]  [db]  [web]  [+]           [⚙️]  [+]     │
├─────────────────────────────────────────────────────────┤
│                                                         │
│                   💻 终端内容区                         │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

#### 样式规格
```kotlin
// 标签栏容器
height = 56.dp
backgroundColor = Surface (#1E1E1E)
elevation = 2.dp

// 单个标签
minWidth = 100.dp
maxWidth = 200.dp
height = 48.dp
padding = 16.dp

// 标签内容
contentSpacing = 8.dp
textFontSize = 14.sp
iconSize = 16.dp

// 关闭按钮
closeButtonSize = 24.dp
closeButtonCornerRadius = 12.dp
closeButtonColor = OnSurface (alpha 0.5)
closeButtonVisible = onHover or selected
```

---

## 三、标签页交互设计

### 3.1 标签页切换

#### 交互方式
| 操作 | 功能 | 反馈 |
|------|------|------|
| 单击标签 | 切换到该会话 | 0.2s 平滑过渡 |
| 双指左滑 | 下一个标签 | 滑动动画 |
| 双指右滑 | 上一个标签 | 滑动动画 |
| Ctrl+Tab | 下一个标签 | 键盘快捷键 |
| Ctrl+Shift+Tab | 上一个标签 | 键盘快捷键 |

#### 切换动画
```kotlin
// 动画规格
animationDuration = 200.ms
animationEasing = FastOutSlowIn
contentFade = true (透明度 0 → 1)
contentSlide = true (横向位移 20dp → 0)
```

### 3.2 标签页新建

#### 入口
1. 左侧栏底部 [+ 新标签] 按钮
2. 顶部标签栏最右侧 [+] 按钮
3. 快捷键 Ctrl+Shift+T
4. 连接列表页右键"在新标签中打开"

#### 新建流程
```
点击 [+ 新标签]
    ↓
弹出快速连接对话框
    ↓
输入主机/选择已有连接
    ↓
创建新标签并连接
    ↓
自动切换到新标签
```

#### 对话框设计
```
┌─────────────────────────────────────┐
│  新建标签页                      [×] │
├─────────────────────────────────────┤
│                                     │
│  快速连接                           │
│  ┌─────────────────────────────┐   │
│  │ 搜索或输入主机地址...       │   │
│  └─────────────────────────────┘   │
│                                     │
│  最近连接                           │
│  ┌─────────────────────────────┐   │
│  │  ▶ aws-prod                 │   │
│  │  ▶ db-backup                │   │
│  │  ▶ web-frontend             │   │
│  └─────────────────────────────┘   │
│                                     │
│         [取消]  [连接]             │
└─────────────────────────────────────┘
```

### 3.3 标签页关闭

#### 关闭方式
| 操作 | 功能 |
|------|------|
| 点击标签 [×] 按钮 | 关闭该标签 |
| 快捷键 Ctrl+W | 关闭当前标签 |
| 中键点击标签 | 直接关闭 |
| 左滑标签（手机） | 关闭该标签 |

#### 关闭确认
- **默认**: 直接关闭（无确认）
- **未保存内容**: 弹出确认对话框
- **最后一个标签**: 不关闭，返回首页

#### 关闭动画
```kotlin
// 动画规格
closeDuration = 150.ms
closeEasing = FastOutSlowIn
closeEffect = shrink + fade
contentSlide = 向右滑出 20dp
```

---

## 四、标签页组件详细设计

### 4.1 标签项组件（Tab Item）

#### 组成元素
```
┌────────────────────────────┐
│ [🔵] aws-prod      [×]    │
│  ↑    ↑            ↑      │
│  │    │            └─ 关闭按钮
│  │    └─ 标签文本
│  └─ 状态指示器
└────────────────────────────┘
```

#### 状态指示器
| 状态 | 图标/颜色 | 位置 |
|------|-----------|------|
| 已连接 | 绿色圆点 (#10B981) | 文本前 |
| 连接中 | 黄色旋转图标 (#F1FA8C) | 文本前 |
| 已断开 | 红色圆点 (#EF4444) | 文本前 |
| 有通知 | 橙色徽章 | 文本后 |

#### 代码示例
```kotlin
@Composable
fun SessionTabItem(
    session: TerminalSession,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        Color(0xFF2563EB).copy(alpha = 0.15f)
    } else {
        Color.Transparent
    }
    
    val leftIndicator = if (isSelected) {
        Modifier
            .fillMaxHeight()
            .width(3.dp)
            .background(Color(0xFF2563EB))
    } else {
        Modifier
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(backgroundColor)
            .clickable(onClick = onSelect)
            .then(leftIndicator)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 状态指示器
            ConnectionStatusIcon(status = session.status)
            
            // 标签文本
            Text(
                text = session.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color(0xFFE5E5E5) 
                        else Color(0xFFE5E5E5).copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // 关闭按钮（悬停或选中时显示）
        if (isSelected || isHovered) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "关闭标签",
                    tint = Color(0xFFE5E5E5).copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
```

### 4.2 标签页栏组件（Tab Bar）

#### 平板垂直布局
```kotlin
@Composable
fun VerticalTabBar(
    sessions: List<TerminalSession>,
    selectedSessionId: String,
    onSessionSelected: (String) -> Unit,
    onSessionClosed: (String) -> Unit,
    onNewSession: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .background(Color(0xFF1E1E1E))
            .padding(8.dp)
    ) {
        // 标签列表
        sessions.forEach { session ->
            SessionTabItem(
                session = session,
                isSelected = session.id == selectedSessionId,
                onSelect = { onSessionSelected(session.id) },
                onClose = { onSessionClosed(session.id) }
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        // 新建标签按钮
        Spacer(modifier = Modifier.weight(1f))
        NewSessionButton(onClick = onNewSession)
    }
}
```

#### 手机顶部水平布局
```kotlin
@Composable
fun HorizontalTabBar(
    sessions: List<TerminalSession>,
    selectedSessionId: String,
    onSessionSelected: (String) -> Unit,
    onSessionClosed: (String) -> Unit,
    onNewSession: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFF1E1E1E))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 可横向滚动
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(sessions) { session ->
                CompactSessionTab(
                    session = session,
                    isSelected = session.id == selectedSessionId,
                    onSelect = { onSessionSelected(session.id) },
                    onClose = { onSessionClosed(session.id) }
                )
            }
            
            // 新建标签按钮
            item {
                IconButton(onClick = onNewSession) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "新建标签",
                        tint = Color(0xFF2563EB)
                    )
                }
            }
        }
    }
}
```

---

## 五、响应式布局

### 5.1 断点定义
```kotlin
val compactBreakpoint = 600.dp  // 手机/小屏
val mediumBreakpoint = 840.dp   // 中等平板
val expandedBreakpoint = 1200.dp // 大平板/桌面
```

### 5.2 布局策略

| 屏幕宽度 | 标签栏位置 | 标签栏宽度 | 布局模式 |
|----------|------------|------------|----------|
| < 600dp | 顶部 | 56dp (高度) | 水平滚动 |
| 600-840dp | 左侧 | 100dp | 垂直列表 |
| 840-1200dp | 左侧 | 120dp | 垂直列表 |
| > 1200dp | 左侧 | 160dp | 垂直列表 + 更多空间 |

### 5.3 平板双栏优化
```kotlin
@Composable
fun TabletLayout(
    windowSizeClass: WindowSizeClass
) {
    val isExpanded = windowSizeClass.widthSizeClass == WidthSizeClass.Expanded
    
    Row(modifier = Modifier.fillMaxSize()) {
        if (isExpanded) {
            // 平板：左侧标签栏 + 右侧终端
            VerticalTabBar(
                modifier = Modifier.width(120.dp),
                // ...
            )
            Divider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp),
                color = Color(0xFF333333)
            )
        }
        
        // 终端内容区
        TerminalContent(
            modifier = Modifier.weight(1f)
        )
    }
}
```

---

## 六、键盘快捷键

### 6.1 标签管理快捷键

| 快捷键 | 功能 | 平台 |
|--------|------|------|
| Ctrl+T | 新建标签 | 全平台 |
| Ctrl+W | 关闭当前标签 | 全平台 |
| Ctrl+Tab | 切换到下一个标签 | 全平台 |
| Ctrl+Shift+Tab | 切换到上一个标签 | 全平台 |
| Ctrl+1 ~ Ctrl+9 | 切换到指定位置标签 | 全平台 |
| Ctrl+Shift+T | 重新打开关闭的标签 | 全平台 |

### 6.2 Compose 实现
```kotlin
Modifier.keyboardShortcut(
    key = Key.T,
    modifiers = KeyModifiers.Ctrl,
    onShortcut = { viewModel.createNewSession() }
)

Modifier.keyboardShortcut(
    key = Key.W,
    modifiers = KeyModifiers.Ctrl,
    onShortcut = { viewModel.closeCurrentSession() }
)

Modifier.keyboardShortcut(
    key = Key.Tab,
    modifiers = KeyModifiers.Ctrl,
    onShortcut = { viewModel.nextSession() }
)
```

---

## 七、可访问性

### 7.1 无障碍支持
- ✅ 所有按钮有 contentDescription
- ✅ 标签状态通过文本和图标双重表达
- ✅ 键盘导航支持（Tab 键切换）
- ✅ 最小点击区域 48x48dp
- ✅ 颜色对比度符合 WCAG AA

### 7.2 屏幕阅读器
```kotlin
IconButton(
    onClick = onClose,
    modifier = Modifier.semantics {
        contentDescription = "关闭 ${session.name} 会话"
    }
)
```

---

## 八、设计稿交付

### 8.1 Figma 文件
- **文件路径**: (待上传)
- **页面**: Multi-tab Session Management
- **组件**: TabBar, TabItem, NewSessionButton

### 8.2 导出资源
| 资源 | 格式 | 尺寸 | 数量 |
|------|------|------|------|
| 标签页图标 | SVG | 24x24dp | 8 个 |
| 状态指示器 | SVG | 16x16dp | 4 个 |
| 关闭按钮 | SVG | 24x24dp | 2 个状态 |

---

## 九、验收标准

### 9.1 视觉验收
- [ ] 标签颜色符合设计系统
- [ ] 间距遵循 4dp 网格
- [ ] 字体大小/字重正确
- [ ] 圆角符合规范 (8dp)
- [ ] 阴影/高程正确

### 9.2 交互验收
- [ ] 点击切换流畅 (200ms)
- [ ] 关闭动画自然 (150ms)
- [ ] 悬停反馈明显
- [ ] 键盘快捷键可用
- [ ] 手势操作灵敏

### 9.3 功能验收
- [ ] 支持至少 10 个标签
- [ ] 标签可排序
- [ ] 状态显示正确
- [ ] 内存占用合理 (<100MB)

---

## 十、开发实现建议

### 10.1 组件拆分
```
presentation/
├── components/
│   ├── tabbar/
│   │   ├── VerticalTabBar.kt
│   │   ├── HorizontalTabBar.kt
│   │   └── SessionTabItem.kt
│   └── tabbar/
│       └── NewSessionButton.kt
├── screens/
│   └── TerminalScreen.kt (更新)
└── viewmodel/
    └── TerminalViewModel.kt (更新)
```

### 10.2 状态管理
```kotlin
data class TerminalScreenState(
    val sessions: List<TerminalSession> = emptyList(),
    val selectedSessionId: String? = null,
    val isCreatingNewSession: Boolean = false
)

class TerminalViewModel(
    // ...
    fun switchSession(sessionId: String) { }
    fun closeSession(sessionId: String) { }
    fun createNewSession() { }
    fun nextSession() { }
    fun previousSession() { }
)
```

### 10.3 性能优化
- 使用 LazyColumn/LazyRow 渲染标签
- 标签内容懒加载（仅渲染可见标签）
- 会话后台保持，按需切换
- 使用 DerivedState 优化重绘

---

## 附录

### A. 设计系统参考
- 色彩：design-system.md §1
- 字体：design-system.md §2
- 间距：design-system.md §3
- 圆角：design-system.md §4

### B. 相关文档
- [SCREENS.md](./screens.md) - 界面设计稿
- [DESIGN_QA.md](./DESIGN_QA.md) - 开发答疑

### C. 联系方式
- 飞书群：Android SSH Client 项目组
- 设计答疑：每周三下午 14:00-16:00

---

*设计版本：v1.0*  
*创建时间：2026-05-11*  
*设计师：UI/UX 设计师*  
*状态：✅ 设计完成*
