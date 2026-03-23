# 图标资源包 v1.0

## 文档信息
- **版本**: v1.0
- **创建日期**: 2026-04-05
- **格式**: SVG + XML (Android Vector Drawable)
- **状态**: 📋 准备中

---

## 图标清单

### 1. 导航图标 (Navigation)

| 图标名 | 尺寸 | 用途 | Figma 链接 |
|--------|------|------|------------|
| ic_menu | 24x24dp | 汉堡菜单 | |
| ic_arrow_back | 24x24dp | 返回箭头 | |
| ic_close | 24x24dp | 关闭 | |
| ic_more_vert | 24x24dp | 更多选项 | |
| ic_settings | 24x24dp | 设置 | |

### 2. 操作图标 (Actions)

| 图标名 | 尺寸 | 用途 | Figma 链接 |
|--------|------|------|------------|
| ic_add | 24x24dp | 新建/添加 | |
| ic_edit | 24x24dp | 编辑 | |
| ic_delete | 24x24dp | 删除 | |
| ic_save | 24x24dp | 保存 | |
| ic_check | 24x24dp | 确认/完成 | |
| ic_cancel | 24x24dp | 取消 | |

### 3. 连接相关 (Connection)

| 图标名 | 尺寸 | 用途 | Figma 链接 |
|--------|------|------|------------|
| ic_server | 24x24dp | 服务器 | |
| ic_terminal | 24x24dp | 终端 | |
| ic_ssh | 24x24dp | SSH 连接 | |
| ic_connection | 24x24dp | 连接状态 | |
| ic_connected | 24x24dp | 已连接 | |
| ic_disconnected | 24x24dp | 已断开 | |
| ic_connecting | 24x24dp | 连接中 | |

### 4. 工具图标 (Tools)

| 图标名 | 尺寸 | 用途 | Figma 链接 |
|--------|------|------|------------|
| ic_keyboard | 24x24dp | 键盘 | |
| ic_content_paste | 24x24dp | 剪贴板 | |
| ic_copy | 24x24dp | 复制 | |
| ic_folder | 24x24dp | 文件/文件夹 | |
| ic_file | 24x24dp | 文件 | |

### 5. 设置图标 (Settings)

| 图标名 | 尺寸 | 用途 | Figma 链接 |
|--------|------|------|------------|
| ic_person | 24x24dp | 账户 | |
| ic_palette | 24x24dp | 外观 | |
| ic_lock | 24x24dp | 安全 | |
| ic_info | 24x24dp | 关于 | |
| ic_cloud | 24x24dp | 云同步 | |
| ic_fingerprint | 24x24dp | 生物识别 | |

### 6. 状态图标 (Status)

| 图标名 | 尺寸 | 用途 | Figma 链接 |
|--------|------|------|------------|
| ic_signal_wifi | 24x24dp | 网络信号 | |
| ic_battery | 24x24dp | 电量 | |
| ic_check_circle | 24x24dp | 成功 | |
| ic_error | 24x24dp | 错误 | |
| ic_warning | 24x24dp | 警告 | |
| ic_info_outline | 24x24dp | 信息 | |

### 7. 功能图标 (Features)

| 图标名 | 尺寸 | 用途 | Figma 链接 |
|--------|------|------|------------|
| ic_session | 24x24dp | 会话 | |
| ic_key | 24x24dp | 密钥 | |
| ic_history | 24x24dp | 历史记录 | |
| ic_bookmark | 24x24dp | 收藏 | |

---

## Material Icons 使用指南

大部分图标可直接使用 Material Icons 库:

### 依赖

```kotlin
// build.gradle.kts
implementation("androidx.compose.material:material-icons-extended:1.6.0")
```

### 使用示例

```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*

// 填充图标
Icon(
    imageVector = Icons.Filled.Menu,
    contentDescription = "菜单"
)

// 轮廓图标
Icon(
    imageVector = Icons.Outlined.Cloud,
    contentDescription = "云同步"
)
```

### Material Icons 对应表

| 我们的命名 | Material Icon | 类型 |
|-----------|---------------|------|
| ic_menu | Icons.Filled.Menu | Filled |
| ic_arrow_back | Icons.Filled.ArrowBack | Filled |
| ic_close | Icons.Filled.Close | Filled |
| ic_more_vert | Icons.Filled.MoreVert | Filled |
| ic_settings | Icons.Filled.Settings | Filled |
| ic_add | Icons.Filled.Add | Filled |
| ic_edit | Icons.Filled.Edit | Filled |
| ic_delete | Icons.Filled.Delete | Filled |
| ic_save | Icons.Filled.Save | Filled |
| ic_check | Icons.Filled.Check | Filled |
| ic_cancel | Icons.Filled.Cancel | Filled |
| ic_server | Icons.Filled.Dns | Filled |
| ic_terminal | Icons.Filled.Terminal | Filled |
| ic_keyboard | Icons.Filled.Keyboard | Filled |
| ic_content_paste | Icons.Filled.ContentPaste | Filled |
| ic_folder | Icons.Filled.Folder | Filled |
| ic_person | Icons.Filled.Person | Filled |
| ic_palette | Icons.Filled.Palette | Filled |
| ic_lock | Icons.Filled.Lock | Filled |
| ic_info | Icons.Filled.Info | Filled |
| ic_cloud | Icons.Outlined.Cloud | Outlined |
| ic_fingerprint | Icons.Filled.Fingerprint | Filled |
| ic_check_circle | Icons.Filled.CheckCircle | Filled |
| ic_error | Icons.Filled.Error | Filled |
| ic_warning | Icons.Filled.Warning | Filled |
| ic_session | Icons.Filled.Tab | Filled |
| ic_key | Icons.Filled.VpnKey | Filled |
| ic_history | Icons.Filled.History | Filled |
| ic_bookmark | Icons.Filled.Bookmark | Filled |

---

## 自定义图标

以下图标需要自定义设计:

### ic_ssh (SSH 专用图标)

```svg
<!-- ic_ssh.svg -->
<svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
  <!-- 安全盾牌 + SSH 文字 -->
  <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z" 
        fill="#2563EB" opacity="0.2"/>
  <path d="M12 3l7 3.5V11c0 4.5-3 8.5-7 10-4-1.5-7-5.5-7-10V6.5L12 3z" 
        stroke="#2563EB" stroke-width="2" fill="none"/>
  <text x="12" y="15" font-family="monospace" font-size="7" 
        fill="#2563EB" text-anchor="middle" font-weight="bold">SSH</text>
</svg>
```

### ic_connection (连接状态组合图标)

提供三个状态版本:

**已连接:**
```svg
<!-- ic_connected.svg -->
<svg width="24" height="24" viewBox="0 0 24 24" fill="none">
  <circle cx="12" cy="12" r="10" fill="#10B981" opacity="0.1"/>
  <circle cx="12" cy="12" r="6" fill="#10B981"/>
</svg>
```

**连接中:**
```svg
<!-- ic_connecting.svg -->
<svg width="24" height="24" viewBox="0 0 24 24" fill="none">
  <circle cx="12" cy="12" r="10" fill="#2563EB" opacity="0.1"/>
  <circle cx="12" cy="12" r="6" fill="#2563EB"/>
</svg>
```

**已断开:**
```svg
<!-- ic_disconnected.svg -->
<svg width="24" height="24" viewBox="0 0 24 24" fill="none">
  <circle cx="12" cy="12" r="10" fill="#EF4444" opacity="0.1"/>
  <circle cx="12" cy="12" r="6" fill="#EF4444"/>
</svg>
```

### ic_empty_state (空白状态插图)

```svg
<!-- ic_empty_state.svg - 64x64dp -->
<svg width="64" height="64" viewBox="0 0 64 64" fill="none">
  <!-- 终端图标 -->
  <rect x="12" y="16" width="40" height="32" rx="4" 
        fill="#1E1E1E" stroke="#6B7280" stroke-width="2"/>
  <rect x="18" y="22" width="28" height="20" rx="2" fill="#0F0F0F"/>
  <path d="M20 48h24" stroke="#6B7280" stroke-width="2" stroke-linecap="round"/>
  <!-- 光标 -->
  <rect x="22" y="26" width="2" height="12" fill="#2563EB">
    <animate attributeName="opacity" values="1;0;1" dur="1s" repeatCount="indefinite"/>
  </rect>
</svg>
```

---

## Android Vector Drawable 格式

### 转换工具

使用 Android Studio 将 SVG 转换为 VectorDrawable:

1. 右键 `res/drawable` 文件夹
2. New → Vector Asset
3. 选择 Local file (SVG)
4. 调整尺寸 (24x24dp)
5. 完成

### 示例格式

```xml
<!-- res/drawable/ic_ssh.xml -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    
    <path
        android:pathData="M12,1L3,5v6c0,5.55 3.84,10.74 9,12 5.16,-1.26 9,-6.45 9,-12V5l-9,-4z"
        android:fillColor="#2563EB"
        android:fillAlpha="0.2"/>
    
    <path
        android:pathData="M12,3l7,3.5V11c0,4.5 -3,8.5 -7,10 -4,-1.5 -7,-5.5 -7,-10V6.5L12,3z"
        android:strokeColor="#2563EB"
        android:strokeWidth="2"
        android:fillColor="#00000000"/>
</vector>
```

---

## 导出规格

### SVG 导出设置 (Figma)

- 格式：SVG 1.1
- 精简：✅ 启用
- 精度：2 位小数
- 字体：转轮廓
- 尺寸：24x24dp (基准)

### 多分辨率导出 (如需 PNG)

```bash
# 目录结构
res/
├── drawable-mdpi/      # 1x (24x24px)
├── drawable-hdpi/      # 1.5x (36x36px)
├── drawable-xhdpi/     # 2x (48x48px)
├── drawable-xxhdpi/    # 3x (72x72px)
└── drawable-xxxhdpi/   # 4x (96x96px)
```

**注意:** 优先使用 VectorDrawable (XML), 只在必要时使用 PNG

---

## 使用规范

### 颜色

- 默认使用 `?attr/colorOnSurface` (自动适配主题)
- 强调色使用 `?attr/colorPrimary`
- 状态色使用定义好的颜色:
  - 成功：`#10B981`
  - 错误：`#EF4444`
  - 警告：`#F59E0B`
  - 信息：`#2563EB`

### 尺寸

- 标准图标：24x24dp
- 大图标：48x48dp (工具栏按钮)
- 特大图标：64x64dp (空白状态)

### 内边距

图标内容应距离边框 2dp, 避免裁剪:

```svg
<!-- 正确的内边距 -->
<svg width="24" height="24" viewBox="0 0 24 24">
  <!-- 图形内容距离边缘至少 2dp -->
</svg>
```

---

## 交付清单

| 图标 | SVG | XML | 状态 |
|------|-----|-----|------|
| ic_menu | ⬜ | ⬜ | 待制作 |
| ic_arrow_back | ⬜ | ⬜ | Material |
| ic_add | ⬜ | ⬜ | Material |
| ic_settings | ⬜ | ⬜ | Material |
| ic_ssh | ⬜ | ⬜ | 待制作 |
| ic_connected | ⬜ | ⬜ | 待制作 |
| ic_connecting | ⬜ | ⬜ | 待制作 |
| ic_disconnected | ⬜ | ⬜ | 待制作 |
| ic_empty_state | ⬜ | ⬜ | 待制作 |

---

## 更新日志

### v1.0 (2026-04-05)
- 初始版本
- 定义图标清单
- 提供 Material Icons 映射
- 制作自定义图标模板

---

*资源版本：v1.0*
*创建时间：2026-04-05*
*负责人：UI/UX 设计师*
*状态：📋 准备中*
