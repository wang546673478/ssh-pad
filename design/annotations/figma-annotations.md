# Figma 设计标注 v1.0

## 文档信息
- **版本**: v1.0
- **创建日期**: 2026-04-05
- **Figma 文件**: (待补充链接)
- **状态**: 🔄 标注中

---

## 界面 1: 主界面 (平板双栏布局)

### 1.1 整体布局

```
┌─────────────────────────────────────────────────────────┐
│  Top Bar (高度：56dp)                                   │
├──────────────┬──────────────────────────────────────────┤
│              │                                          │
│  侧边栏      │         主内容区                         │
│  (宽度：250dp)│         (自适应)                        │
│              │                                          │
│              │                                          │
├──────────────┴──────────────────────────────────────────┤
│  Bottom Bar (高度：48dp)                                │
└─────────────────────────────────────────────────────────┘
```

### 1.2 Top Bar (顶部栏)

**尺寸:**
- 高度：56dp
- 左右内边距：16dp

**元素:**

| 元素 | 位置 | 尺寸 | 颜色 | 说明 |
|------|------|------|------|------|
| 菜单图标 | 左侧 | 24x24dp | OnBackground | ≡ 汉堡菜单 |
| 标题 | 左侧 (距图标 16dp) | auto | headlineSmall | "SSH Pad" |
| 设置图标 | 右侧 | 24x24dp | OnBackground | ⚙️ 设置按钮 |
| 新建按钮 | 右侧 (距设置 16dp) | 24x24dp | Primary | + 新建连接 |

**背景色:** Surface (#1E1E1E)

### 1.3 侧边栏 (连接列表)

**尺寸:**
- 宽度：250dp (固定)
- 背景色：Surface (#1E1E1E)

**连接列表项:**

```kotlin
// 单个连接项
ListItem(
    modifier = Modifier
        .height(64dp)
        .fillMaxWidth(),
    colors = ListItemDefaults.colors(
        containerColor = if (selected) Primary.copy(alpha = 0.1f) else Transparent
    )
)
```

| 元素 | 尺寸 | 颜色 | 说明 |
|------|------|------|------|
| 状态图标 | 20x20dp | Secondary/Grey | ● 连接状态 (绿=已连接，灰=未连接) |
| 服务器名称 | titleMedium | OnBackground | 如 "aws-prod" |
| 主机地址 | bodySmall | DisabledText | 如 "192.168.1.100:22" |
| 选中背景 | - | Primary alpha 10% | 当前选中项 |

**列表间距:**
- 项内边距：16dp (左右), 12dp (上下)
- 项间距：0dp (紧密排列)

**添加连接按钮:**
- 位置：列表底部
- 高度：48dp
- 样式：Text Button
- 文本："+ 添加连接"
- 颜色：Primary

### 1.4 主内容区 (终端区域)

**尺寸:**
- 宽度：自适应 (总宽 - 250dp)
- 高度：总高 - 56dp (TopBar) - 48dp (BottomBar)

**终端内容:**

```kotlin
TerminalView(
    backgroundColor = TerminalBg,      // #000000 纯黑
    textColor = TerminalWhite,         // #F8F8F2
    fontFamily = JetBrainsMono,
    fontSize = 14.sp,                  // 可调节 10-24sp
    lineHeight = 20.sp
)
```

**空白状态 (无连接):**

```
┌─────────────────────────────────────┐
│                                     │
│         🖥️                          │
│    (64x64dp 图标)                   │
│                                     │
│    暂无连接                         │
│    (titleMedium, OnSurface)         │
│                                     │
│    点击右上角 + 新建连接            │
│    (bodyMedium, DisabledText)       │
│                                     │
│    [ 新建连接 ]                     │
│    (Filled Button, 48dp 高)         │
│                                     │
└─────────────────────────────────────┘
```

### 1.5 Bottom Bar (底部栏)

**尺寸:**
- 高度：48dp
- 背景色：Surface (#1E1E1E)

**元素:**

| 元素 | 位置 | 尺寸 | 颜色 | 说明 |
|------|------|------|------|------|
| 键盘切换 | 左侧 | auto | OnSurface | ⌨️ 键盘按钮 |
| 会话计数 | 中间 | auto | DisabledText | "3 个会话" |
| 连接状态 | 右侧 | auto | Secondary | 📶 已连接 |

---

## 界面 2: 新建连接界面

### 2.1 整体布局

```
┌─────────────────────────────────────────┐
│  Top Bar                                │
├─────────────────────────────────────────┤
│                                         │
│  Form Content (滚动)                    │
│                                         │
├─────────────────────────────────────────┤
│  Action Bar (固定在底部)                │
└─────────────────────────────────────────┘
```

### 2.2 Top Bar

| 元素 | 位置 | 尺寸 | 颜色 | 说明 |
|------|------|------|------|------|
| 返回箭头 | 左侧 | 24x24dp | OnBackground | ← |
| 标题 | 左侧 (距箭头 16dp) | auto | titleLarge | "新建连接" |

### 2.3 表单内容

**表单字段:**

```kotlin
// 每个字段间距
val fieldSpacing = 24.dp

// 标签样式
Text(
    text = "名称",
    style = labelMedium,
    color = OnSurface
)

// 输入框样式
OutlinedTextField(
    value = name,
    onValueChange = { name = it },
    modifier = Modifier
        .fillMaxWidth()
        .height(56.dp),
    shape = RoundedCornerShape(8.dp)
)
```

| 字段 | 类型 | 必填 | 默认值 | 验证 |
|------|------|------|--------|------|
| 名称 | TextField | 是 | 空 | 1-50 字符 |
| 主机 | TextField | 是 | 空 | IP 或域名 |
| 端口 | TextField | 是 | 22 | 1-65535 |
| 用户名 | TextField | 是 | 空 | 1-50 字符 |
| 认证方式 | Radio | 是 | 密钥 | 密码/密钥 |
| 密码 | TextField | 条件 | 空 | 认证方式=密码时必填 |
| 密钥文件 | FilePicker | 条件 | - | 认证方式=密钥时必填 |
| 保存密码 | Checkbox | 否 | false | - |

**布局间距:**
- 表单整体左右内边距：24dp
- 字段间距：24dp
- 段落间距：32dp

### 2.4 底部操作栏

```kotlin
Row(
    modifier = Modifier
        .fillMaxWidth()
        .height(80.dp)
        .padding(16.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp)
) {
    // 取消按钮 (50% 宽度)
    OutlinedButton(
        modifier = Modifier.weight(1f),
        onClick = { }
    ) { Text("取消") }
    
    // 保存并连接 (50% 宽度)
    Button(
        modifier = Modifier.weight(1f),
        onClick = { }
    ) { Text("保存并连接") }
}
```

---

## 界面 3: 终端界面 (全屏模式)

### 3.1 整体布局

```
┌─────────────────────────────────────────┐
│  Top Bar (56dp)                         │
├─────────────────────────────────────────┤
│                                         │
│  Terminal (自适应，可滚动)              │
│                                         │
├─────────────────────────────────────────┤
│  Toolbar (56dp)                         │
└─────────────────────────────────────────┘
```

### 3.2 Top Bar

| 元素 | 位置 | 尺寸 | 颜色 | 说明 |
|------|------|------|------|------|
| 返回箭头 | 左侧 | 24x24dp | OnBackground | ← |
| 会话名称 | 左侧 (距箭头 16dp) | auto | titleMedium | "我的服务器" |
| 更多菜单 | 右侧 | 24x24dp | OnBackground | ⋮ 三点菜单 |

### 3.3 终端区域

**样式:**
- 背景色：#000000 (纯黑)
- 文本颜色：#F8F8F2 (Dracula White)
- 字体：JetBrains Mono
- 字号：14sp (可调节 10-24sp)
- 行高：20sp
- 左右内边距：12dp
- 上下内边距：16dp

**光标:**
- 颜色：Primary Blue (#2563EB)
- 宽度：2dp
- 闪烁动画：500ms 间隔

**选中文本:**
- 背景色：Primary Blue alpha 30%

### 3.4 工具栏 (Toolbar)

```
┌─────────────────────────────────────────┐
│ [Ctrl] [Alt] [Tab] [ESC] [📋] [⌨️]     │
└─────────────────────────────────────────┘
```

| 按钮 | 尺寸 | 颜色 | 说明 |
|------|------|------|------|
| Ctrl | 48x48dp | Surface / OnSurface | Ctrl 键 |
| Alt | 48x48dp | Surface / OnSurface | Alt 键 |
| Tab | 48x48dp | Surface / OnSurface | Tab 键 |
| ESC | 48x48dp | Surface / OnSurface | ESC 键 |
| 📋 剪贴板 | 48x48dp | Surface / Primary | 复制/粘贴 |
| ⌨️ 键盘 | 48x48dp | Surface / OnSurface | 显示/隐藏键盘 |

**工具栏背景:** Surface (#1E1E1E)

---

## 界面 4: 设置界面

### 4.1 整体布局

```
┌─────────────────────────────────────────┐
│  Top Bar (56dp)                         │
├─────────────────────────────────────────┤
│                                         │
│  Settings List (滚动)                   │
│                                         │
└─────────────────────────────────────────┘
```

### 4.2 Top Bar

| 元素 | 位置 | 尺寸 | 颜色 | 说明 |
|------|------|------|------|------|
| 标题 | 左侧 | auto | titleLarge | "设置" |

### 4.3 设置列表

**分组结构:**

```kotlin
// 设置分组
Section(header = "👤 账户") {
    SettingsItem(
        icon = Icons.Default.Person,
        title = "云同步",
        subtitle = "离线",
        trailing = { Switch(checked = false) }
    )
}
```

**设置项样式:**

| 元素 | 尺寸 | 颜色 | 说明 |
|------|------|------|------|
| 图标 | 24x24dp | Primary | 设置图标 |
| 标题 | bodyLarge | OnBackground | 设置名称 |
| 副标题 | bodySmall | DisabledText | 当前值/说明 |
| 尾部 | - | - | Switch/Text/Arrow |
| 高度 | 72dp | - | 列表项高度 |
| 内边距 | 16dp | - | 左右内边距 |

**设置分组:**

| 分组 | 图标 | 设置项 |
|------|------|--------|
| 👤 账户 | Person | 云同步 (开关) |
| 🎨 外观 | Palette | 主题 (选择器)<br>终端主题 (选择器)<br>字体大小 (滑块) |
| ⌨️ 键盘 | Keyboard | 快捷键映射 (页面)<br>虚拟键盘工具栏 (开关) |
| 🔒 安全 | Lock | 生物识别锁 (开关)<br>自动锁定 (选择器) |
| 📞 关于 | Info | 版本号 (文本) |

### 4.4 设置详情页 (如主题选择)

```
┌─────────────────────────────────────────┐
│  ← 主题                                 │
├─────────────────────────────────────────┤
│                                         │
│  ○ 跟随系统                            │
│  ○ 浅色                                │
│  ● 深色                                │
│                                         │
└─────────────────────────────────────────┘
```

**单选列表项:**
- 高度：56dp
- 选中状态：Primary 圆点
- 未选中：空心圆

---

## 状态说明

### 连接状态

| 状态 | 图标颜色 | 文本 | 说明 |
|------|----------|------|------|
| 已连接 | Secondary (#10B981) | 📶 已连接 | 正常连接 |
| 连接中 | Primary (#2563EB) | ⏳ 连接中 | 正在建立连接 |
| 断开 | Error (#EF4444) | 🔴 已断开 | 连接断开 |
| 错误 | Error (#EF4444) | ⚠️ 错误 | 连接失败 |

### 按钮状态

| 状态 | 背景色 | 文本颜色 | 说明 |
|------|--------|----------|------|
| 正常 | Primary Blue | White | 可点击 |
| 禁用 | Surface | DisabledText | 不可点击 |
| 按下 | Primary Blue darker | White | 点击反馈 |
| 加载 | Primary Blue + Spinner | White | 加载中 |

---

## 导出资源清单

### 图标列表

| 图标 | 尺寸 | 格式 | 用途 |
|------|------|------|------|
| menu | 24x24dp | SVG | 汉堡菜单 |
| settings | 24x24dp | SVG | 设置 |
| add | 24x24dp | SVG | 新建 |
| back | 24x24dp | SVG | 返回 |
| more_vert | 24x24dp | SVG | 更多 |
| keyboard | 24x24dp | SVG | 键盘 |
| clipboard | 24x24dp | SVG | 剪贴板 |
| person | 24x24dp | SVG | 账户 |
| palette | 24x24dp | SVG | 外观 |
| lock | 24x24dp | SVG | 安全 |
| info | 24x24dp | SVG | 关于 |
| connection | 64x64dp | SVG | 空白状态 |

### 颜色导出

```xml
<!-- res/values/colors.xml -->
<color name="primary_blue">#2563EB</color>
<color name="success_green">#10B981</color>
<color name="error_red">#EF4444</color>
<color name="background_dark">#0F0F0F</color>
<color name="surface_dark">#1E1E1E</color>
<color name="terminal_bg">#000000</color>
<color name="on_background">#F8F8F2</color>
<color name="disabled_text">#6B7280</color>
```

---

## 交互原型说明

### 页面流程

```
主界面
├─> 新建连接 (点击 + 按钮)
├─> 设置 (点击 ⚙️)
└─> 终端全屏 (点击连接项)
     └─> 返回主界面 (点击 ←)

新建连接
└─> 主界面 (保存并连接/取消)

设置
├─> 主题选择 (点击主题)
├─> 终端主题 (点击终端主题)
└─> 字体大小调节 (点击字体大小)
```

### 动画规范

| 过渡 | 时长 | 缓动 |
|------|------|------|
| 页面推入 | 300ms | FastOutSlowIn |
| 页面弹出 | 250ms | FastOutSlowIn |
| 列表项展开 | 300ms | LinearOutSlowIn |
| 按钮点击 | 150ms | FastOutSlowIn |

---

*标注版本：v1.0*
*创建时间：2026-04-05*
*负责人：UI/UX 设计师*
*状态：🔄 标注中 (等待 Figma 链接)*
