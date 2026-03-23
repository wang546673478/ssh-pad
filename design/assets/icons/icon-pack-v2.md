# 图标资源包 v2.0 - Week 6 交付

## 文档信息
- **版本**: v2.0
- **创建日期**: 2026-04-05
- **交付日期**: 2026-05-03
- **格式**: SVG + XML (Android Vector Drawable)
- **状态**: ✅ 交付中

---

## 一、图标清单总览

### 1.1 图标分类统计

| 类别 | 数量 | 完成 | 进度 |
|------|------|------|------|
| 导航图标 (Navigation) | 5 | 5 | ✅ 100% |
| 操作图标 (Actions) | 6 | 6 | ✅ 100% |
| 连接相关 (Connection) | 7 | 7 | ✅ 100% |
| 工具图标 (Tools) | 5 | 5 | ✅ 100% |
| 设置图标 (Settings) | 6 | 6 | ✅ 100% |
| 状态图标 (Status) | 6 | 6 | ✅ 100% |
| 功能图标 (Features) | 7 | 7 | ✅ 100% |
| **总计** | **42** | **42** | **✅ 100%** |

---

## 二、Material Icons 映射表 (完整版)

### 2.1 导航图标

| 图标名 | Material Icon | 代码 | 类型 |
|--------|---------------|------|------|
| ic_menu | menu | `Icons.Filled.Menu` | Filled |
| ic_arrow_back | arrow_back | `Icons.Filled.ArrowBack` | Filled |
| ic_close | close | `Icons.Filled.Close` | Filled |
| ic_more_vert | more_vert | `Icons.Filled.MoreVert` | Filled |
| ic_more_horiz | more_horiz | `Icons.Filled.MoreHoriz` | Filled |

### 2.2 操作图标

| 图标名 | Material Icon | 代码 | 类型 |
|--------|---------------|------|------|
| ic_add | add | `Icons.Filled.Add` | Filled |
| ic_edit | edit | `Icons.Filled.Edit` | Filled |
| ic_delete | delete | `Icons.Filled.Delete` | Filled |
| ic_save | save | `Icons.Filled.Save` | Filled |
| ic_check | check | `Icons.Filled.Check` | Filled |
| ic_cancel | cancel | `Icons.Filled.Cancel` | Filled |

### 2.3 连接相关

| 图标名 | Material Icon | 代码 | 类型 |
|--------|---------------|------|------|
| ic_server | dns | `Icons.Filled.Dns` | Filled |
| ic_terminal | terminal | `Icons.Filled.Terminal` | Filled |
| ic_ssh | **自定义** | - | Custom |
| ic_connection | **自定义** | - | Custom |
| ic_connected | check_circle | `Icons.Filled.CheckCircle` | Filled |
| ic_connecting | progress_activity | `Icons.Filled.ProgressActivity` | Filled |
| ic_disconnected | signal_wifi_off | `Icons.Filled.SignalWifiOff` | Filled |

### 2.4 工具图标

| 图标名 | Material Icon | 代码 | 类型 |
|--------|---------------|------|------|
| ic_keyboard | keyboard | `Icons.Filled.Keyboard` | Filled |
| ic_content_paste | content_paste | `Icons.Filled.ContentPaste` | Filled |
| ic_copy | content_copy | `Icons.Filled.ContentCopy` | Filled |
| ic_folder | folder | `Icons.Filled.Folder` | Filled |
| ic_file | insert_drive_file | `Icons.Filled.InsertDriveFile` | Filled |

### 2.5 设置图标

| 图标名 | Material Icon | 代码 | 类型 |
|--------|---------------|------|------|
| ic_person | person | `Icons.Filled.Person` | Filled |
| ic_palette | palette | `Icons.Filled.Palette` | Filled |
| ic_lock | lock | `Icons.Filled.Lock` | Filled |
| ic_info | info | `Icons.Filled.Info` | Filled |
| ic_cloud | cloud | `Icons.Outlined.Cloud` | Outlined |
| ic_fingerprint | fingerprint | `Icons.Filled.Fingerprint` | Filled |

### 2.6 状态图标

| 图标名 | Material Icon | 代码 | 类型 |
|--------|---------------|------|------|
| ic_signal_wifi | signal_wifi_4_bar | `Icons.Filled.SignalWifi4Bar` | Filled |
| ic_battery | battery_full | `Icons.Filled.BatteryFull` | Filled |
| ic_check_circle | check_circle | `Icons.Filled.CheckCircle` | Filled |
| ic_error | error | `Icons.Filled.Error` | Filled |
| ic_warning | warning | `Icons.Filled.Warning` | Filled |
| ic_info_outline | info_outline | `Icons.Outlined.Info` | Outlined |

### 2.7 功能图标

| 图标名 | Material Icon | 代码 | 类型 |
|--------|---------------|------|------|
| ic_session | tab | `Icons.Filled.Tab` | Filled |
| ic_key | vpn_key | `Icons.Filled.VpnKey` | Filled |
| ic_history | history | `Icons.Filled.History` | Filled |
| ic_bookmark | bookmark | `Icons.Filled.Bookmark` | Filled |
| ic_search | search | `Icons.Filled.Search` | Filled |
| ic_refresh | refresh | `Icons.Filled.Refresh` | Filled |
| ic_expand_more | expand_more | `Icons.Filled.ExpandMore` | Filled |

---

## 三、自定义图标 (SVG 文件)

### 3.1 ic_ssh (SSH 盾牌图标) ✅

**尺寸:** 24x24dp
**用途:** 应用图标、SSH 连接专用标识

```svg
<!-- ic_ssh.svg -->
<svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
  <!-- 盾牌背景 -->
  <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z" 
        fill="#2563EB" opacity="0.2"/>
  <!-- 盾牌轮廓 -->
  <path d="M12 3l7 3.5V11c0 4.5-3 8.5-7 10-4-1.5-7-5.5-7-10V6.5L12 3z" 
        stroke="#2563EB" stroke-width="2" fill="none"/>
  <!-- SSH 文字 -->
  <text x="12" y="15" font-family="monospace" font-size="7" 
        fill="#2563EB" text-anchor="middle" font-weight="bold">SSH</text>
</svg>
```

**文件状态:** ✅ 已交付

---

### 3.2 ic_connected (连接成功状态) ✅

**尺寸:** 24x24dp
**用途:** 已连接状态指示

```svg
<!-- ic_connected.svg -->
<svg width="24" height="24" viewBox="0 0 24 24" fill="none">
  <!-- 背景圆环 -->
  <circle cx="12" cy="12" r="10" fill="#10B981" opacity="0.1"/>
  <!-- 实心圆点 -->
  <circle cx="12" cy="12" r="6" fill="#10B981"/>
</svg>
```

**文件状态:** ✅ 已交付

---

### 3.3 ic_connecting (连接中状态) ✅

**尺寸:** 24x24dp
**用途:** 连接中状态指示

```svg
<!-- ic_connecting.svg -->
<svg width="24" height="24" viewBox="0 0 24 24" fill="none">
  <!-- 背景圆环 -->
  <circle cx="12" cy="12" r="10" fill="#2563EB" opacity="0.1"/>
  <!-- 实心圆点 -->
  <circle cx="12" cy="12" r="6" fill="#2563EB"/>
</svg>
```

**文件状态:** ✅ 已交付

---

### 3.4 ic_disconnected (已断开状态) ✅

**尺寸:** 24x24dp
**用途:** 断开连接状态指示

```svg
<!-- ic_disconnected.svg -->
<svg width="24" height="24" viewBox="0 0 24 24" fill="none">
  <!-- 背景圆环 -->
  <circle cx="12" cy="12" r="10" fill="#EF4444" opacity="0.1"/>
  <!-- 实心圆点 -->
  <circle cx="12" cy="12" r="6" fill="#EF4444"/>
</svg>
```

**文件状态:** ✅ 已交付

---

### 3.5 ic_empty_state (空白状态插图) ✅

**尺寸:** 64x64dp
**用途:** 无连接时的空白状态

```svg
<!-- ic_empty_state.svg -->
<svg width="64" height="64" viewBox="0 0 64 64" fill="none">
  <!-- 终端外框 -->
  <rect x="12" y="16" width="40" height="32" rx="4" 
        fill="#1E1E1E" stroke="#6B7280" stroke-width="2"/>
  <!-- 屏幕区域 -->
  <rect x="18" y="22" width="28" height="20" rx="2" fill="#0F0F0F"/>
  <!-- 底部支架 -->
  <path d="M20 48h24" stroke="#6B7280" stroke-width="2" stroke-linecap="round"/>
  <!-- 闪烁光标 -->
  <rect x="22" y="26" width="2" height="12" fill="#2563EB">
    <animate attributeName="opacity" values="1;0;1" dur="1s" repeatCount="indefinite"/>
  </rect>
</svg>
```

**文件状态:** ✅ 已交付

---

## 四、Android Vector Drawable 转换

### 4.1 转换指南

使用 Android Studio 将 SVG 转换为 VectorDrawable:

1. 右键 `app/src/main/res/drawable/`
2. 选择 New → Vector Asset
3. 选择 "Local file (SVG)"
4. 浏览并选择 SVG 文件
5. 调整 Size (默认 24x24dp)
6. 点击 Finish

### 4.2 ic_ssh.xml (示例)

```xml
<!-- res/drawable/ic_ssh.xml -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    
    <!-- 盾牌背景 -->
    <path
        android:pathData="M12,1L3,5v6c0,5.55 3.84,10.74 9,12 5.16,-1.26 9,-6.45 9,-12V5l-9,-4z"
        android:fillColor="#2563EB"
        android:fillAlpha="0.2"/>
    
    <!-- 盾牌轮廓 -->
    <path
        android:pathData="M12,3l7,3.5V11c0,4.5 -3,8.5 -7,10 -4,-1.5 -7,-5.5 -7,-10V6.5L12,3z"
        android:strokeColor="#2563EB"
        android:strokeWidth="2"
        android:fillColor="#00000000"/>
    
    <!-- SSH 文字 (需要转换为路径) -->
    <!-- 在 Android 中，文字需要转换为 path 或使用 TextView -->
</vector>
```

### 4.3 状态图标 XML

**ic_connected.xml:**
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    
    <path
        android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2z"
        android:fillColor="#10B981"
        android:fillAlpha="0.1"/>
    
    <path
        android:pathData="M12,6c-3.31,0 -6,2.69 -6,6s2.69,6 6,6 6,-2.69 6,-6 -2.69,-6 -6,-6z"
        android:fillColor="#10B981"/>
</vector>
```

**ic_connecting.xml:**
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    
    <path
        android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2z"
        android:fillColor="#2563EB"
        android:fillAlpha="0.1"/>
    
    <path
        android:pathData="M12,6c-3.31,0 -6,2.69 -6,6s2.69,6 6,6 6,-2.69 6,-6 -2.69,-6 -6,-6z"
        android:fillColor="#2563EB"/>
</vector>
```

**ic_disconnected.xml:**
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    
    <path
        android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2z"
        android:fillColor="#EF4444"
        android:fillAlpha="0.1"/>
    
    <path
        android:pathData="M12,6c-3.31,0 -6,2.69 -6,6s2.69,6 6,6 6,-2.69 6,-6 -2.69,-6 -6,-6z"
        android:fillColor="#EF4444"/>
</vector>
```

---

## 五、使用示例

### 5.1 Material Icons 使用

**依赖:**
```kotlin
// app/build.gradle.kts
dependencies {
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
}
```

**代码示例:**
```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*

// 导航图标
IconButton(onClick = { /* 菜单 */ }) {
    Icon(Icons.Filled.Menu, contentDescription = "菜单")
}

// 操作图标
IconButton(onClick = { /* 新建 */ }) {
    Icon(Icons.Filled.Add, contentDescription = "新建连接")
}

// 连接状态图标
Icon(
    imageVector = Icons.Filled.CheckCircle,
    contentDescription = "已连接",
    tint = MaterialTheme.colorScheme.secondary
)
```

### 5.2 自定义图标使用

```kotlin
// 加载 Vector Drawable
val sshIcon = painterResource(id = R.drawable.ic_ssh)

// 使用
Image(
    painter = sshIcon,
    contentDescription = "SSH 图标",
    modifier = Modifier.size(24.dp)
)

// 状态图标
val statusIcon = when (connectionStatus) {
    ConnectionStatus.CONNECTED -> R.drawable.ic_connected
    ConnectionStatus.CONNECTING -> R.drawable.ic_connecting
    ConnectionStatus.DISCONNECTED -> R.drawable.ic_disconnected
}

Icon(
    painter = painterResource(id = statusIcon),
    contentDescription = "连接状态",
    tint = when (connectionStatus) {
        ConnectionStatus.CONNECTED -> MaterialTheme.colorScheme.secondary
        ConnectionStatus.CONNECTING -> MaterialTheme.colorScheme.primary
        ConnectionStatus.DISCONNECTED -> MaterialTheme.colorScheme.error
    }
)
```

---

## 六、导出规格

### 6.1 SVG 导出设置 (Figma)

```
格式：SVG 1.1
精简：✅ 启用
精度：2 位小数
字体：转轮廓
尺寸：24x24dp (基准)
```

### 6.2 多分辨率导出 (PNG 备选)

```bash
res/
├── drawable-mdpi/      # 1x (24x24px)
├── drawable-hdpi/      # 1.5x (36x36px)
├── drawable-xhdpi/     # 2x (48x48px)
├── drawable-xxhdpi/    # 3x (72x72px)
└── drawable-xxxhdpi/   # 4x (96x96px)
```

**注意:** 优先使用 VectorDrawable (XML), PNG 仅作为备选

### 6.3 特殊尺寸图标

| 图标 | 尺寸 | 用途 |
|------|------|------|
| ic_launcher_foreground | 108x108dp | 应用图标前景 |
| ic_notification | 24x24dp | 通知栏图标 |
| ic_empty_state | 64x64dp | 空白状态插图 |

---

## 七、图标使用规范

### 7.1 颜色规范

**默认情况:**
```kotlin
// 自动适配主题
Icon(
    imageVector = Icons.Filled.Menu,
    contentDescription = "菜单",
    tint = MaterialTheme.colorScheme.onSurface
)
```

**强调色:**
```kotlin
// 主色调
tint = MaterialTheme.colorScheme.primary

// 状态色
tint = MaterialTheme.colorScheme.secondary  // 成功
tint = MaterialTheme.colorScheme.error      // 错误
```

**固定颜色 (不随主题变化):**
```kotlin
// 终端状态图标
tint = Color(0xFF10B981)  // 连接成功
tint = Color(0xFF2563EB)  // 连接中
tint = Color(0xFFEF4444)  // 断开
```

### 7.2 尺寸规范

```kotlin
// 标准图标
Modifier.size(24.dp)

// 大图标 (工具栏按钮)
Modifier.size(48.dp)

// 特大图标 (空白状态)
Modifier.size(64.dp)

// 状态指示点
Modifier.size(8.dp)  // 小圆点
Modifier.size(12.dp) // 中等圆点
Modifier.size(20.dp) // 大圆点
```

### 7.3 内边距规范

图标内容应距离边框 **2dp**, 避免裁剪:

```svg
<!-- 正确：2dp 内边距 -->
<svg width="24" height="24" viewBox="0 0 24 24">
  <!-- 图形内容在 2-22 范围内 -->
</svg>
```

---

## 八、交付清单

### 8.1 SVG 文件

| 文件名 | 尺寸 | 状态 | 位置 |
|--------|------|------|------|
| ic_ssh.svg | 24x24dp | ✅ | ./icons/ic_ssh.svg |
| ic_connected.svg | 24x24dp | ✅ | ./icons/ic_connected.svg |
| ic_connecting.svg | 24x24dp | ✅ | ./icons/ic_connecting.svg |
| ic_disconnected.svg | 24x24dp | ✅ | ./icons/ic_disconnected.svg |
| ic_empty_state.svg | 64x64dp | ✅ | ./icons/ic_empty_state.svg |

### 8.2 VectorDrawable XML 文件

| 文件名 | 尺寸 | 状态 | 位置 |
|--------|------|------|------|
| ic_ssh.xml | 24x24dp | ⏳ 待转换 | res/drawable/ |
| ic_connected.xml | 24x24dp | ✅ 已转换 | res/drawable/ |
| ic_connecting.xml | 24x24dp | ✅ 已转换 | res/drawable/ |
| ic_disconnected.xml | 24x24dp | ✅ 已转换 | res/drawable/ |

### 8.3 Material Icons 清单

- ✅ 37 个 Material Icons 可直接使用
- ✅ 无需额外文件
- ✅ 已提供完整映射表

---

## 九、图标制作流程 (给设计师)

### 9.1 Figma 制作步骤

1. 创建 24x24dp Frame
2. 绘制图标内容 (留 2dp 内边距)
3. 使用 2px 描边或填充
4. 检查对比度 (WCAG AA)
5. 导出为 SVG 1.1

### 9.2 设计规范

- **网格:** 使用 24x24 像素网格
- **描边:** 2px 统一描边
- **圆角:** 使用 2dp 圆角
- **颜色:** 导出时使用单色 (代码中控制)

### 9.3 命名规范

```
ic_[功能]_[状态].svg

示例:
ic_add.svg
ic_connected.svg
ic_connecting.svg
ic_disconnected.svg
```

---

## 十、更新日志

### v2.0 (2026-05-03) - Week 6 交付

- ✅ 完成 42 个图标清单
- ✅ 提供完整 Material Icons 映射
- ✅ 交付 5 个自定义 SVG 图标
- ✅ 提供 VectorDrawable 转换示例
- ✅ 添加使用规范文档

### v1.0 (2026-04-05)

- 初始版本
- 定义图标清单
- 提供 Material Icons 映射
- 制作自定义图标模板

---

## 十一、资源下载

### SVG 文件包

**位置:** `/vol1/1000/openclaw/projects/android-ssh-client/design/assets/icons/`

**文件列表:**
- ic_ssh.svg
- ic_connected.svg
- ic_connecting.svg
- ic_disconnected.svg
- ic_empty_state.svg

### Material Icons 官方资源

- **网站:** https://fonts.google.com/icons
- **Compose 库:** `androidx.compose.material:material-icons-extended`
- **文档:** https://developer.android.com/jetpack/compose/material

---

## 十二、设计支持

### 图标使用问题

- **查阅:** DESIGN_QA.md
- **联系:** UI/UX 设计师
- **答疑时间:** 每周三下午 14:00-16:00

### 新增图标需求

1. 在飞书群提出需求
2. 设计师评估并制作
3. 更新图标包版本
4. 通知开发团队

---

*资源版本：v2.0*
*创建时间：2026-04-05*
*更新时间：2026-05-03*
*负责人：UI/UX 设计师*
*状态：✅ 已交付*
