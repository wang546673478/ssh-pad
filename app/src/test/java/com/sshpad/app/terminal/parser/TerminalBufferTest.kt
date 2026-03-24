package com.sshpad.app.terminal.parser

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * 终端缓冲区单元测试
 */
class TerminalBufferTest {
    
    private lateinit var buffer: TerminalBuffer
    
    @Before
    fun setUp() {
        buffer = TerminalBuffer(width = 80, height = 24, scrollbackSize = 100)
    }
    
    // ========== 基础文本渲染测试 ==========
    
    @Test
    fun testSimpleTextRender() {
        buffer.processText("Hello")
        
        val line = buffer.getLine(0)
        assertTrue(line.text.startsWith("Hello"))
    }
    
    @Test
    fun testCursorAdvancement() {
        buffer.processText("ABC")
        
        assertEquals(0, buffer.cursorRow)
        assertEquals(3, buffer.cursorCol)
    }
    
    @Test
    fun testNewlineHandling() {
        buffer.processText("Line1\nLine2")
        
        assertEquals(1, buffer.cursorRow)
        assertEquals(0, buffer.cursorCol)
    }
    
    @Test
    fun testCarriageReturn() {
        buffer.processText("ABC\rXY")
        
        val line = buffer.getLine(0)
        assertTrue(line.text.startsWith("XYC"))
    }
    
    @Test
    fun testTabExpansion() {
        buffer.processText("A\tB")
        
        // 制表符应该跳到下一个 8 字符边界
        assertEquals(8, buffer.cursorCol)
    }
    
    @Test
    fun testBackspace() {
        buffer.processText("ABC\bD")
        
        val line = buffer.getLine(0)
        assertTrue(line.text.startsWith("ABD"))
    }
    
    // ========== 光标移动测试 ==========
    
    @Test
    fun testCursorMoveUp() {
        buffer.processText("Line1\nLine2")
        buffer.processText("\u001b[1A") // 上移一行
        
        assertEquals(0, buffer.cursorRow)
    }
    
    @Test
    fun testCursorMoveDown() {
        buffer.processText("\u001b[5B") // 下移 5 行
        
        assertEquals(5, buffer.cursorRow)
    }
    
    @Test
    fun testCursorPositionAbsolute() {
        buffer.processText("\u001b[10;20H") // 移动到第 10 行第 20 列
        
        assertEquals(9, buffer.cursorRow)   // 0-indexed
        assertEquals(19, buffer.cursorCol)  // 0-indexed
    }
    
    @Test
    fun testCursorBoundaryClamping() {
        // 尝试移出边界
        buffer.processText("\u001b[100;200H")
        
        // 应该被限制在有效范围内
        assertTrue(buffer.cursorRow < buffer.height)
        assertTrue(buffer.cursorCol < buffer.width)
    }
    
    // ========== 清除操作测试 ==========
    
    @Test
    fun testClearScreen() {
        buffer.processText("Some text")
        buffer.processText("\u001b[2J") // 清除全屏
        
        val lines = buffer.getDisplayLines()
        assertTrue(lines.all { it.text.trim().isEmpty() })
        assertEquals(0, buffer.cursorRow)
        assertEquals(0, buffer.cursorCol)
    }
    
    @Test
    fun testClearLine() {
        buffer.processText("XXXXXXXX")
        buffer.processText("\u001b[G")   // 回到行首
        buffer.processText("\u001b[2K")  // 清除整行
        
        val line = buffer.getLine(0)
        assertTrue(line.text.trim().isEmpty())
    }
    
    // ========== 滚动测试 ==========
    
    @Test
    fun testScrollUp() {
        // 填充屏幕
        for (i in 0 until 24) {
            buffer.processText("Line $i\n")
        }
        
        // 向上滚动
        buffer.processText("\u001b[2S")
        
        // 检查滚动缓冲
        val scrollback = buffer.getScrollback()
        assertEquals(2, scrollback.size)
    }
    
    @Test
    fun testScrollbackLimit() {
        // 生成大量输出以测试滚动缓冲限制
        for (i in 0 until 150) {
            buffer.processText("Line $i\n")
        }
        
        val scrollback = buffer.getScrollback()
        assertTrue(scrollback.size <= 100) // 滚动缓冲限制为 100
    }
    
    // ========== 属性继承测试 ==========
    
    @Test
    fun testAttributePersistence() {
        buffer.processText("\u001b[31m") // 设置红色
        buffer.processText("Red")
        
        // 属性应该保持
        val line = buffer.getLine(0)
        // TODO: 验证样式信息
    }
    
    // ========== 重置测试 ==========
    
    @Test
    fun testReset() {
        buffer.processText("Test")
        buffer.processText("\u001b[5;10H")
        
        buffer.reset()
        
        val lines = buffer.getDisplayLines()
        assertTrue(lines.all { it.text.trim().isEmpty() })
        assertEquals(0, buffer.cursorRow)
        assertEquals(0, buffer.cursorCol)
    }
    
    // ========== 组合场景测试 ==========
    
    @Test
    fun testAnsiColorSequence() {
        buffer.processText("\u001b[31mRed\u001b[0m Normal")
        
        val line = buffer.getLine(0)
        assertTrue(line.text.contains("Red"))
        assertTrue(line.text.contains("Normal"))
    }
    
    @Test
    fun testComplexAnsiSequence() {
        // 粗体红色文字
        buffer.processText("\u001b[1;31mBold Red\u001b[0m")
        
        val line = buffer.getLine(0)
        assertTrue(line.text.contains("Bold Red"))
    }
}
