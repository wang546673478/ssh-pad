package com.sshpad.app.data.model

/**
 * Terminal session state
 */
data class TerminalSession(
    val connectionId: String,
    val sessionId: String = java.util.UUID.randomUUID().toString(),
    val title: String = "Terminal",
    val width: Int = 80,
    val height: Int = 24,
    val fontSize: Float = 14f,
    val theme: TerminalTheme = TerminalTheme.DARK,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Terminal color themes
     */
    enum class TerminalTheme {
        DARK,
        LIGHT,
        SOLARIZED_DARK,
        SOLARIZED_LIGHT,
        MONOKAI
    }
}

/**
 * Terminal buffer data
 */
data class TerminalBuffer(
    val lines: List<TerminalLine> = emptyList(),
    val cursorRow: Int = 0,
    val cursorCol: Int = 0,
    val scrollbackSize: Int = 1000
)

/**
 * Single terminal line with styling
 */
data class TerminalLine(
    val text: String = "",
    val styles: List<LineStyle> = emptyList()
) {
    data class LineStyle(
        val startIndex: Int,
        val endIndex: Int,
        val foregroundColor: Int? = null,
        val backgroundColor: Int? = null,
        val isBold: Boolean = false,
        val isUnderline: Boolean = false,
        val isItalic: Boolean = false
    )
}
