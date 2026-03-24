package com.sshpad.app.terminal.parser

import com.sshpad.app.data.model.TerminalLine

/**
 * 终端缓冲区实现
 * 
 * 集成 ANSI 解析器，维护终端状态和显示内容
 * 
 * @param width 终端宽度（字符数）
 * @param height 终端高度（行数）
 * @param scrollbackSize 滚动缓冲区大小
 */
class TerminalBuffer(
    val width: Int = 80,
    val height: Int = 24,
    val scrollbackSize: Int = 1000
) {
    // 主显示缓冲区
    private val lines = MutableList(height) { createEmptyLine() }
    
    // 滚动缓冲区（历史输出）
    private val scrollbackBuffer = mutableListOf<String>()
    
    // 光标位置（0-indexed）
    var cursorRow: Int = 0
        private set
    var cursorCol: Int = 0
        private set
    
    // 保存的光标状态
    private var savedCursorRow: Int = 0
    private var savedCursorCol: Int = 0
    
    // 当前文本属性
    private var currentAttributes: TextAttributes = TextAttributes()
    
    // ANSI 解析器
    private val ansiParser = AnsiParser()
    
    init {
        // 设置解析器监听器
        ansiParser.listener = object : AnsiParserListenerAdapter() {
            override fun onText(text: String, attributes: TextAttributes) {
                renderText(text, attributes)
            }
            
            override fun onCursorMove(deltaX: Int, deltaY: Int) {
                moveCursor(deltaX, deltaY)
            }
            
            override fun onCursorPosition(row: Int, col: Int) {
                setCursorPosition(row, col)
            }
            
            override fun onCursorHorizontalAbsolute(col: Int) {
                cursorCol = col.coerceIn(0, width - 1)
            }
            
            override fun onCursorNextLine(lines: Int) {
                cursorRow = (cursorRow + lines).coerceIn(0, height - 1)
                cursorCol = 0
            }
            
            override fun onCursorPreviousLine(lines: Int) {
                cursorRow = (cursorRow - lines).coerceIn(0, height - 1)
                cursorCol = 0
            }
            
            override fun onEraseDisplay(mode: EraseMode) {
                eraseDisplay(mode)
            }
            
            override fun onEraseLine(mode: EraseMode) {
                eraseLine(mode)
            }
            
            override fun onScrollUp(lines: Int) {
                scrollUp(lines)
            }
            
            override fun onScrollDown(lines: Int) {
                scrollDown(lines)
            }
            
            override fun onAttributesChanged(attributes: TextAttributes) {
                currentAttributes = attributes
            }
            
            override fun onReverseLineFeed() {
                // 反向换行：光标上移一行，如果已在顶部则向上滚动
                if (cursorRow > 0) {
                    cursorRow--
                } else {
                    scrollUp(1)
                }
            }
            
            override fun onSaveCursor() {
                savedCursorRow = cursorRow
                savedCursorCol = cursorCol
            }
            
            override fun onRestoreCursor() {
                cursorRow = savedCursorRow
                cursorCol = savedCursorCol
            }
        }
    }
    
    /**
     * 处理输入文本
     */
    fun processText(text: String) {
        ansiParser.processText(text)
    }
    
    /**
     * 获取指定行的内容
     */
    fun getLine(row: Int): TerminalLine {
        return lines[row].toTerminalLine()
    }
    
    /**
     * 获取所有显示行
     */
    fun getDisplayLines(): List<TerminalLine> {
        return lines.map { it.toTerminalLine() }
    }
    
    /**
     * 获取滚动缓冲区内容
     */
    fun getScrollback(): List<String> {
        return scrollbackBuffer.toList()
    }
    
    /**
     * 重置终端
     */
    fun reset() {
        for (i in lines.indices) {
            lines[i] = createEmptyLine()
        }
        scrollbackBuffer.clear()
        cursorRow = 0
        cursorCol = 0
        ansiParser.reset()
    }
    
    // ========== 内部实现 ==========
    
    /**
     * 创建空行
     */
    private fun createEmptyLine(): StringBuilder {
        return StringBuilder(" ".repeat(width))
    }
    
    /**
     * 渲染文本到当前光标位置
     */
    private fun renderText(text: String, attributes: TextAttributes) {
        for (char in text) {
            if (char == '\n') {
                // 换行
                cursorRow = (cursorRow + 1).coerceIn(0, height - 1)
                cursorCol = 0
            } else if (char == '\r') {
                // 回车
                cursorCol = 0
            } else if (char == '\t') {
                // 制表符（每 8 个字符一个制表位）
                val nextTabStop = ((cursorCol / 8) + 1) * 8
                cursorCol = nextTabStop.coerceIn(0, width - 1)
            } else if (char == '\b' || char == '\u007f') {
                // 退格
                cursorCol = (cursorCol - 1).coerceIn(0, width - 1)
            } else {
                // 普通字符
                if (cursorCol < width) {
                    lines[cursorRow].setCharAt(cursorCol, char)
                    cursorCol++
                    
                    // 如果到达行尾，自动换行
                    if (cursorCol >= width) {
                        cursorCol = 0
                        cursorRow = (cursorRow + 1).coerceIn(0, height - 1)
                    }
                }
            }
        }
    }
    
    /**
     * 移动光标
     */
    private fun moveCursor(deltaX: Int, deltaY: Int) {
        cursorCol = (cursorCol + deltaX).coerceIn(0, width - 1)
        cursorRow = (cursorRow + deltaY).coerceIn(0, height - 1)
    }
    
    /**
     * 设置光标位置
     */
    private fun setCursorPosition(row: Int, col: Int) {
        cursorRow = row.coerceIn(0, height - 1)
        cursorCol = col.coerceIn(0, width - 1)
    }
    
    /**
     * 清除显示
     */
    private fun eraseDisplay(mode: EraseMode) {
        when (mode) {
            EraseMode.CURSOR_TO_END -> {
                // 清除从光标到行尾
                for (c in cursorCol until width) {
                    lines[cursorRow].setCharAt(c, ' ')
                }
                // 清除下面的所有行
                for (r in (cursorRow + 1) until height) {
                    lines[r] = createEmptyLine()
                }
            }
            EraseMode.CURSOR_TO_START -> {
                // 清除上面的所有行
                for (r in 0 until cursorRow) {
                    lines[r] = createEmptyLine()
                }
                // 清除从行首到光标位置
                for (c in 0..cursorCol) {
                    lines[cursorRow].setCharAt(c, ' ')
                }
            }
            EraseMode.FULL -> {
                // 清除所有行
                for (r in 0 until height) {
                    lines[r] = createEmptyLine()
                }
                cursorRow = 0
                cursorCol = 0
            }
            EraseMode.FULL_WITH_SCROLLBACK -> {
                // 清除所有行和滚动缓冲
                scrollbackBuffer.clear()
                for (r in 0 until height) {
                    lines[r] = createEmptyLine()
                }
                cursorRow = 0
                cursorCol = 0
            }
        }
    }
    
    /**
     * 清除行
     */
    private fun eraseLine(mode: EraseMode) {
        when (mode) {
            EraseMode.CURSOR_TO_END -> {
                for (c in cursorCol until width) {
                    lines[cursorRow].setCharAt(c, ' ')
                }
            }
            EraseMode.CURSOR_TO_START -> {
                for (c in 0..cursorCol) {
                    lines[cursorRow].setCharAt(c, ' ')
                }
            }
            EraseMode.FULL -> {
                lines[cursorRow] = createEmptyLine()
            }
            EraseMode.FULL_WITH_SCROLLBACK -> {
                // 清除行并清空滚动缓冲（对于 EL 命令，此模式等同于 FULL）
                lines[cursorRow] = createEmptyLine()
            }
        }
    }
    
    /**
     * 向上滚动
     */
    private fun scrollUp(lines: Int) {
        for (i in 0 until lines) {
            // 将第一行添加到滚动缓冲
            if (scrollbackBuffer.size >= scrollbackSize) {
                scrollbackBuffer.removeAt(0)
            }
            scrollbackBuffer.add(this.lines[0].toString())
            
            // 删除第一行，在末尾添加新行
            this.lines.removeAt(0)
            this.lines.add(createEmptyLine())
        }
    }
    
    /**
     * 向下滚动
     */
    private fun scrollDown(lines: Int) {
        for (i in 0 until lines) {
            // 在开头插入空行
            this.lines.add(0, createEmptyLine())
            
            // 删除最后一行
            this.lines.removeAt(this.lines.size - 1)
        }
    }
    
    /**
     * 将 StringBuilder 转换为 TerminalLine
     */
    private fun StringBuilder.toTerminalLine(): TerminalLine {
        // TODO: 实现样式信息
        return TerminalLine(toString())
    }
}
