# 代码规范检查清单 (Code Review Checklist)

## 一、Kotlin Conventions 遵循 ✅

### 1.1 命名规范

**类和接口：**
- [ ] 类名使用 PascalCase（大驼峰）
- [ ] 接口名使用形容词或动词（如 `Repository`, `Service`）
- [ ] 例外：注解接口使用 `@Annotation` 格式
- [ ] 单例对象使用名词（如 `AppModule`, `Constants`）

**函数和属性：**
- [ ] 函数名使用 camelCase（小驼峰）
- [ ] 属性名使用 camelCase
- [ ] 常量名使用 UPPER_SNAKE_CASE
- [ ] 枚举值使用 PascalCase 或 UPPER_SNAKE_CASE（保持一致）
- [ ] 私有属性使用驼峰，不加下划线前缀

**包名：**
- [ ] 全部小写，使用点号分隔
- [ ] 遵循 `com.sshpad.app.layer.package` 结构
- [ ] 无下划线

**类型参数：**
- [ ] 单个大写字母（如 `T`, `K`, `V`, `R`）
- [ ] 有意义的名称（如 `User`, `Response`）

### 1.2 代码格式化

**缩进和空格：**
- [ ] 使用 4 个空格缩进（非 Tab）
- [ ] 操作符前后有空格
- [ ] 逗号后有空格
- [ ] 类/函数/控制块内空一行分隔逻辑段落

**括号和花括号：**
- [ ] 控制结构使用花括号（即使单行）
- [ ] 花括号前有空格
- [ ] 左花括号不换行（Kotlin 风格）

**行宽：**
- [ ] 单行不超过 120 字符
- [ ] 长链式调用适当换行

**Import 顺序：**
- [ ] Android imports
- [ ] Kotlin stdlib imports
- [ ] 第三方库 imports
- [ ] 项目内部 imports
- [ ] 每组之间空一行

### 1.3 文档和注释

**KDoc：**
- [ ] 公共 API 必须有 KDoc
- [ ] 包含 `@param`, `@return`, `@throws`
- [ ] 简洁描述用途，非实现细节

**行内注释：**
- [ ] 解释 "为什么" 而非 "做什么"
- [ ] 避免冗余注释
- [ ] 使用 `//` 单行注释
- [ ] 块注释使用 `/* ... */`

**TODO 注释：**
- [ ] 格式：`// TODO(姓名): 描述`
- [ ] 包含责任人
- [ ] 关联 Issue（如适用）

## 二、架构规范 ✅

### 2.1 Clean Architecture 分层

**Data 层：**
- [ ] 数据模型定义（`data class`）
- [ ] Repository 接口（domain 层定义）
- [ ] Repository 实现
- [ ] 数据源（Local/Remote）
- [ ] 数据类型转换（DTO ↔ Domain）

**Domain 层：**
- [ ] 纯 Kotlin 模块（无 Android 依赖）
- [ ] 业务逻辑封装
- [ ] Use Case 定义
- [ ] 领域模型定义

**Presentation 层：**
- [ ] UI 状态管理
- [ ] 用户交互处理
- [ ] 视图渲染（Composable）

### 2.2 MVVM 模式

**ViewModel：**
- [ ] 继承 `androidx.lifecycle.ViewModel`
- [ ] 持有 UI 状态（StateFlow/LiveData）
- [ ] 暴露只读状态流（`val uiState: StateFlow<UiState>`）
- [ ] 通过函数接收用户事件
- [ ] 不持有 Android Context
- [ ] 生命周期感知（使用 `viewModelScope`）

**State 设计：**
- [ ] 使用 `data class` 表示 UI 状态
- [ ] 状态不可变（`val`）
- [ ] 使用 sealed class 表示状态变化

**Event 处理：**
- [ ] 用户操作通过函数调用 ViewModel
- [ ] 事件处理委托给 Use Case 或 Repository
- [ ] 异常通过 State 或 Channel 暴露

### 2.3 依赖注入

**Koin 规范：**
- [ ] Module 定义在 `di` 包
- [ ] 使用 `single` / `factory` / `viewModel`
- [ ] 避免 `androidContext()` 滥用
- [ ] 模块按功能拆分（非单一巨大 Module）

**依赖注入原则：**
- [ ] 依赖抽象（接口）而非具体实现
- [ ] 构造函数注入优先
- [ ] 避免 Service Locator 模式

## 三、代码质量 ✅

### 3.1 函数设计

- [ ] 单一职责原则
- [ ] 函数长度 < 40 行
- [ ] 参数数量 < 5 个（过多考虑封装）
- [ ] 纯函数优先（无副作用）
- [ ] 适当的可见性修饰符（`private`, `internal`, `public`）

### 3.2 空安全

- [ ] 避免 `!!` 操作符
- [ ] 使用 `?.`, `?:`, `let` 处理 null
- [ ] 默认值使用 Elvis 操作符
- [ ] 返回类型明确是否可空

### 3.3 异常处理

- [ ] 使用 `Result` 封装可能失败的操作
- [ ] 捕获具体异常类型
- [ ] 避免空 catch 块
- [ ] 记录异常日志（带上下文）
- [ ] 不吞掉异常（至少记录或重新抛出）

### 3.4 协程使用

- [ ] 使用 `viewModelScope` / `lifecycleScope`
- [ ] 长时任务使用 `Dispatchers.IO` / `Dispatchers.Default`
- [ ] UI 更新在 `Dispatchers.Main`
- [ ] 使用 `supervisorScope` 处理子协程
- [ ] 避免 `GlobalScope`
- [ ] 正确处理协程取消

### 3.5 资源管理

- [ ] 使用 `use` 处理 Closeable 资源
- [ ] 及时取消 Flow 收集
- [ ] 释放监听器和回调
- [ ] 避免内存泄漏（WeakReference）

## 四、测试规范 ✅

### 4.1 单元测试

- [ ] 测试类命名：`ClassNameTest`
- [ ] 测试方法命名：`methodName_condition_expectedBehavior`
- [ ] 使用 AAA 模式（Arrange-Act-Assert）
- [ ] 测试独立，无依赖顺序
- [ ] 测试边界条件和异常情况
- [ ] Mock 外部依赖

### 4.2 测试覆盖率

- [ ] 业务逻辑覆盖率 > 70%
- [ ] 关键路径 100% 覆盖
- [ ] 边界条件测试
- [ ] 错误处理测试

### 4.3 集成测试

- [ ] Repository 层集成测试
- [ ] ViewModel 层集成测试
- [ ] UI 测试（使用 Compose Test）

## 五、Git 规范 ✅

### 5.1 提交信息

- [ ] 使用现在时态（"Add feature" 非 "Added feature"）
- [ ] 首字母大写
- [ ] 无句号结尾
- [ ] 50 字符内摘要
- [ ] 详细描述（如需要）

**格式：**
```
<type>(scope): subject

body (optional)

footer (optional)
```

**Type 类型：**
- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档更新
- `style`: 格式调整
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具

### 5.2 分支命名

- [ ] 功能分支：`feature/description`
- [ ] Bug 修复：`fix/description`
- [ ] 发布分支：`release/version`
- [ ] Hotfix：`hotfix/description`

### 5.3 PR 规范

- [ ] 分支基于最新主分支
- [ ] 提交历史整洁（squash 琐碎提交）
- [ ] 关联 Issue
- [ ] 填写 PR 模板

## 六、性能优化 ✅

- [ ] 避免过度 Composable 重组
- [ ] 使用 `remember` 缓存计算结果
- [ ] 大列表使用 `LazyColumn` / `LazyRow`
- [ ] 图片加载使用 Coil/Glide（非直接加载）
- [ ] 避免主线程阻塞操作
- [ ] 使用 `derivedStateOf` 优化状态派生

## 七、安全性 ✅

- [ ] 敏感数据不硬编码
- [ ] 密码/密钥使用加密存储
- [ ] 网络通信使用 HTTPS/TLS
- [ ] 日志不输出敏感信息
- [ ] 权限最小化原则

---

## 审查评分标准

| 等级 | 代码规范 | 安全检查 | 架构分层 | 单测覆盖 |
|------|----------|----------|----------|----------|
| ✅ 优秀 | 100% | 0 高危 | 清晰 | > 85% |
| ✓ 通过 | > 95% | 0 高危 | 明确 | > 70% |
| ⚠️ 待改进 | > 80% | 0 严重 | 基本清晰 | > 50% |
| ❌ 不通过 | < 80% | 有高危 | 混乱 | < 50% |

---

*审查人签名：_________________*

*审查日期：_________________*

*PR 编号：_________________*

---

*版本：v1.0*
*最后更新：2026-04-27*
