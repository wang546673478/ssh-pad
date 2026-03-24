package com.sshpad.app.terminal.parser

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * ANSI 解析器单元测试
 * 
 * 测试覆盖：
 * - 基础 8 色支持
 * - 16 色扩展
 * - 256 色模式
 * - 真彩色（24-bit）
 * - 光标移动（CUU, CUD, CUF, CUB, CUP）
 * - 清除屏幕（ED, EL）
 * - 滚动（SU, SD）
 * - 文本属性（粗体、斜体、下划线、反色、隐藏、删除线）
 */
class AnsiParserTest {
    
    private lateinit var parser: AnsiParser
    private lateinit var mockListener: MockAnsiParserListener
    
    @Before
    fun setUp() {
        parser = AnsiParser()
        mockListener = MockAnsiParserListener()
        parser.listener = mockListener
    }
    
    // ========== 基础文本处理测试 ==========
    
    @Test
    fun testPlainTextView() {
        parser.processText("Hello, World!")
        
        assertEquals(1, mockListener.textEvents.size)
        assertEquals("Hello, World!", mockListener.textEvents[0].first)
    }
    
    @Test
    fun testTextWithNewline() {
        parser.processText("Line 1\nLine 2")
        
        assertEquals(2, mockListener.textEvents.size)
        assertEquals("Line 1", mockListener.textEvents[0].first)
        assertEquals("Line 2", mockListener.textEvents[1].first)
    }
    
    // ========== 基础 8 色测试 ==========
    
    @Test
    fun testBasic8ForegroundColors() {
        val colorCodes = listOf(30 to AnsiColor.BLACK, 31 to AnsiColor.RED, 32 to AnsiColor.GREEN,
                               33 to AnsiColor.YELLOW, 34 to AnsiColor.BLUE, 35 to AnsiColor.MAGENTA,
                               36 to AnsiColor.CYAN, 37 to AnsiColor.WHITE)
        
        for ((code, expectedColor) in colorCodes) {
            mockListener.reset()
            parser.processText("\u001b[${code}m")
            
            assertEquals("Color code $code", expectedColor::class, parser.currentAttributes.foregroundColor!!::class)
        }
    }
    
    @Test
    fun testBasic8BackgroundColor() {
        val colorCodes = listOf(40 to AnsiColor.BLACK, 41 to AnsiColor.RED, 42 to AnsiColor.GREEN,
                               43 to AnsiColor.YELLOW, 44 to AnsiColor.BLUE, 45 to AnsiColor.MAGENTA,
                               46 to AnsiColor.CYAN, 47 to AnsiColor.WHITE)
        
        for ((code, expectedColor) in colorCodes) {
            mockListener.reset()
            parser.processText("\u001b[${code}m")
            
            assertEquals("Color code $code", expectedColor::class, parser.currentAttributes.backgroundColor!!::class)
        }
    }
    
    // ========== 16 色扩展测试 ==========
    
    @Test
    fun testBrightForegroundColors() {
        val colorCodes = listOf(90 to AnsiColor.BRIGHT_BLACK, 91 to AnsiColor.BRIGHT_RED,
                               92 to AnsiColor.BRIGHT_GREEN, 93 to AnsiColor.BRIGHT_YELLOW,
                               94 to AnsiColor.BRIGHT_BLUE, 95 to AnsiColor.BRIGHT_MAGENTA,
                               96 to AnsiColor.BRIGHT_CYAN, 97 to AnsiColor.BRIGHT_WHITE)
        
        for ((code, expectedColor) in colorCodes) {
            mockListener.reset()
            parser.processText("\u001b[${code}m")
            
            assertEquals("Bright color code $code", expectedColor::class, parser.currentAttributes.foregroundColor!!::class)
        }
    }
    
    @Test
    fun testBrightBackgroundColor() {
        val colorCodes = listOf(100 to AnsiColor.BRIGHT_BLACK, 101 to AnsiColor.BRIGHT_RED,
                               102 to AnsiColor.BRIGHT_GREEN, 103 to AnsiColor.BRIGHT_YELLOW,
                               104 to AnsiColor.BRIGHT_BLUE, 105 to AnsiColor.BRIGHT_MAGENTA,
                               106 to AnsiColor.BRIGHT_CYAN, 107 to AnsiColor.BRIGHT_WHITE)
        
        for ((code, expectedColor) in colorCodes) {
            mockListener.reset()
            parser.processText("\u001b[${code}m")
            
            assertEquals("Bright background color code $code", expectedColor::class, parser.currentAttributes.backgroundColor!!::class)
        }
    }
    
    // ========== 256 色模式测试 ==========
    
    @Test
    fun test256ColorForeground() {
        // 测试 256 色前景：\u001b[38;5;Nm
        parser.processText("\u001b[38;5;196m") // 亮红色
        
        assertTrue(parser.currentAttributes.foregroundColor is AnsiColor.Color256)
        assertEquals(196, (parser.currentAttributes.foregroundColor as AnsiColor.Color256).index)
    }
    
    @Test
    fun test256ColorBackground() {
        // 测试 256 色背景：\u001b[48;5;Nm
        parser.processText("\u001b[48;5;21m") // 蓝色
        
        assertTrue(parser.currentAttributes.backgroundColor is AnsiColor.Color256)
        assertEquals(21, (parser.currentAttributes.backgroundColor as AnsiColor.Color256).index)
    }
    
    @Test
    fun test256ColorRange() {
        // 测试 256 色的全部范围 (0-255)
        val testIndices = listOf(0, 16, 100, 200, 231, 232, 255)
        
        for (index in testIndices) {
            mockListener.reset()
            parser.processText("\u001b[38;5;${index}m")
            
            assertTrue("Index $index", parser.currentAttributes.foregroundColor is AnsiColor.Color256)
            assertEquals("Index $index", index, (parser.currentAttributes.foregroundColor as AnsiColor.Color256).index)
        }
    }
    
    // ========== 真彩色（24-bit）测试 ==========
    
    @Test
    fun testTrueColorForeground() {
        // 测试真彩色前景：\u001b[38;2;R;G;Bm
        parser.processText("\u001b[38;2;255;128;64m")
        
        assertTrue(parser.currentAttributes.foregroundColor is AnsiColor.TrueColor)
        val trueColor = parser.currentAttributes.foregroundColor as AnsiColor.TrueColor
        assertEquals(255, trueColor.r)
        assertEquals(128, trueColor.g)
        assertEquals(64, trueColor.b)
    }
    
    @Test
    fun testTrueColorBackground() {
        // 测试真彩色背景：\u001b[48;2;R;G;Bm
        parser.processText("\u001b[48;2;100;150;200m")
        
        assertTrue(parser.currentAttributes.backgroundColor is AnsiColor.TrueColor)
        val trueColor = parser.currentAttributes.backgroundColor as AnsiColor.TrueColor
        assertEquals(100, trueColor.r)
        assertEquals(150, trueColor.g)
        assertEquals(200, trueColor.b)
    }
    
    // ========== 文本属性测试 ==========
    
    @Test
    fun testBoldAttribute() {
        parser.processText("\u001b[1m")
        assertTrue(parser.currentAttributes.isBold)
        
        parser.processText("\u001b[22m")
        assertFalse(parser.currentAttributes.isBold)
    }
    
    @Test
    fun testItalicAttribute() {
        parser.processText("\u001b[3m")
        assertTrue(parser.currentAttributes.isItalic)
        
        parser.processText("\u001b[23m")
        assertFalse(parser.currentAttributes.isItalic)
    }
    
    @Test
    fun testUnderlineAttribute() {
        parser.processText("\u001b[4m")
        assertTrue(parser.currentAttributes.isUnderline)
        
        parser.processText("\u001b[24m")
        assertFalse(parser.currentAttributes.isUnderline)
    }
    
    @Test
    fun testReverseAttribute() {
        parser.processText("\u001b[7m")
        assertTrue(parser.currentAttributes.isReverse)
        
        parser.processText("\u001b[27m")
        assertFalse(parser.currentAttributes.isReverse)
    }
    
    @Test
    fun testHiddenAttribute() {
        parser.processText("\u001b[8m")
        assertTrue(parser.currentAttributes.isHidden)
        
        parser.processText("\u001b[28m")
        assertFalse(parser.currentAttributes.isHidden)
    }
    
    @Test
    fun testStrikethroughAttribute() {
        parser.processText("\u001b[9m")
        assertTrue(parser.currentAttributes.isStrikethrough)
        
        parser.processText("\u001b[29m")
        assertFalse(parser.currentAttributes.isStrikethrough)
    }
    
    @Test
    fun testResetAllAttributes() {
        // 设置多个属性
        parser.processText("\u001b[1;3;4;7;9m")
        assertTrue(parser.currentAttributes.isBold)
        assertTrue(parser.currentAttributes.isItalic)
        assertTrue(parser.currentAttributes.isUnderline)
        assertTrue(parser.currentAttributes.isReverse)
        assertTrue(parser.currentAttributes.isStrikethrough)
        
        // 重置所有属性
        parser.processText("\u001b[0m")
        assertFalse(parser.currentAttributes.isBold)
        assertFalse(parser.currentAttributes.isItalic)
        assertFalse(parser.currentAttributes.isUnderline)
        assertFalse(parser.currentAttributes.isReverse)
        assertFalse(parser.currentAttributes.isStrikethrough)
        assertNull(parser.currentAttributes.foregroundColor)
        assertNull(parser.currentAttributes.backgroundColor)
    }
    
    // ========== 光标移动测试 ==========
    
    @Test
    fun testCursorUp() {
        // CUU: \u001b[nA
        parser.processText("\u001b[5A")
        
        assertEquals(0, mockListener.cursorMoveDeltaX)
        assertEquals(-5, mockListener.cursorMoveDeltaY)
    }
    
    @Test
    fun testCursorDown() {
        // CUD: \u001b[nB
        parser.processText("\u001b[3B")
        
        assertEquals(0, mockListener.cursorMoveDeltaX)
        assertEquals(3, mockListener.cursorMoveDeltaY)
    }
    
    @Test
    fun testCursorForward() {
        // CUF: \u001b[nC
        parser.processText("\u001b[10C")
        
        assertEquals(10, mockListener.cursorMoveDeltaX)
        assertEquals(0, mockListener.cursorMoveDeltaY)
    }
    
    @Test
    fun testCursorBackward() {
        // CUB: \u001b[nD
        parser.processText("\u001b[7D")
        
        assertEquals(-7, mockListener.cursorMoveDeltaX)
        assertEquals(0, mockListener.cursorMoveDeltaY)
    }
    
    @Test
    fun testCursorPosition() {
        // CUP: \u001b[row;colH
        parser.processText("\u001b[15;40H")
        
        assertEquals(14, mockListener.cursorRow) // 0-indexed
        assertEquals(39, mockListener.cursorCol)  // 0-indexed
    }
    
    @Test
    fun testCursorPositionDefaultParams() {
        // CUP 无参数默认为 (1,1)
        parser.processText("\u001b[H")
        
        assertEquals(0, mockListener.cursorRow)
        assertEquals(0, mockListener.cursorCol)
    }
    
    @Test
    fun testCursorHorizontalAbsolute() {
        // CHA: \u001b[nG
        parser.processText("\u001b[25G")
        
        assertEquals(24, mockListener.cursorColAbsolute) // 0-indexed
    }
    
    // ========== 清除命令测试 ==========
    
    @Test
    fun testEraseDisplayToEnd() {
        // ED mode 0: \u001b[0J
        parser.processText("\u001b[0J")
        
        assertEquals(EraseMode.CURSOR_TO_END, mockListener.eraseDisplayMode)
    }
    
    @Test
    fun testEraseDisplayToStart() {
        // ED mode 1: \u001b[1J
        parser.processText("\u001b[1J")
        
        assertEquals(EraseMode.CURSOR_TO_START, mockListener.eraseDisplayMode)
    }
    
    @Test
    fun testEraseDisplayFull() {
        // ED mode 2: \u001b[2J
        parser.processText("\u001b[2J")
        
        assertEquals(EraseMode.FULL, mockListener.eraseDisplayMode)
    }
    
    @Test
    fun testEraseDisplayWithScrollback() {
        // ED mode 3: \u001b[3J
        parser.processText("\u001b[3J")
        
        assertEquals(EraseMode.FULL_WITH_SCROLLBACK, mockListener.eraseDisplayMode)
    }
    
    @Test
    fun testEraseLineToEnd() {
        // EL mode 0: \u001b[0K
        parser.processText("\u001b[0K")
        
        assertEquals(EraseMode.CURSOR_TO_END, mockListener.eraseLineMode)
    }
    
    @Test
    fun testEraseLineToStart() {
        // EL mode 1: \u001b[1K
        parser.processText("\u001b[1K")
        
        assertEquals(EraseMode.CURSOR_TO_START, mockListener.eraseLineMode)
    }
    
    @Test
    fun testEraseLineFull() {
        // EL mode 2: \u001b[2K
        parser.processText("\u001b[2K")
        
        assertEquals(EraseMode.FULL, mockListener.eraseLineMode)
    }
    
    // ========== 滚动命令测试 ==========
    
    @Test
    fun testScrollUp() {
        // SU: \u001b[nS
        parser.processText("\u001b[3S")
        
        assertEquals(3, mockListener.scrollUpLines)
    }
    
    @Test
    fun testScrollDown() {
        // SD: \u001b[nT
        parser.processText("\u001b[5T")
        
        assertEquals(5, mockListener.scrollDownLines)
    }
    
    // ========== 组合序列测试 ==========
    
    @Test
    fun testMultipleSgrParameters() {
        // 多个 SGR 参数组合：粗体 + 红色前景 + 蓝色背景
        parser.processText("\u001b[1;31;44m")
        
        assertTrue(parser.currentAttributes.isBold)
        assertTrue(parser.currentAttributes.foregroundColor is AnsiColor.RED)
        assertTrue(parser.currentAttributes.backgroundColor is AnsiColor.BLUE)
    }
    
    @Test
    fun testColorWithAttribute() {
        // 256 色 + 粗体
        parser.processText("\u001b[1;38;5;82m")
        
        assertTrue(parser.currentAttributes.isBold)
        assertTrue(parser.currentAttributes.foregroundColor is AnsiColor.Color256)
        assertEquals(82, (parser.currentAttributes.foregroundColor as AnsiColor.Color256).index)
    }
    
    // ========== 边界情况测试 ==========
    
    @Test
    fun testEmptyCsiSequence() {
        // 空的 CSI 序列应该使用默认值
        parser.processText("\u001b[m")
        
        // 应该重置所有属性
        assertFalse(parser.currentAttributes.isBold)
        assertNull(parser.currentAttributes.foregroundColor)
    }
    
    @Test
    fun testInvalidSequence() {
        // 无效序列应该被忽略
        parser.processText("\u001b[999Z")
        
        // 不应该影响状态
    }
    
    @Test
    fun testResetParser() {
        parser.processText("\u001b[1;31m")
        assertTrue(parser.currentAttributes.isBold)
        
        parser.reset()
        
        assertFalse(parser.currentAttributes.isBold)
        assertNull(parser.currentAttributes.foregroundColor)
    }
}

/**
 * 模拟监听器用于测试
 */
class MockAnsiParserListener : AnsiParserListener {
    var textEvents: List<Pair<String, TextAttributes>> = emptyList()
        private set
    
    var cursorMoveDeltaX: Int = 0
        private set
    var cursorMoveDeltaY: Int = 0
        private set
    
    var cursorRow: Int = 0
        private set
    var cursorCol: Int = 0
        private set
    var cursorColAbsolute: Int = 0
        private set
    
    var eraseDisplayMode: EraseMode? = null
        private set
    var eraseLineMode: EraseMode? = null
        private set
    
    var scrollUpLines: Int = 0
        private set
    var scrollDownLines: Int = 0
        private set
    
    var attributesChangedCount: Int = 0
        private set
    
    override fun onText(text: String, attributes: TextAttributes) {
        textEvents = textEvents + (text to attributes.copy())
    }
    
    override fun onCursorMove(deltaX: Int, deltaY: Int) {
        cursorMoveDeltaX = deltaX
        cursorMoveDeltaY = deltaY
    }
    
    override fun onCursorPosition(row: Int, col: Int) {
        cursorRow = row
        cursorCol = col
    }
    
    override fun onCursorHorizontalAbsolute(col: Int) {
        cursorColAbsolute = col
    }
    
    override fun onCursorNextLine(lines: Int) {
        // Not used in tests
    }
    
    override fun onCursorPreviousLine(lines: Int) {
        // Not used in tests
    }
    
    override fun onEraseDisplay(mode: EraseMode) {
        eraseDisplayMode = mode
    }
    
    override fun onEraseLine(mode: EraseMode) {
        eraseLineMode = mode
    }
    
    override fun onScrollUp(lines: Int) {
        scrollUpLines = lines
    }
    
    override fun onScrollDown(lines: Int) {
        scrollDownLines = lines
    }
    
    override fun onAttributesChanged(attributes: TextAttributes) {
        attributesChangedCount++
    }
    
    override fun onReverseLineFeed() {
        // Not used in tests
    }
    
    override fun onSaveCursor() {
        // Not used in tests
    }
    
    override fun onRestoreCursor() {
        // Not used in tests
    }
    
    fun reset() {
        textEvents = emptyList()
        cursorMoveDeltaX = 0
        cursorMoveDeltaY = 0
        cursorRow = 0
        cursorCol = 0
        cursorColAbsolute = 0
        eraseDisplayMode = null
        eraseLineMode = null
        scrollUpLines = 0
        scrollDownLines = 0
        attributesChangedCount = 0
    }
}
