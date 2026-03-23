# 源代码目录

## 项目结构 (规划)

```
app/
├── build.gradle.kts
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/sshpad/
│   │   │   ├── MainActivity.kt
│   │   │   ├── ui/
│   │   │   │   ├── theme/
│   │   │   │   ├── components/
│   │   │   │   └── screens/
│   │   │   ├── data/
│   │   │   │   ├── model/
│   │   │   │   ├── repository/
│   │   │   │   └── local/
│   │   │   ├── domain/
│   │   │   │   ├── usecase/
│   │   │   │   └── repository/
│   │   │   └── ssh/
│   │   │       ├── SshClient.kt
│   │   │       ├── SshSession.kt
│   │   │       └── SftpClient.kt
│   │   └── res/
│   └── test/
└── gradle/
```

## 开发环境

- Android Studio Hedgehog+
- JDK 17
- minSdk: 31 (Android 12)
- targetSdk: 34 (Android 14)

## 下一步

1. 创建 GitHub 仓库
2. 初始化 Gradle 项目
3. 搭建 Compose 基础框架
4. 实现 SSH 连接原型

---

*创建时间：2026-03-24*
*负责人：张工*
*状态：待开始*
