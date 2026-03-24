package com.sshpad.app.terminal.parser

/**
 * ANSI 转义序列解析器
 * 
 * 支持完整的 ANSI 256 色和光标控制功能：
 * - 8 色基础支持
 * - 16 色扩展
 * - 256 色模式（\u001b[38;5;N m）
 * - 真彩色（24-bit）支持
 * - 光标移动（CUU, CUD, CUF, CUB, CUP）
 * - 清除屏幕（ED, EL）
 * - 滚动（SU, SD）
 * - 文本属性（粗体、斜体、下划线、反色、隐藏、删除线）
 * 
 * 参考标准：ECMA-48, ISO/IEC 6429, VT100/VT220
 */
class AnsiParser {
    
    // 当前解析状态
    private var state = ParseState.IDLE
    private var params = mutableListOf<Int>()
    private var intermediateBytes = StringBuilder()
    
    // 当前文本属性
    var currentAttributes = TextAttributes()
        private set
    
    // 回调接口
    var listener: AnsiParserListener? = null
    
    /**
     * 处理输入文本，解析 ANSI 转义序列
     * @param text 输入文本
     */
    fun processText(text: String) {
        var i = 0
        while (i < text.length) {
            val char = text[i]
            
            when (state) {
                ParseState.IDLE -> {
                    if (char == '\u001b') {
                        // ESC 字符，进入转义序列
                        state = ParseState.ESCAPE
                        i++
                        continue
                    } else {
                        // 普通文本，直接输出
                        listener?.onText(text[i].toString(), currentAttributes.copy())
                    }
                }
                
                ParseState.ESCAPE -> {
                    when (char) {
                        '[' -> {
                            // CSI (Control Sequence Introducer) 序列
                            state = ParseState.CSI
                            params.clear()
                            intermediateBytes.clear()
                        }
                        'M' -> {
                            // 反向换行 (RI)
                            listener?.onReverseLineFeed()
                            state = ParseState.IDLE
                        }
                        '7' -> {
                            // 保存光标 (SC)
                            listener?.onSaveCursor()
                            state = ParseState.IDLE
                        }
                        '8' -> {
                            // 恢复光标 (RC)
                            listener?.onRestoreCursor()
                            state = ParseState.IDLE
                        }
                        else -> {
                            // 其他转义序列，暂时忽略
                            state = ParseState.IDLE
                        }
                    }
                    i++
                }
                
                ParseState.CSI -> {
                    when {
                        char.isDigit() -> {
                            // 累积参数数字
                            val currentNum = if (params.isEmpty() || params.last() == -1) 0 else params.last()
                            if (params.isEmpty() || params.last() == -1) {
                                params.add(char.digitToInt())
                            } else {
                                params[params.size - 1] = currentNum * 10 + char.digitToInt()
                            }
                        }
                        char == ';' -> {
                            // 参数分隔符
                            if (params.isEmpty() || params.last() != -1) {
                                params.add(-1) // 标记空参数
                            }
                        }
                        char.code in 0x20..0x2F -> {
                            // 中间字节
                            intermediateBytes.append(char)
                        }
                        char in '@'..'~' -> {
                            // 最终字节，执行命令
                            executeCsiCommand(char)
                            state = ParseState.IDLE
                        }
                        else -> {
                            // 无效序列，重置
                            state = ParseState.IDLE
                        }
                    }
                    i++
                }
            }
        }
    }
    
    /**
     * 执行 CSI 命令
     */
    private fun executeCsiCommand(finalByte: Char) {
        // 处理空参数为 0
        val normalizedParams = params.map { if (it == -1) 0 else it }
        
        when (finalByte) {
            // ========== 光标移动命令 ==========
            'A' -> {
                // CUU - Cursor Up: 光标上移
                val n = normalizedParams.firstOrNull() ?: 0
                val lines = if (n == 0) 1 else n
                listener?.onCursorMove(0, -lines)
            }
            'B' -> {
                // CUD - Cursor Down: 光标下移
                val n = normalizedParams.firstOrNull() ?: 0
                val lines = if (n == 0) 1 else n
                listener?.onCursorMove(0, lines)
            }
            'C' -> {
                // CUF - Cursor Forward: 光标右移
                val n = normalizedParams.firstOrNull() ?: 0
                val cols = if (n == 0) 1 else n
                listener?.onCursorMove(cols, 0)
            }
            'D' -> {
                // CUB - Cursor Backward: 光标左移
                val n = normalizedParams.firstOrNull() ?: 0
                val cols = if (n == 0) 1 else n
                listener?.onCursorMove(-cols, 0)
            }
            'E' -> {
                // CNL - Cursor Next Line: 光标移到下一行第 n 列
                val n = normalizedParams.firstOrNull() ?: 0
                val lines = if (n == 0) 1 else n
                listener?.onCursorNextLine(lines)
            }
            'F' -> {
                // CPL - Cursor Previous Line: 光标移到上一行第 n 列
                val n = normalizedParams.firstOrNull() ?: 0
                val lines = if (n == 0) 1 else n
                listener?.onCursorPreviousLine(lines)
            }
            'G' -> {
                // CHA - Cursor Horizontal Absolute: 光标移到第 n 列
                val n = normalizedParams.firstOrNull() ?: 0
                val col = if (n == 0) 1 else n
                listener?.onCursorHorizontalAbsolute(col - 1) // 转换为 0-indexed
            }
            'H' -> {
                // CUP - Cursor Position: 光标移到第 row 行第 col 列
                val row = normalizedParams.getOrNull(0) ?: 0
                val col = normalizedParams.getOrNull(1) ?: 0
                val actualRow = if (row == 0) 1 else row
                val actualCol = if (col == 0) 1 else col
                listener?.onCursorPosition(actualRow - 1, actualCol - 1) // 转换为 0-indexed
            }
            'f' -> {
                // HVP - Horizontal Vertical Position: 同 CUP
                val row = normalizedParams.getOrNull(0) ?: 0
                val col = normalizedParams.getOrNull(1) ?: 0
                val actualRow = if (row == 0) 1 else row
                val actualCol = if (col == 0) 1 else col
                listener?.onCursorPosition(actualRow - 1, actualCol - 1)
            }
            
            // ========== 清除命令 ==========
            'J' -> {
                // ED - Erase in Display
                val mode = normalizedParams.firstOrNull() ?: 0
                when (mode) {
                    0 -> listener?.onEraseDisplay(EraseMode.CURSOR_TO_END)
                    1 -> listener?.onEraseDisplay(EraseMode.CURSOR_TO_START)
                    2 -> listener?.onEraseDisplay(EraseMode.FULL)
                    3 -> listener?.onEraseDisplay(EraseMode.FULL_WITH_SCROLLBACK)
                }
            }
            'K' -> {
                // EL - Erase in Line
                val mode = normalizedParams.firstOrNull() ?: 0
                when (mode) {
                    0 -> listener?.onEraseLine(EraseMode.CURSOR_TO_END)
                    1 -> listener?.onEraseLine(EraseMode.CURSOR_TO_START)
                    2 -> listener?.onEraseLine(EraseMode.FULL)
                }
            }
            
            // ========== 滚动命令 ==========
            'S' -> {
                // SU - Scroll Up: 向上滚动 n 行
                val n = normalizedParams.firstOrNull() ?: 0
                val lines = if (n == 0) 1 else n
                listener?.onScrollUp(lines)
            }
            'T' -> {
                // SD - Scroll Down: 向下滚动 n 行
                val n = normalizedParams.firstOrNull() ?: 0
                val lines = if (n == 0) 1 else n
                listener?.onScrollDown(lines)
            }
            
            // ========== 文本属性命令 (SGR) ==========
            'm' -> {
                // SGR - Set Graphics Rendition
                applySgrParameters(normalizedParams)
            }
        }
    }
    
    /**
     * 应用 SGR (Select Graphic Rendition) 参数
     */
    private fun applySgrParameters(params: List<Int>) {
        if (params.isEmpty() || (params.size == 1 && params[0] == 0)) {
            // 重置所有属性
            currentAttributes = TextAttributes()
            listener?.onAttributesChanged(currentAttributes.copy())
            return
        }
        
        var i = 0
        while (i < params.size) {
            val code = params[i]
            
            when (code) {
                // ========== 重置 ==========
                0 -> {
                    currentAttributes = TextAttributes()
                }
                
                // ========== 文本效果 ==========
                1 -> currentAttributes.isBold = true
                2 -> currentAttributes.isFaint = true
                3 -> currentAttributes.isItalic = true
                4 -> currentAttributes.isUnderline = true
                5 -> currentAttributes.isBlink = true
                6 -> currentAttributes.isBlinkRapid = true
                7 -> currentAttributes.isReverse = true
                8 -> currentAttributes.isHidden = true
                9 -> currentAttributes.isStrikethrough = true
                
                // ========== 效果关闭 ==========
                21 -> currentAttributes.isBold = false // 粗体关闭 (某些终端)
                22 -> {
                    currentAttributes.isBold = false
                    currentAttributes.isFaint = false
                }
                23 -> currentAttributes.isItalic = false
                24 -> currentAttributes.isUnderline = false
                25 -> {
                    currentAttributes.isBlink = false
                    currentAttributes.isBlinkRapid = false
                }
                27 -> currentAttributes.isReverse = false
                28 -> currentAttributes.isHidden = false
                29 -> currentAttributes.isStrikethrough = false
                
                // ========== 前景色 - 基础 8 色 ==========
                30 -> currentAttributes.foregroundColor = AnsiColor.BLACK
                31 -> currentAttributes.foregroundColor = AnsiColor.RED
                32 -> currentAttributes.foregroundColor = AnsiColor.GREEN
                33 -> currentAttributes.foregroundColor = AnsiColor.YELLOW
                34 -> currentAttributes.foregroundColor = AnsiColor.BLUE
                35 -> currentAttributes.foregroundColor = AnsiColor.MAGENTA
                36 -> currentAttributes.foregroundColor = AnsiColor.CYAN
                37 -> currentAttributes.foregroundColor = AnsiColor.WHITE
                
                // ========== 前景色 - 高亮 8 色 (16 色模式) ==========
                90 -> currentAttributes.foregroundColor = AnsiColor.BRIGHT_BLACK
                91 -> currentAttributes.foregroundColor = AnsiColor.BRIGHT_RED
                92 -> currentAttributes.foregroundColor = AnsiColor.BRIGHT_GREEN
                93 -> currentAttributes.foregroundColor = AnsiColor.BRIGHT_YELLOW
                94 -> currentAttributes.foregroundColor = AnsiColor.BRIGHT_BLUE
                95 -> currentAttributes.foregroundColor = AnsiColor.BRIGHT_MAGENTA
                96 -> currentAttributes.foregroundColor = AnsiColor.BRIGHT_CYAN
                97 -> currentAttributes.foregroundColor = AnsiColor.BRIGHT_WHITE
                
                // ========== 背景色 - 基础 8 色 ==========
                40 -> currentAttributes.backgroundColor = AnsiColor.BLACK
                41 -> currentAttributes.backgroundColor = AnsiColor.RED
                42 -> currentAttributes.backgroundColor = AnsiColor.GREEN
                43 -> currentAttributes.backgroundColor = AnsiColor.YELLOW
                44 -> currentAttributes.backgroundColor = AnsiColor.BLUE
                45 -> currentAttributes.backgroundColor = AnsiColor.MAGENTA
                46 -> currentAttributes.backgroundColor = AnsiColor.CYAN
                47 -> currentAttributes.backgroundColor = AnsiColor.WHITE
                
                // ========== 背景色 - 高亮 8 色 (16 色模式) ==========
                100 -> currentAttributes.backgroundColor = AnsiColor.BRIGHT_BLACK
                101 -> currentAttributes.backgroundColor = AnsiColor.BRIGHT_RED
                102 -> currentAttributes.backgroundColor = AnsiColor.BRIGHT_GREEN
                103 -> currentAttributes.backgroundColor = AnsiColor.BRIGHT_YELLOW
                104 -> currentAttributes.backgroundColor = AnsiColor.BRIGHT_BLUE
                105 -> currentAttributes.backgroundColor = AnsiColor.BRIGHT_MAGENTA
                106 -> currentAttributes.backgroundColor = AnsiColor.BRIGHT_CYAN
                107 -> currentAttributes.backgroundColor = AnsiColor.BRIGHT_WHITE
                
                // ========== 256 色模式 ==========
                38 -> {
                    // 设置前景色为 256 色模式
                    if (i + 1 < params.size) {
                        val mode = params[i + 1]
                        when (mode) {
                            5 -> {
                                // 256 色模式：38;5;N
                                if (i + 2 < params.size) {
                                    val colorIndex = params[i + 2]
                                    currentAttributes.foregroundColor = AnsiColor.Color256(colorIndex)
                                    i += 2
                                }
                            }
                            2 -> {
                                // 真彩色 (24-bit): 38;2;R;G;B
                                if (i + 4 < params.size) {
                                    val r = params[i + 2]
                                    val g = params[i + 3]
                                    val b = params[i + 4]
                                    currentAttributes.foregroundColor = AnsiColor.TrueColor(r, g, b)
                                    i += 4
                                }
                            }
                        }
                    }
                }
                48 -> {
                    // 设置背景色为 256 色模式
                    if (i + 1 < params.size) {
                        val mode = params[i + 1]
                        when (mode) {
                            5 -> {
                                // 256 色模式：48;5;N
                                if (i + 2 < params.size) {
                                    val colorIndex = params[i + 2]
                                    currentAttributes.backgroundColor = AnsiColor.Color256(colorIndex)
                                    i += 2
                                }
                            }
                            2 -> {
                                // 真彩色 (24-bit): 48;2;R;G;B
                                if (i + 4 < params.size) {
                                    val r = params[i + 2]
                                    val g = params[i + 3]
                                    val b = params[i + 4]
                                    currentAttributes.backgroundColor = AnsiColor.TrueColor(r, g, b)
                                    i += 4
                                }
                            }
                        }
                    }
                }
                
                // ========== 默认前景色/背景色 ==========
                39 -> currentAttributes.foregroundColor = null
                49 -> currentAttributes.backgroundColor = null
            }
            
            i++
        }
        
        // 通知属性变化
        listener?.onAttributesChanged(currentAttributes.copy())
    }
    
    /**
     * 重置解析器状态
     */
    fun reset() {
        state = ParseState.IDLE
        params.clear()
        intermediateBytes.clear()
        currentAttributes = TextAttributes()
    }
    
    // ========== 解析状态枚举 ==========
    private enum class ParseState {
        IDLE,      // 空闲状态，处理普通文本
        ESCAPE,    // 已读取 ESC，等待后续字符
        CSI        // CSI 序列，累积参数
    }
}

/**
 * 文本属性
 */
data class TextAttributes(
    var foregroundColor: AnsiColor? = null,
    var backgroundColor: AnsiColor? = null,
    var isBold: Boolean = false,
    var isFaint: Boolean = false,
    var isItalic: Boolean = false,
    var isUnderline: Boolean = false,
    var isBlink: Boolean = false,
    var isBlinkRapid: Boolean = false,
    var isReverse: Boolean = false,
    var isHidden: Boolean = false,
    var isStrikethrough: Boolean = false
)

/**
 * ANSI 颜色表示
 */
sealed class AnsiColor {
    // 基础 8 色
    object BLACK : AnsiColor()
    object RED : AnsiColor()
    object GREEN : AnsiColor()
    object YELLOW : AnsiColor()
    object BLUE : AnsiColor()
    object MAGENTA : AnsiColor()
    object CYAN : AnsiColor()
    object WHITE : AnsiColor()
    
    // 高亮 8 色 (16 色模式)
    object BRIGHT_BLACK : AnsiColor()
    object BRIGHT_RED : AnsiColor()
    object BRIGHT_GREEN : AnsiColor()
    object BRIGHT_YELLOW : AnsiColor()
    object BRIGHT_BLUE : AnsiColor()
    object BRIGHT_MAGENTA : AnsiColor()
    object BRIGHT_CYAN : AnsiColor()
    object BRIGHT_WHITE : AnsiColor()
    
    // 256 色模式
    data class Color256(val index: Int) : AnsiColor() {
        init {
            require(index in 0..255) { "256 色索引必须在 0-255 范围内" }
        }
    }
    
    // 真彩色 (24-bit)
    data class TrueColor(val r: Int, val g: Int, val b: Int) : AnsiColor() {
        init {
            require(r in 0..255) { "R 值必须在 0-255 范围内" }
            require(g in 0..255) { "G 值必须在 0-255 范围内" }
            require(b in 0..255) { "B 值必须在 0-255 范围内" }
        }
    }
    
    /**
     * 转换为 Android Color
     */
    fun toAndroidColor(): Int {
        return when (this) {
            is BLACK -> android.graphics.Color.parseColor("#000000")
            is RED -> android.graphics.Color.parseColor("#CD0000")
            is GREEN -> android.graphics.Color.parseColor("#00CD00")
            is YELLOW -> android.graphics.Color.parseColor("#CDCD00")
            is BLUE -> android.graphics.Color.parseColor("#0000EE")
            is MAGENTA -> android.graphics.Color.parseColor("#CD00CD")
            is CYAN -> android.graphics.Color.parseColor("#00CDCD")
            is WHITE -> android.graphics.Color.parseColor("#E5E5E5")
            is BRIGHT_BLACK -> android.graphics.Color.parseColor("#808080")
            is BRIGHT_RED -> android.graphics.Color.parseColor("#FF0000")
            is BRIGHT_GREEN -> android.graphics.Color.parseColor("#00FF00")
            is BRIGHT_YELLOW -> android.graphics.Color.parseColor("#FFFF00")
            is BRIGHT_BLUE -> android.graphics.Color.parseColor("#5C5CFF")
            is BRIGHT_MAGENTA -> android.graphics.Color.parseColor("#FF00FF")
            is BRIGHT_CYAN -> android.graphics.Color.parseColor("#00FFFF")
            is BRIGHT_WHITE -> android.graphics.Color.parseColor("#FFFFFF")
            is Color256 -> color256ToAndroid(this.index)
            is TrueColor -> android.graphics.Color.rgb(this.r, this.g, this.b)
        }
    }
    
    /**
     * 将 256 色索引转换为 Android Color
     * 256 色方案：
     * - 0-15: 基础 16 色
     * - 16-231: 6x6x6 颜色立方体
     * - 232-255: 灰度
     */
    private fun color256ToAndroid(index: Int): Int {
        return when {
            index < 16 -> {
                // 前 16 色映射到基础色
                when (index) {
                    0 -> android.graphics.Color.parseColor("#000000")
                    1 -> android.graphics.Color.parseColor("#CD0000")
                    2 -> android.graphics.Color.parseColor("#00CD00")
                    3 -> android.graphics.Color.parseColor("#CDCD00")
                    4 -> android.graphics.Color.parseColor("#0000EE")
                    5 -> android.graphics.Color.parseColor("#CD00CD")
                    6 -> android.graphics.Color.parseColor("#00CDCD")
                    7 -> android.graphics.Color.parseColor("#E5E5E5")
                    8 -> android.graphics.Color.parseColor("#808080")
                    9 -> android.graphics.Color.parseColor("#FF0000")
                    10 -> android.graphics.Color.parseColor("#00FF00")
                    11 -> android.graphics.Color.parseColor("#FFFF00")
                    12 -> android.graphics.Color.parseColor("#5C5CFF")
                    13 -> android.graphics.Color.parseColor("#FF00FF")
                    14 -> android.graphics.Color.parseColor("#00FFFF")
                    15 -> android.graphics.Color.parseColor("#FFFFFF")
                    else -> android.graphics.Color.BLACK
                }
            }
            index < 232 -> {
                // 6x6x6 颜色立方体 (16-231)
                val adjustedIndex = index - 16
                val r = (adjustedIndex / 36) % 6
                val g = (adjustedIndex / 6) % 6
                val b = adjustedIndex % 6
                
                // 映射到 0, 95, 135, 175, 215, 255
                val colorValues = intArrayOf(0, 95, 135, 175, 215, 255)
                android.graphics.Color.rgb(
                    colorValues[r],
                    colorValues[g],
                    colorValues[b]
                )
            }
            else -> {
                // 灰度 (232-255)
                val grayValue = 8 + (index - 232) * 10
                android.graphics.Color.rgb(grayValue, grayValue, grayValue)
            }
        }
    }
}

/**
 * 清除模式枚举
 */
enum class EraseMode {
    CURSOR_TO_END,      // 从光标到末尾
    CURSOR_TO_START,    // 从光标到开头
    FULL,               // 全部清除
    FULL_WITH_SCROLLBACK // 清除全部包括滚动缓冲
}

/**
 * ANSI 解析器监听器接口
 */
interface AnsiParserListener {
    fun onText(text: String, attributes: TextAttributes)
    fun onCursorMove(deltaX: Int, deltaY: Int)
    fun onCursorPosition(row: Int, col: Int)
    fun onCursorHorizontalAbsolute(col: Int)
    fun onCursorNextLine(lines: Int)
    fun onCursorPreviousLine(lines: Int)
    fun onEraseDisplay(mode: EraseMode)
    fun onEraseLine(mode: EraseMode)
    fun onScrollUp(lines: Int)
    fun onScrollDown(lines: Int)
    fun onAttributesChanged(attributes: TextAttributes)
    fun onReverseLineFeed()
    fun onSaveCursor()
    fun onRestoreCursor()
}
