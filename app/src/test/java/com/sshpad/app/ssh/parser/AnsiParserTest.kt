package com.sshpad.app.ssh.parser

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for AnsiParser with 256-color and True Color support
 */
class AnsiParserTest {
    
    private val parser = AnsiParser()
    
    @Test
    fun `test basic ANSI colors`() {
        val input = "\u001B[31mRed Text\u001B[0m"
        val segments = parser.parse(input)
        
        assertEquals(1, segments.size)
        assertEquals("Red Text", segments[0].text)
        assertEquals(AnsiColor.RED, segments[0].style.foregroundColor)
    }
    
    @Test
    fun `test 256-color foreground`() {
        // 38;5;196 is bright red in 256-color palette
        val input = "\u001B[38;5;196m256 Color Text\u001B[0m"
        val segments = parser.parse(input)
        
        assertEquals(1, segments.size)
        assertEquals("256 Color Text", segments[0].text)
        assertTrue(segments[0].style.foregroundColor is AnsiColor.Indexed)
        assertEquals(196, (segments[0].style.foregroundColor as AnsiColor.Indexed).index)
    }
    
    @Test
    fun `test 256-color background`() {
        // 48;5;21 is blue in 256-color palette
        val input = "\u001B[48;5;21mBlue Background\u001B[0m"
        val segments = parser.parse(input)
        
        assertEquals(1, segments.size)
        assertEquals("Blue Background", segments[0].text)
        assertTrue(segments[0].style.backgroundColor is AnsiColor.Indexed)
        assertEquals(21, (segments[0].style.backgroundColor as AnsiColor.Indexed).index)
    }
    
    @Test
    fun `test True Color foreground`() {
        // 38;2;255;128;64 is a specific RGB color
        val input = "\u001B[38;2;255;128;64mTrue Color Text\u001B[0m"
        val segments = parser.parse(input)
        
        assertEquals(1, segments.size)
        assertEquals("True Color Text", segments[0].text)
        assertTrue(segments[0].style.foregroundColor is AnsiColor.TrueColor)
        val trueColor = segments[0].style.foregroundColor as AnsiColor.TrueColor
        assertEquals(255, trueColor.r)
        assertEquals(128, trueColor.g)
        assertEquals(64, trueColor.b)
    }
    
    @Test
    fun `test True Color background`() {
        // 48;2;0;128;255 is a specific RGB background
        val input = "\u001B[48;2;0;128;255mRGB Background\u001B[0m"
        val segments = parser.parse(input)
        
        assertEquals(1, segments.size)
        assertEquals("RGB Background", segments[0].text)
        assertTrue(segments[0].style.backgroundColor is AnsiColor.TrueColor)
        val trueColor = segments[0].style.backgroundColor as AnsiColor.TrueColor
        assertEquals(0, trueColor.r)
        assertEquals(128, trueColor.g)
        assertEquals(255, trueColor.b)
    }
    
    @Test
    fun `test multiple color codes in sequence`() {
        val input = "\u001B[38;5;100mFirst\u001B[38;5;200mSecond\u001B[0m"
        val segments = parser.parse(input)
        
        assertEquals(2, segments.size)
        assertEquals("First", segments[0].text)
        assertEquals(100, (segments[0].style.foregroundColor as AnsiColor.Indexed).index)
        assertEquals("Second", segments[1].text)
        assertEquals(200, (segments[1].style.foregroundColor as AnsiColor.Indexed).index)
    }
    
    @Test
    fun `test combined attributes with 256 color`() {
        val input = "\u001B[1;38;5;82mBold Green\u001B[0m"
        val segments = parser.parse(input)
        
        assertEquals(1, segments.size)
        assertEquals("Bold Green", segments[0].text)
        assertTrue(segments[0].style.isBold)
        assertEquals(82, (segments[0].style.foregroundColor as AnsiColor.Indexed).index)
    }
    
    @Test
    fun `test reset clears all attributes`() {
        val input = "\u001B[1;38;5;100mStyled\u001B[0mNormal"
        val segments = parser.parse(input)
        
        assertEquals(2, segments.size)
        assertTrue(segments[0].style.isBold)
        assertFalse(segments[1].style.isBold)
        assertNull(segments[1].style.foregroundColor)
    }
    
    @Test
    fun `test invalid color indices are ignored`() {
        // Test with out-of-range color index
        val input = "\u001B[38;5;999mInvalid\u001B[0m"
        val segments = parser.parse(input)
        
        assertEquals(1, segments.size)
        assertEquals("Invalid", segments[0].text)
        // Should not have Indexed color for invalid index
        assertTrue(segments[0].style.foregroundColor !is AnsiColor.Indexed)
    }
    
    @Test
    fun `test True Color with invalid RGB values are ignored`() {
        // Test with out-of-range RGB values
        val input = "\u001B[38;2;300;400;500mInvalid RGB\u001B[0m"
        val segments = parser.parse(input)
        
        assertEquals(1, segments.size)
        assertEquals("Invalid RGB", segments[0].text)
        assertTrue(segments[0].style.foregroundColor !is AnsiColor.TrueColor)
    }
}
