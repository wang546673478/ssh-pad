# SFTP 文件浏览器 UI 设计稿 - Week 8

## 文档信息
- **版本**: v1.0
- **创建日期**: 2026-05-11
- **设计师**: UI/UX 设计师
- **适用阶段**: Week 8 (2026-05-11 ~ 2026-05-17)
- **状态**: 📋 设计完成

---

## 一、设计概述

### 1.1 功能目标
提供直观、高效的 SFTP 文件传输功能，支持浏览、上传、下载、管理远程服务器文件。

### 1.2 设计原则
- **直观**: 文件列表清晰，操作入口明显
- **高效**: 批量操作、拖拽传输
- **安全**: 敏感操作确认、权限提示
- **一致**: 遵循 Material 3 和设计系统规范

---

## 二、SFTP 文件浏览器界面

### 2.1 整体布局（平板双栏）

```
┌──────────────────────────────────────────────────────────────────┐
│  ← SSH 连接    SFTP 文件浏览器                      [⚙️]  [⋮]   │
├────────────────┬─────────────────────────────────────────────────┤
│                │                                                 │
│  📁 本地文件   │          📁 远程服务器 (/home/admin)            │
│                │                                                 │
│  ┌──────────┐  │  ┌───────────────────────────────────────────┐ │
│  │ 📁 项目   │  │  │ 📁 ..        上级目录         2026-05-10 │ │
│  │ 📁 文档   │  │  │ 📁 projects  目录           2026-05-11 │ │
│  │ 📁 下载   │  │  │ 📁 logs      目录           2026-05-09 │ │
│  │ 📄 文件   │  │  │ 📄 config.yml 文件 2.3 KB   2026-05-10 │ │
│  │          │  │  │ 📄 README.md  文件 1.2 KB   2026-05-08 │ │
│  └──────────┘  │  │ 📄 backup.tar.gz文件 15 MB   2026-05-07 │ │
│                │  └───────────────────────────────────────────┘ │
│  /storage/    │                                                 │
│  emulated/0/  │  ┌───────────────────────────────────────────┐ │
│                │  │  3 个文件夹 | 3 个文件 | 17.5 MB 总计     │ │
│  [上传] [刷新] │  └───────────────────────────────────────────┘ │
├────────────────┴─────────────────────────────────────────────────┤
│  ⬆️ 上传 3.2 MB/s  |  ⬇️ 下载 1.8 MB/s  |  2 个传输任务         │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 手机单栏布局

```
┌─────────────────────────────────────┐
│  ← SFTP     远程服务器        [⋮]  │
├─────────────────────────────────────┤
│  [本地] [远程] (标签切换)           │
├─────────────────────────────────────┤
│                                     │
│  📁 ..         上级目录             │
│  📁 projects   目录           5-11  │
│  📁 logs       目录           5-09  │
│  📄 config.yml 文件 2.3 KB    5-10  │
│  📄 README.md  文件 1.2 KB    5-08  │
│                                     │
│  ┌────────────────────────────────┐ │
│  │ 3 文件夹 | 3 文件 | 17.5 MB    │ │
│  └────────────────────────────────┘ │
├─────────────────────────────────────┤
│  [⬆️ 上传] [⬇️ 下载] [🔄 刷新]      │
└─────────────────────────────────────┘
```

---

## 三、文件列表组件

### 3.1 列表项设计

#### 样式规格
```kotlin
// 列表项容器
height = 64.dp
padding = 16.dp
cornerRadius = 8.dp
backgroundColor = Transparent
hoverBackgroundColor = Color(0xFF1E1E1E)
selectedBackgroundColor = Color(0xFF2563EB).copy(alpha = 0.15f)

// 图标
iconSize = 32.dp
folderIconColor = Color(0xFF60A5FA)  // 蓝色文件夹
fileIconColor = Color(0xFF9CA3AF)    // 灰色文件

// 文本
fileNameFontSize = 16.sp
fileNameFontWeight = FontWeight.Medium
fileNameColor = Color(0xFFF8F8F2)
metaInfoFontSize = 12.sp
metaInfoFontWeight = FontWeight.Regular
metaInfoColor = Color(0xFF9CA3AF)

// 间距
iconTextSpacing = 16.dp
textMetaSpacing = 8.dp
```

#### 列表项组成
```
┌─────────────────────────────────────────────────────────┐
│  [📁]  projects           目录      2026-05-11 10:30   │
│   ↑     ↑                   ↑            ↑             │
│   │     │                   │            └─ 修改时间   │
│   │     │                   └─ 类型标签               │
│   │     └─ 文件名                                   │
│   └─ 文件类型图标                                    │
└─────────────────────────────────────────────────────────┘
```

### 3.2 文件类型图标

| 类型 | 图标 | 颜色 | 说明 |
|------|------|------|------|
| 文件夹 | 📁 | #60A5FA | 蓝色文件夹 |
| 普通文件 | 📄 | #9CA3AF | 灰色文件 |
| 代码文件 | 📝 | #F472B6 | 粉色（.kt, .java） |
| 配置文件 | ⚙️ | #FBBF24 | 黄色（.yml, .json） |
| 图片 | 🖼️ | #34D399 | 绿色（.png, .jpg） |
| 压缩包 | 📦 | #F87171 | 红色（.zip, .tar） |
| 上级目录 | 🔼 | #9CA3AF | 灰色返回 |

### 3.3 代码实现
```kotlin
@Composable
fun FileListItem(
    fileItem: SFTPFileItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onLongPress: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> Color(0xFF2563EB).copy(alpha = 0.15f)
        else -> Color.Transparent
    }
    
    val icon = when {
        fileItem.isDirectory -> Icons.Default.Folder
        else -> getFileIcon(fileItem.extension)
    }
    
    val iconColor = when {
        fileItem.isDirectory -> Color(0xFF60A5FA)
        else -> getFileIconColor(fileItem.extension)
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(backgroundColor)
            .clickable(
                onClick = onSelect,
                onLongClick = onLongPress
            )
            .combinedClickable(
                onClick = onSelect,
                onLongClick = onLongPress
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 文件图标
        Icon(
            imageVector = icon,
            contentDescription = if (fileItem.isDirectory) "文件夹" else "文件",
            tint = iconColor,
            modifier = Modifier.size(32.dp)
        )
        
        // 文件信息
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 文件名
            Text(
                text = fileItem.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFF8F8F2),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // 元信息
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = fileItem.typeLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Regular,
                    color = Color(0xFF9CA3AF)
                )
                
                if (!fileItem.isDirectory) {
                    Text(
                        text = formatFileSize(fileItem.size),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Regular,
                        color = Color(0xFF9CA3AF)
                    )
                }
                
                Text(
                    text = formatDate(fileItem.modifiedDate),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Regular,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
        
        // 更多操作（长按或悬停显示）
        if (isSelected || isHovered) {
            IconButton(onClick = { /* 显示操作菜单 */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "更多操作",
                    tint = Color(0xFF9CA3AF)
                )
            }
        }
    }
}
```

---

## 四、上传/下载进度 UI

### 4.1 传输进度指示器

#### 底部状态栏
```
┌─────────────────────────────────────────────────────────────────┐
│  ⬆️ 上传 3.2 MB/s  |  ⬇️ 下载 1.8 MB/s  |  2 个传输任务  [展开] │
└─────────────────────────────────────────────────────────────────┘
```

#### 展开的传输面板
```
┌──────────────────────────────────────────────────────────────┐
│  传输队列 (2)                                         [×]   │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  ⬆️ 上传中                                                  │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ 📄 backup.tar.gz                            45%       │ │
│  │ [█████████████░░░░░░░░░░░░░] 6.75 MB / 15 MB          │ │
│  │ 3.2 MB/s | 剩余 2.5 秒 | /home/admin/backup/          │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
│  ⬇️ 下载中                                                  │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ 📄 logs/app.log                             78%       │ │
│  │ [████████████████████████░] 3.9 MB / 5 MB             │ │
│  │ 1.8 MB/s | 剩余 0.6 秒 | /sdcard/Download/            │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
│  等待中 (1)                                                 │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ 📄 config.yml                                 等待中  │ │
│  │ 队列位置：1                                            │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
│                            [暂停全部]  [取消全部]  [关闭]   │
└──────────────────────────────────────────────────────────────┘
```

### 4.2 进度条设计

#### 样式规格
```kotlin
// 进度条容器
height = 4.dp
cornerRadius = 2.dp
backgroundColor = Color(0xFF333333)

// 进度填充
progressColor = Color(0xFF2563EB)  // Primary Blue
successColor = Color(0xFF10B981)   // Green
errorColor = Color(0xFFEF4444)     // Red

// 动画
animationDuration = 300.ms
animationEasing = LinearOutSlowIn
```

#### 进度状态
| 状态 | 颜色 | 说明 |
|------|------|------|
| 传输中 | #2563EB (蓝色) | 正常传输 |
| 完成 | #10B981 (绿色) | 传输成功 |
| 错误 | #EF4444 (红色) | 传输失败 |
| 暂停 | #F1FA8C (黄色) | 暂停状态 |

### 4.3 代码实现
```kotlin
@Composable
fun TransferProgressItem(
    transfer: TransferTask,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = transfer.bytesTransferred.toFloat() / transfer.totalBytes
    
    val statusColor = when (transfer.status) {
        TransferStatus.TRANSFERRING -> Color(0xFF2563EB)
        TransferStatus.COMPLETED -> Color(0xFF10B981)
        TransferStatus.ERROR -> Color(0xFFEF4444)
        TransferStatus.PAUSED -> Color(0xFFF1FA8C)
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        // 文件信息
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (transfer.isUpload) 
                                  Icons.Default.Upload 
                                else Icons.Default.Download,
                    contentDescription = if (transfer.isUpload) "上传" else "下载",
                    tint = statusColor,
                    modifier = Modifier.size(20.dp)
                )
                
                Text(
                    text = transfer.fileName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFF8F8F2),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = statusColor
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 进度条
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = statusColor,
            trackColor = Color(0xFF333333)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 传输详情
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${formatSize(transfer.bytesTransferred)} / ${formatSize(transfer.totalBytes)}",
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF)
            )
            
            if (transfer.status == TransferStatus.TRANSFERRING) {
                Text(
                    text = "${formatSpeed(transfer.speed)} | 剩余 ${transfer.estimatedTimeRemaining}",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
        
        // 取消按钮
        if (transfer.status == TransferStatus.TRANSFERRING || 
            transfer.status == TransferStatus.PAUSED) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onCancel,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("取消", fontSize = 12.sp)
            }
        }
    }
}
```

### 4.4 速度显示

#### 格式规范
```kotlin
// 速度单位
val speedUnits = listOf("B/s", "KB/s", "MB/s", "GB/s")

// 格式化示例
formatSpeed(3_200_000)  // "3.2 MB/s"
formatSpeed(1_800_000)  // "1.8 MB/s"
formatSpeed(512_000)    // "512 KB/s"
```

#### 显示位置
- **底部状态栏**: 实时上传/下载速度
- **进度项详情**: 单个任务速度
- **通知栏**: 后台传输时显示

---

## 五、交互设计

### 5.1 文件操作

#### 手势操作
| 手势 | 功能 | 反馈 |
|------|------|------|
| 单击 | 选择文件 | 高亮背景 |
| 双击 | 打开文件/进入文件夹 | 0.2s 过渡动画 |
| 长按 | 多选模式 | 震动反馈 + 复选框 |
| 右滑 | 返回上级目录 | 滑动动画 |
| 左滑（手机） | 显示操作菜单 | 滑出菜单 |

#### 右键菜单（平板）
```
┌─────────────────────────────┐
│  📥 下载                   │
│  📤 上传到...              │
│  ✏️ 重命名                 │
│  📋 复制                   │
│  ✂️ 剪切                   │
│  🗑️ 删除                   │
│  ────────────────────────  │
│  ℹ️ 属性                   │
└─────────────────────────────┘
```

### 5.2 拖拽传输

#### 平板支持
```
本地文件区  ──────→  远程服务器区
   (拖拽上传)

远程服务器区 ──────→  本地文件区
   (拖拽下载)
```

#### 拖拽反馈
```kotlin
// 拖拽进入
onEnter = { showDropZone Highlight() }

// 拖拽离开
onLeave = { hideDropZoneHighlight() }

// 放下
onDrop = { 
    startTransfer(files = droppedFiles)
    showSuccessToast()
}
```

### 5.3 批量操作

#### 多选模式
```
┌─────────────────────────────────────────────────────────────┐
│  ← 取消      已选择 3 项              [下载] [删除] [更多]  │
├─────────────────────────────────────────────────────────────┤
│  ☑️ 📁 projects                                             │
│  ☑️ 📄 config.yml                                           │
│  ☑️ 📄 README.md                                            │
│  ☐ 📄 backup.tar.gz                                         │
└─────────────────────────────────────────────────────────────┘
```

#### 批量操作
- 批量下载（打包为 ZIP）
- 批量上传
- 批量删除（确认对话框）
- 批量移动/复制

---

## 六、空状态和错误状态

### 6.1 空状态

#### 文件夹为空
```
┌─────────────────────────────────────┐
│                                     │
│           📁                        │
│                                     │
│        此文件夹为空                 │
│                                     │
│      [⬆️ 上传文件] [📝 新建文件]    │
│                                     │
└─────────────────────────────────────┘
```

#### 无网络连接
```
┌─────────────────────────────────────┐
│                                     │
│           📶                        │
│                                     │
│       无法连接到服务器              │
│                                     │
│      请检查网络连接后重试           │
│                                     │
│          [🔄 重试]                  │
│                                     │
└─────────────────────────────────────┘
```

### 6.2 错误状态

#### 权限不足
```
┌─────────────────────────────────────┐
│  ⚠️ 权限不足                     [×] │
├─────────────────────────────────────┤
│                                     │
│  您没有权限访问此文件夹             │
│                                     │
│  错误代码：EACCES (Permission       │
│  denied)                            │
│                                     │
│  [查看详情]  [尝试 Root]  [取消]    │
│                                     │
└─────────────────────────────────────┘
```

#### 传输失败
```
┌─────────────────────────────────────┐
│  ❌ 传输失败                     [×] │
├─────────────────────────────────────┤
│                                     │
│  📄 backup.tar.gz                   │
│                                     │
│  连接中断，传输未完成               │
│                                     │
│  已传输：6.75 MB / 15 MB            │
│  错误：Connection reset by peer     │
│                                     │
│  [⬇️ 恢复下载] [重试] [取消]        │
│                                     │
└─────────────────────────────────────┘
```

---

## 七、响应式布局

### 7.1 断点定义
```kotlin
val phoneBreakpoint = 600.dp
val tabletBreakpoint = 840.dp
val desktopBreakpoint = 1200.dp
```

### 7.2 布局策略

| 屏幕宽度 | 布局模式 | 特点 |
|----------|----------|------|
| < 600dp | 单栏标签式 | 本地/远程标签切换 |
| 600-840dp | 单栏分割式 | 上下分割 |
| 840-1200dp | 双栏并排 | 左右等宽 |
| > 1200dp | 三栏布局 | 本地 + 远程 + 详情 |

### 7.3 平板双栏优化
```kotlin
@Composable
fun SFTPTwoPaneLayout(
    localFiles: List<SFTPFileItem>,
    remoteFiles: List<SFTPFileItem>,
    onFileSelected: (SFTPFileItem) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // 本地文件区 (50%)
        FileBrowserPane(
            title = "本地文件",
            files = localFiles,
            modifier = Modifier.weight(1f),
            onFileSelected = onFileSelected,
            isLocal = true
        )
        
        Divider(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp),
            color = Color(0xFF333333)
        )
        
        // 远程文件区 (50%)
        FileBrowserPane(
            title = "远程服务器",
            files = remoteFiles,
            modifier = Modifier.weight(1f),
            onFileSelected = onFileSelected,
            isLocal = false
        )
    }
}
```

---

## 八、键盘快捷键

### 8.1 文件操作快捷键

| 快捷键 | 功能 | 平台 |
|--------|------|------|
| Ctrl+U | 上传文件 | 全平台 |
| Ctrl+D | 下载文件 | 全平台 |
| F2 | 重命名 | 全平台 |
| Delete | 删除 | 全平台 |
| Ctrl+A | 全选 | 全平台 |
| Ctrl+R | 刷新 | 全平台 |
| Backspace | 上级目录 | 全平台 |
| Enter | 打开/进入 | 全平台 |

### 8.2 Compose 实现
```kotlin
Modifier.keyboardShortcut(
    key = Key.U,
    modifiers = KeyModifiers.Ctrl,
    onShortcut = { viewModel.showUploadDialog() }
)

Modifier.keyboardShortcut(
    key = Key.F2,
    onShortcut = { viewModel.startRename() }
)

Modifier.keyboardShortcut(
    key = Key.Delete,
    onShortcut = { viewModel.deleteSelected() }
)
```

---

## 九、可访问性

### 9.1 无障碍支持
- ✅ 所有图标按钮有 contentDescription
- ✅ 文件类型通过图标和文本双重表达
- ✅ 键盘导航支持（方向键浏览）
- ✅ 最小点击区域 48x48dp
- ✅ 颜色对比度符合 WCAG AA
- ✅ 屏幕阅读器支持（朗读文件名和类型）

### 9.2 屏幕阅读器
```kotlin
ListItem(
    modifier = Modifier.semantics {
        contentDescription = "${fileItem.name}, " +
            "${if (fileItem.isDirectory) "文件夹" else "文件"}, " +
            "${formatFileSize(fileItem.size)}, " +
            "修改于 ${formatDate(fileItem.modifiedDate)}"
    }
)
```

---

## 十、设计稿交付

### 10.1 Figma 文件
- **文件路径**: (待上传)
- **页面**: SFTP File Browser
- **组件**: FileListItem, TransferProgress, FileBrowserPane

### 10.2 导出资源
| 资源 | 格式 | 尺寸 | 数量 |
|------|------|------|------|
| 文件类型图标 | SVG | 32x32dp | 8 个 |
| 操作图标 | SVG | 24x24dp | 12 个 |
| 状态图标 | SVG | 24x24dp | 6 个 |

---

## 十一、验收标准

### 11.1 视觉验收
- [ ] 文件列表清晰易读
- [ ] 图标识别度高
- [ ] 进度条动画流畅
- [ ] 颜色符合设计系统
- [ ] 间距遵循 4dp 网格

### 11.2 交互验收
- [ ] 文件选择响应及时
- [ ] 拖拽操作流畅
- [ ] 进度更新实时
- [ ] 快捷键可用
- [ ] 错误提示清晰

### 11.3 功能验收
- [ ] 支持大文件传输 (>1GB)
- [ ] 支持断点续传
- [ ] 支持批量操作
- [ ] 传输队列管理
- [ ] 后台传输支持

---

## 十二、开发实现建议

### 12.1 组件拆分
```
presentation/
├── components/
│   ├── sftp/
│   │   ├── FileListItem.kt
│   │   ├── FileBrowserPane.kt
│   │   ├── TransferProgress.kt
│   │   └── TransferQueuePanel.kt
├── screens/
│   └── SFTPScreen.kt
└── viewmodel/
    └── SFTPViewModel.kt
```

### 12.2 状态管理
```kotlin
data class SFTPScreenState(
    val localFiles: List<SFTPFileItem> = emptyList(),
    val remoteFiles: List<SFTPFileItem> = emptyList(),
    val currentLocalPath: String = "",
    val currentRemotePath: String = "",
    val selectedFiles: Set<String> = emptySet(),
    val transferTasks: List<TransferTask> = emptyList(),
    val isLoading: Boolean = false
)

class SFTPViewModel(
    // ...
    fun navigateUp(isLocal: Boolean) { }
    fun selectFile(fileId: String) { }
    fun startUpload(files: List<File>) { }
    fun startDownload(fileIds: List<String>) { }
    fun cancelTransfer(taskId: String) { }
)
```

### 12.3 性能优化
- 使用 LazyColumn 渲染文件列表
- 文件图标缓存
- 传输进度节流更新（100ms 间隔）
- 后台服务处理 SFTP 传输

---

## 附录

### A. 设计系统参考
- 色彩：design-system.md §1
- 字体：design-system.md §2
- 间距：design-system.md §3
- 圆角：design-system.md §4
- 组件：design-system.md §6

### B. 相关文件
- [design-system.md](./design-system.md)
- [screens.md](./screens.md)
- [DESIGN_QA.md](./DESIGN_QA.md)

### C. 联系方式
- 飞书群：Android SSH Client 项目组
- 设计答疑：每周三下午 14:00-16:00

---

*设计版本：v1.0*  
*创建时间：2026-05-11*  
*设计师：UI/UX 设计师*  
*状态：✅ 设计完成*
