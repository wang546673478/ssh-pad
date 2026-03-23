package com.sshpad.app.util

/**
 * 应用常量定义
 * 避免魔法数字，提高代码可维护性
 */
object AppConstants {
    
    // ============ SSH 连接相关 ============
    
    /** SSH 默认端口 */
    const val SSH_DEFAULT_PORT = 22
    
    /** SSH 连接超时时间（毫秒） */
    const val SSH_CONNECT_TIMEOUT_MS = 30_000L
    
    /** SSH 认证超时时间（毫秒） */
    const val SSH_AUTH_TIMEOUT_MS = 30_000L
    
    /** SSH 通道打开超时时间（毫秒） */
    const val SSH_CHANNEL_OPEN_TIMEOUT_MS = 5_000L
    
    /** SSH 命令执行超时时间（毫秒） */
    const val SSH_COMMAND_TIMEOUT_MS = 30_000L
    
    /** SSH Keep-Alive 默认间隔（秒） */
    const val SSH_KEEPALIVE_INTERVAL_SECONDS = 60
    
    /** SSH Keep-Alive 响应超时倍数 */
    const val SSH_KEEPALIVE_TIMEOUT_MULTIPLIER = 3
    
    // ============ 密钥管理相关 ============
    
    /** Android Keystore 密钥别名前缀 */
    const val KEYSTORE_KEY_ALIAS_PREFIX = "ssh_key_"
    
    /** ECDSA 密钥大小（P-256 曲线） */
    const val ECDSA_KEY_SIZE = 256
    
    // ============ UI 间距相关 (dp) ============
    
    /** 大间距：16dp */
    const val SPACING_LARGE = 16
    
    /** 中间距：8dp */
    const val SPACING_MEDIUM = 8
    
    /** 小间距：4dp */
    const val SPACING_SMALL = 4
    
    /** 微小间距：2dp */
    const val SPACING_EXTRA_SMALL = 2
    
    // ============ UI 高度相关 (dp) ============
    
    /** 卡片默认 elevation */
    const val CARD_ELEVATION_DEFAULT = 4
    
    /** 卡片低 elevation */
    const val CARD_ELEVATION_LOW = 2
}
