package com.sshpad.app.ssh.parser

/**
 * ANSI Escape Sequence Parser
 * Week 7: Interactive Shell Enhancement
 */
class AnsiParser {
    
    fun parse(input: String): List<TextSegment> {
        val segments = mutableListOf<TextSegment>()
        var currentIndex = 0
        var currentStyle = TextStyle()
        var currentText = StringBuilder()
        var inEscape = false
        var escapeParams = StringBuilder()
        
        while (currentIndex < input.length) {
            val char = input[currentIndex]
            
            when {
                char == '\u001B' -> {
                    if (currentText.isNotEmpty()) {
                        segments.add(TextSegment(currentText.toString(), currentStyle))
                        currentText.clear()
                    }
                    inEscape = true
                    escapeParams.clear()
                }
                inEscape && char == '[' -> {
                    // CSI sequence start
                }
                inEscape && (char.isDigit() || char == ';' || char == '?' || char == '<' || char == '=' || char == '>') -> {
                    escapeParams.append(char)
                }
                inEscape && char in '\u0040'..'\u007E' -> {
                    // Command character
                    if (char == 'm') {
                        currentStyle = interpretSgr(escapeParams.toString())
                    }
                    inEscape = false
                }
                else -> {
                    currentText.append(char)
                }
            }
            
            currentIndex++
        }
        
        if (currentText.isNotEmpty()) {
            segments.add(TextSegment(currentText.toString(), currentStyle))
        }
        
        return segments
    }
    
    private fun interpretSgr(params: String): TextStyle {
        if (params.isEmpty() || params == "0") return TextStyle()
        
        val style = TextStyle()
        val codes = params.split(';').mapNotNull { it.toIntOrNull() }.toMutableList()
        
        var i = 0
        while (i < codes.size) {
            val code = codes[i]
            when (code) {
                0 -> return TextStyle()
                1 -> style.isBold = true
                3 -> style.isItalic = true
                4 -> style.isUnderline = true
                30 -> style.foregroundColor = AnsiColor.BLACK
                31 -> style.foregroundColor = AnsiColor.RED
                32 -> style.foregroundColor = AnsiColor.GREEN
                33 -> style.foregroundColor = AnsiColor.YELLOW
                34 -> style.foregroundColor = AnsiColor.BLUE
                35 -> style.foregroundColor = AnsiColor.MAGENTA
                36 -> style.foregroundColor = AnsiColor.CYAN
                37 -> style.foregroundColor = AnsiColor.WHITE
                39 -> style.foregroundColor = null
                40 -> style.backgroundColor = AnsiColor.BLACK
                41 -> style.backgroundColor = AnsiColor.RED
                42 -> style.backgroundColor = AnsiColor.GREEN
                43 -> style.backgroundColor = AnsiColor.YELLOW
                44 -> style.backgroundColor = AnsiColor.BLUE
                45 -> style.backgroundColor = AnsiColor.MAGENTA
                46 -> style.backgroundColor = AnsiColor.CYAN
                47 -> style.backgroundColor = AnsiColor.WHITE
                49 -> style.backgroundColor = null
                90 -> style.foregroundColor = AnsiColor.BRIGHT_BLACK
                91 -> style.foregroundColor = AnsiColor.BRIGHT_RED
                92 -> style.foregroundColor = AnsiColor.BRIGHT_GREEN
                93 -> style.foregroundColor = AnsiColor.BRIGHT_YELLOW
                94 -> style.foregroundColor = AnsiColor.BRIGHT_BLUE
                95 -> style.foregroundColor = AnsiColor.BRIGHT_MAGENTA
                96 -> style.foregroundColor = AnsiColor.BRIGHT_CYAN
                97 -> style.foregroundColor = AnsiColor.BRIGHT_WHITE
                38 -> {
                    // Extended foreground color
                    if (i + 1 < codes.size) {
                        val mode = codes[i + 1]
                        if (mode == 5 && i + 2 < codes.size) {
                            // 256-color mode: 38;5;n
                            val colorIndex = codes[i + 2]
                            if (colorIndex in 0..255) {
                                style.foregroundColor = AnsiColor.Indexed(colorIndex)
                            }
                            i += 2
                        } else if (mode == 2 && i + 4 < codes.size) {
                            // True Color mode: 38;2;r;g;b
                            val r = codes[i + 2]
                            val g = codes[i + 3]
                            val b = codes[i + 4]
                            if (r in 0..255 && g in 0..255 && b in 0..255) {
                                style.foregroundColor = AnsiColor.TrueColor(r, g, b)
                            }
                            i += 4
                        }
                    }
                }
                48 -> {
                    // Extended background color
                    if (i + 1 < codes.size) {
                        val mode = codes[i + 1]
                        if (mode == 5 && i + 2 < codes.size) {
                            // 256-color mode: 48;5;n
                            val colorIndex = codes[i + 2]
                            if (colorIndex in 0..255) {
                                style.backgroundColor = AnsiColor.Indexed(colorIndex)
                            }
                            i += 2
                        } else if (mode == 2 && i + 4 < codes.size) {
                            // True Color mode: 48;2;r;g;b
                            val r = codes[i + 2]
                            val g = codes[i + 3]
                            val b = codes[i + 4]
                            if (r in 0..255 && g in 0..255 && b in 0..255) {
                                style.backgroundColor = AnsiColor.TrueColor(r, g, b)
                            }
                            i += 4
                        }
                    }
                }
            }
            i++
        }
        
        return style
    }
}

data class TextSegment(val text: String, val style: TextStyle)

data class TextStyle(
    val foregroundColor: AnsiColor? = null,
    val backgroundColor: AnsiColor? = null,
    val isBold: Boolean = false,
    val isUnderline: Boolean = false,
    val isItalic: Boolean = false
)

sealed class AnsiColor {
    object BLACK : AnsiColor()
    object RED : AnsiColor()
    object GREEN : AnsiColor()
    object YELLOW : AnsiColor()
    object BLUE : AnsiColor()
    object MAGENTA : AnsiColor()
    object CYAN : AnsiColor()
    object WHITE : AnsiColor()
    object BRIGHT_BLACK : AnsiColor()
    object BRIGHT_RED : AnsiColor()
    object BRIGHT_GREEN : AnsiColor()
    object BRIGHT_YELLOW : AnsiColor()
    object BRIGHT_BLUE : AnsiColor()
    object BRIGHT_MAGENTA : AnsiColor()
    object BRIGHT_CYAN : AnsiColor()
    object BRIGHT_WHITE : AnsiColor()
    data class Indexed(val index: Int) : AnsiColor()
    data class TrueColor(val r: Int, val g: Int, val b: Int) : AnsiColor()
}
