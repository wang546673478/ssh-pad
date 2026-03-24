package com.sshpad.app.terminal.parser

/**
 * ANSI 解析器监听器抽象实现
 * 
 * 提供默认空实现，子类可选择性重写需要的方法
 */
abstract class AnsiParserListenerAdapter : AnsiParserListener {
    override fun onText(text: String, attributes: TextAttributes) {
        // 默认不处理
    }

    override fun onCursorMove(deltaX: Int, deltaY: Int) {
        // 默认不处理
    }

    override fun onCursorPosition(row: Int, col: Int) {
        // 默认不处理
    }

    override fun onCursorHorizontalAbsolute(col: Int) {
        // 默认不处理
    }

    override fun onCursorNextLine(lines: Int) {
        // 默认不处理
    }

    override fun onCursorPreviousLine(lines: Int) {
        // 默认不处理
    }

    override fun onEraseDisplay(mode: EraseMode) {
        // 默认不处理
    }

    override fun onEraseLine(mode: EraseMode) {
        // 默认不处理
    }

    override fun onScrollUp(lines: Int) {
        // 默认不处理
    }

    override fun onScrollDown(lines: Int) {
        // 默认不处理
    }

    override fun onAttributesChanged(attributes: TextAttributes) {
        // 默认不处理
    }

    override fun onReverseLineFeed() {
        // 默认不处理
    }

    override fun onSaveCursor() {
        // 默认不处理
    }

    override fun onRestoreCursor() {
        // 默认不处理
    }
}
