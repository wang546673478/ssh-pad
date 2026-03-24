package com.sshpad.app.util

import android.util.Log
import com.sshpad.app.BuildConfig

/**
 * SafeLogger - 安全日志工具类
 * 
 * 安全特性:
 * - 自动过滤敏感字段（password, privateKey, secret, token, credential 等）
 * - 脱敏显示（如：p****d）
 * - 可配置日志级别
 * - Release 构建自动禁用敏感日志
 * 
 * 使用示例:
 * ```
 * SafeLogger.d("SSH", "Connecting with password: mySecretPassword")
 * // 输出：Connecting with password: p****d
 * 
 * SafeLogger.i("Auth", "Token: abc123xyz")
 * // 输出：Token: a***z
 * ```
 */
object SafeLogger {

    /**
     * 敏感字段关键词列表（不区分大小写匹配）
     */
    private val SENSITIVE_KEYWORDS = listOf(
        "password",
        "passwd",
        "pwd",
        "secret",
        "privatekey",
        "private_key",
        "passphrase",
        "token",
        "credential",
        "auth_token",
        "access_token",
        "refresh_token",
        "api_key",
        "apikey",
        "secret_key",
        "secretkey",
        "encryption_key",
        "signing_key",
        "bearer"
    )

    /**
     * 脱敏替换字符
     */
    private const val REDACTED = "*****"

    /**
     * 是否启用日志（BuildConfig.DEBUG 控制）
     */
    private var isDebuggable = BuildConfig.DEBUG

    /**
     * 日志级别配置
     */
    enum class LogLevel {
        VERBOSE,
        DEBUG,
        INFO,
        WARN,
        ERROR,
        NONE  // 禁用所有日志
    }

    /**
     * 当前日志级别（可通过 setLogLevel 动态调整）
     */
    private var currentLogLevel = LogLevel.DEBUG

    /**
     * 设置日志级别
     * 
     * @param level 日志级别
     */
    fun setLogLevel(level: LogLevel) {
        currentLogLevel = level
    }

    /**
     * 获取当前日志级别
     */
    fun getLogLevel(): LogLevel = currentLogLevel

    /**
     * 设置是否启用日志（用于运行时动态控制）
     * 
     * @param enabled true: 启用日志，false: 禁用日志
     */
    fun setEnabled(enabled: Boolean) {
        isDebuggable = enabled
    }

    /**
     * 是否应该记录指定级别的日志
     */
    private fun shouldLog(level: LogLevel): Boolean {
        if (!isDebuggable) return false
        if (currentLogLevel == LogLevel.NONE) return false
        return level.ordinal >= currentLogLevel.ordinal
    }

    /**
     * 脱敏处理消息中的敏感信息
     * 
     * 处理策略:
     * 1. 检测包含敏感关键词的键值对（如 password=xxx, token: xxx）
     * 2. 对敏感值进行脱敏（保留首尾字符）
     * 3. 对纯敏感值进行脱敏（长度>3 时保留首尾）
     * 
     * @param message 原始消息
     * @return 脱敏后的消息
     */
    private fun sanitize(message: String): String {
        var sanitized = message

        // 处理键值对模式：key=value 或 key: value
        SENSITIVE_KEYWORDS.forEach { keyword ->
            // 匹配 key=value (无空格)
            val regexEquals = Regex("(?i)($keyword)\\s*=\\s*([^\\s,&]+)")
            sanitized = sanitized.replace(regexEquals) { match ->
                val key = match.groupValues[1]
                val value = match.groupValues[2]
                "$key=${redactValue(value)}"
            }

            // 匹配 key: value (有空格)
            val regexColon = Regex("(?i)($keyword)\\s*:\\s*([^\\s,&]+)")
            sanitized = sanitized.replace(regexColon) { match ->
                val key = match.groupValues[1]
                val value = match.groupValues[2]
                "$key: ${redactValue(value)}"
            }
        }

        // 对过长的敏感值进行通用脱敏（避免意外泄露）
        // 检测类似 "password: mySecretPassword123" 的模式
        val longValueRegex = Regex(":\\s*([A-Za-z0-9+/=_\\-]{8,})")
        sanitized = sanitized.replace(longValueRegex) { match ->
            val value = match.groupValues[1]
            // 只对可能是敏感信息的值脱敏（包含字母和数字混合）
            if (value.any { it.isLetter() } && value.any { it.isDigit() }) {
                ": ${redactValue(value)}"
            } else {
                match.value
            }
        }

        return sanitized
    }

    /**
     * 对单个值进行脱敏
     * 
     * 脱敏规则:
     * - 长度 <= 2: 全部脱敏为 *****
     * - 长度 3-5: 保留首字符，其余脱敏
     * - 长度 > 5: 保留首尾各 1 字符，中间脱敏
     * 
     * @param value 原始值
     * @return 脱敏后的值
     */
    private fun redactValue(value: String): String {
        return when {
            value.length <= 2 -> REDACTED
            value.length <= 5 -> "${value[0]}${REDACTED}"
            else -> "${value[0]}${REDACTED}${value.last()}"
        }
    }

    /**
     * 记录 VERBOSE 级别日志
     * 
     * @param tag 日志标签
     * @param message 日志消息（自动脱敏）
     * @param throwable 可选的异常堆栈
     */
    fun v(tag: String, message: String, throwable: Throwable? = null) {
        if (!shouldLog(LogLevel.VERBOSE)) return
        val sanitized = sanitize(message)
        Log.v(tag, sanitized, throwable)
    }

    /**
     * 记录 DEBUG 级别日志
     * 
     * @param tag 日志标签
     * @param message 日志消息（自动脱敏）
     * @param throwable 可选的异常堆栈
     */
    fun d(tag: String, message: String, throwable: Throwable? = null) {
        if (!shouldLog(LogLevel.DEBUG)) return
        val sanitized = sanitize(message)
        Log.d(tag, sanitized, throwable)
    }

    /**
     * 记录 INFO 级别日志
     * 
     * @param tag 日志标签
     * @param message 日志消息（自动脱敏）
     * @param throwable 可选的异常堆栈
     */
    fun i(tag: String, message: String, throwable: Throwable? = null) {
        if (!shouldLog(LogLevel.INFO)) return
        val sanitized = sanitize(message)
        Log.i(tag, sanitized, throwable)
    }

    /**
     * 记录 WARN 级别日志
     * 
     * @param tag 日志标签
     * @param message 日志消息（自动脱敏）
     * @param throwable 可选的异常堆栈
     */
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (!shouldLog(LogLevel.WARN)) return
        val sanitized = sanitize(message)
        Log.w(tag, sanitized, throwable)
    }

    /**
     * 记录 ERROR 级别日志
     * 
     * @param tag 日志标签
     * @param message 日志消息（自动脱敏）
     * @param throwable 可选的异常堆栈
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (!shouldLog(LogLevel.ERROR)) return
        val sanitized = sanitize(message)
        Log.e(tag, sanitized, throwable)
    }

    /**
     * 记录 WTF (What a Terrible Failure) 级别日志
     * 
     * @param tag 日志标签
     * @param message 日志消息（自动脱敏）
     * @param throwable 可选的异常堆栈
     */
    fun wtf(tag: String, message: String, throwable: Throwable? = null) {
        if (!shouldLog(LogLevel.ERROR)) return
        val sanitized = sanitize(message)
        Log.wtf(tag, sanitized, throwable)
    }
}
