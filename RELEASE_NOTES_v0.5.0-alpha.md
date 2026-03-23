# SSH-Pad v0.5.0-alpha 发布说明

**发布日期：** 2026-06-03  
**版本类型：** Alpha Pre-release  
**GitHub Release:** https://github.com/wang546673478/ssh-pad/releases/tag/v0.5.0-alpha

---

## 🎉 Alpha 版本发布

这是 SSH-Pad 的首个 Alpha 版本，标志着 Android SSH 客户端原型开发完成！

## ✨ 主要功能

### SSH 连接管理
- ✅ 支持密码认证连接 SSH 服务器
- ✅ 支持私钥认证连接 SSH 服务器
- ✅ 严格的服务器密钥验证（防止 MITM 攻击）
- ✅ 连接状态管理（连接中、认证中、已连接、错误）
- ✅ Keep-Alive 心跳机制
- ✅ 可配置的连接超时和认证超时

### 安全存储
- ✅ 使用 Android EncryptedSharedPreferences 加密存储凭据
- ✅ AES-256-GCM 加密值
- ✅ AES-256-SIV 加密键
- ✅ 支持硬件级安全（Keystore 后端）
- ✅ 自动密钥轮换支持

### 终端功能
- ✅ 交互式 Shell 会话
- ✅ 单命令执行
- ✅ ANSI 转义序列解析
- ✅ 终端尺寸调整支持

### 代码质量
- ✅ Clean Architecture 架构（数据层、领域层、表现层）
- ✅ 依赖注入（Hilt/Koin）
- ✅ 魔法数字提取为常量（P3-007）
- ✅ 完整的单元测试覆盖

## 🔧 技术改进

### P3-007: 魔法数字提取常量
- 新增 `AppConstants` 对象集中管理应用常量
- SSH 相关常量：默认端口、连接/认证/通道/命令超时时间
- 密钥管理常量：别名前缀、ECDSA 密钥大小
- Keep-Alive 相关常量：默认间隔、超时倍数
- UI 间距常量：大/中/小/微小间距
- UI Elevation 常量：默认/低 elevation

## 📱 系统要求

- **Android 版本：** Android 8.0 (API 26) 及以上
- **架构支持：** arm64-v8a, armeabi-v7a, x86_64
- **最低内存：** 512MB
- **推荐内存：** 1GB+

## 🚧 已知问题

### P0 - 严重
- 暂无

### P1 - 高优先级
- 暂无

### P2 - 中优先级
- [ ] 终端渲染性能优化（长输出时可能卡顿）
- [ ] 连接断开后的自动重连机制

### P3 - 低优先级
- [ ] 主题颜色自定义支持
- [ ] 批量导入/导出连接配置
- [ ] 会话历史记录功能

## 📋 安装说明

### 方法一：GitHub Releases 下载
1. 从 [Releases 页面](https://github.com/wang546673478/ssh-pad/releases/tag/v0.5.0-alpha) 下载 `ssh-pad-v0.5.0-alpha.apk`
2. 在 Android 设备上启用"未知来源"安装
3. 安装 APK 文件
4. 启动应用

### 方法二：从源码构建
```bash
git clone https://github.com/wang546673478/ssh-pad.git
cd ssh-pad
./gradlew assembleDebug
# APK 位于 app/build/outputs/apk/debug/
```

## 📖 使用指南

### 首次连接
1. 打开应用，点击"新建连接"
2. 输入 SSH 服务器信息：
   - 名称：连接的显示名称
   - 主机：SSH 服务器地址（IP 或域名）
   - 端口：默认为 22
   - 用户名：SSH 用户名
   - 认证类型：密码或私钥
3. 首次连接时会显示服务器指纹，请确认无误后接受
4. 连接成功后可以执行命令或进入交互式 Shell

### 安全提示
- ⚠️ 首次连接时请仔细核对服务器指纹
- ⚠️ 不要接受未知或不信任的服务器指纹
- ⚠️ 建议使用强密码或私钥认证
- ⚠️ 定期更新凭据

## 🐛 问题反馈

如遇问题或有建议，请通过以下方式反馈：

- **GitHub Issues:** https://github.com/wang546673478/ssh-pad/issues
- **邮件:** （待添加）
- **酷安评论区:** （待上架）

## 📝 更新日志

### v0.5.0-alpha (2026-06-03)
- Initial Alpha Release
- Clean Architecture 架构实现
- SSH 客户端核心功能完成
- 安全存储系统实现
- 终端解析器原型
- P3-007 魔法数字常量提取

---

## 🙏 致谢

感谢所有参与 Alpha 测试的用户和贡献者！

**技术栈:**
- Apache MINA sshd - SSH 客户端库
- Jetpack Compose - 现代 UI 框架
- Android Keystore - 安全存储
- Clean Architecture - 架构模式

---

**开发团队：** SSH-Pad Team  
**许可证:** MIT License
