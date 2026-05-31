package com.example.util

data class BrailleCell(
    val char: Char,
    val dots: Set<Int>, // Numbers 1 to 6 representing active dots
    val unicode: Char  // Unicode Braille glyph
)

object BrailleTranslator {

    // Grade 1 letter mappings
    private val letterToDots = mapOf(
        'a' to setOf(1),
        'b' to setOf(1, 2),
        'c' to setOf(1, 4),
        'd' to setOf(1, 4, 5),
        'e' to setOf(1, 5),
        'f' to setOf(1, 2, 4),
        'g' to setOf(1, 2, 4, 5),
        'h' to setOf(1, 2, 5),
        'i' to setOf(2, 4),
        'j' to setOf(2, 4, 5),
        'k' to setOf(1, 3),
        'l' to setOf(1, 2, 3),
        'm' to setOf(1, 3, 4),
        'n' to setOf(1, 3, 4, 5),
        'o' to setOf(1, 3, 5),
        'p' to setOf(1, 2, 3, 4),
        'q' to setOf(1, 2, 3, 4, 5),
        'r' to setOf(1, 2, 3, 5),
        's' to setOf(2, 3, 4),
        't' to setOf(2, 3, 4, 5),
        'u' to setOf(1, 3, 6),
        'v' to setOf(1, 2, 3, 6),
        'w' to setOf(2, 4, 5, 6),
        'x' to setOf(1, 3, 4, 6),
        'y' to setOf(1, 3, 4, 5, 6),
        'z' to setOf(1, 3, 5, 6),
        ' ' to emptySet()
    )

    private val numberToDots = mapOf(
        '1' to setOf(1),
        '2' to setOf(1, 2),
        '3' to setOf(1, 4),
        '4' to setOf(1, 4, 5),
        '5' to setOf(1, 5),
        '6' to setOf(1, 2, 4),
        '7' to setOf(1, 2, 4, 5),
        '8' to setOf(1, 2, 5),
        '9' to setOf(2, 4),
        '0' to setOf(2, 4, 5)
    )

    private val punctuationToDots = mapOf(
        '.' to setOf(2, 5, 6),
        ',' to setOf(2),
        ';' to setOf(2, 3),
        ':' to setOf(2, 5),
        '!' to setOf(2, 3, 5),
        '?' to setOf(2, 3, 6),
        '-' to setOf(3, 6),
        '\'' to setOf(3)
    )

    // Dots 3, 4, 5, 6 represents the number sign prefix
    val NUMBER_SIGN_DOTS = setOf(3, 4, 5, 6)
    val NUMBER_SIGN_UNICODE = '⠼'

    // Capital marker dot 6
    val CAPITAL_SIGN_DOTS = setOf(6)
    val CAPITAL_SIGN_UNICODE = '⠠'

    /**
     * Converts a set of active dot numbers (1-6) to its corresponding Unicode Braille character.
     * The Braille patterns start at Unicode 0x2800.
     * Standard offset:
     * Dot 1 -> 1, Dot 2 -> 2, Dot 3 -> 4, Dot 4 -> 8, Dot 5 -> 16, Dot 6 -> 32, Dot 7 -> 64, Dot 8 -> 128
     */
    fun dotsToUnicode(dots: Set<Int>): Char {
        var offset = 0
        if (dots.contains(1)) offset += 1
        if (dots.contains(2)) offset += 2
        if (dots.contains(3)) offset += 4
        if (dots.contains(4)) offset += 8
        if (dots.contains(5)) offset += 16
        if (dots.contains(6)) offset += 32
        return (0x2800 + offset).toChar()
    }

    /**
     * Converts a Unicode Braille character back into its active dot numbers (1-6).
     */
    fun unicodeToDots(unicode: Char): Set<Int> {
        val codeValue = unicode.code
        if (codeValue < 0x2800 || codeValue > 0x28FF) return emptySet()
        val offset = codeValue - 0x2800
        val dots = mutableSetOf<Int>()
        if ((offset and 1) != 0) dots.add(1)
        if ((offset and 2) != 0) dots.add(2)
        if ((offset and 4) != 0) dots.add(3)
        if ((offset and 8) != 0) dots.add(4)
        if ((offset and 16) != 0) dots.add(5)
        if ((offset and 32) != 0) dots.add(6)
        return dots
    }

    /**
     * Translates a custom Set of dot combinations into an English character.
     */
    fun dotsToChar(dots: Set<Int>): Char {
        // Try letters first
        letterToDots.forEach { (char, set) ->
            if (set == dots && char != ' ') return char
        }
        // Try punctuation
        punctuationToDots.forEach { (char, set) ->
            if (set == dots) return char
        }
        return '?' // Unknown dot combination
    }

    /**
     * Full Text to Braille conversion workflow. Handles capital cases and numbers.
     */
    fun textToBraille(text: String): List<BrailleCell> {
        val result = mutableListOf<BrailleCell>()
        var inNumberMode = false

        for (char in text) {
            val lowerChar = char.lowercaseChar()
            when {
                lowerChar == ' ' -> {
                    inNumberMode = false
                    result.add(BrailleCell(' ', emptySet(), '⠀'))
                }
                char.isDigit() -> {
                    if (!inNumberMode) {
                        // Insert number indicator
                        result.add(BrailleCell('#', NUMBER_SIGN_DOTS, NUMBER_SIGN_UNICODE))
                        inNumberMode = true
                    }
                    val dots = numberToDots[char] ?: emptySet()
                    result.add(BrailleCell(char, dots, dotsToUnicode(dots)))
                }
                char.isUpperCase() -> {
                    inNumberMode = false
                    // Insert capitalization indicator
                    result.add(BrailleCell('^', CAPITAL_SIGN_DOTS, CAPITAL_SIGN_UNICODE))
                    val dots = letterToDots[lowerChar] ?: emptySet()
                    result.add(BrailleCell(char, dots, dotsToUnicode(dots)))
                }
                else -> {
                    inNumberMode = false
                    val dots = letterToDots[lowerChar] ?: punctuationToDots[lowerChar] ?: emptySet()
                    result.add(BrailleCell(char, dots, dotsToUnicode(dots)))
                }
            }
        }
        return result
    }

    /**
     * Braille Unicode sequence to Text translation workflow.
     */
    fun brailleToText(brailleSequence: String): String {
        val sb = StringBuilder()
        var capNext = false
        var numNext = false

        for (glyph in brailleSequence) {
            if (glyph == '⠀') {
                sb.append(' ')
                numNext = false
                continue
            }
            if (glyph == CAPITAL_SIGN_UNICODE) {
                capNext = true
                continue
            }
            if (glyph == NUMBER_SIGN_UNICODE) {
                numNext = true
                continue
            }

            val dots = unicodeToDots(glyph)
            var char = dotsToChar(dots)

            if (numNext) {
                // Find digit mapping to dots
                val digit = numberToDots.entries.find { it.value == dots }?.key ?: '?'
                sb.append(digit)
                continue
            }

            if (capNext) {
                char = char.uppercaseChar()
                capNext = false
            }
            sb.append(char)
        }

        return sb.toString()
    }
}
