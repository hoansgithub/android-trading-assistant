package co.alcheclub.ai.trading.assistant.core.util

/**
 * Multi-layer LLM JSON response repair pipeline.
 * Handles code fences, invalid escapes, trailing commas, and truncated responses.
 */
object JsonRepair {

    /**
     * Extract JSON object from LLM output (strips code fences, extra text).
     */
    fun extractJsonObject(text: String): String {
        var cleaned = text.trim()

        // Strip code fences: ```json ... ``` or ``` ... ```
        if (cleaned.startsWith("```")) {
            val firstNewline = cleaned.indexOf('\n')
            if (firstNewline >= 0) {
                cleaned = cleaned.substring(firstNewline + 1)
            }
            val closingIndex = cleaned.lastIndexOf("```")
            if (closingIndex >= 0) {
                cleaned = cleaned.substring(0, closingIndex)
            }
            cleaned = cleaned.trim()
        }

        // Extract JSON object between first { and last }
        val openBrace = cleaned.indexOf('{')
        val closeBrace = cleaned.lastIndexOf('}')
        if (openBrace >= 0 && closeBrace > openBrace) {
            return cleaned.substring(openBrace, closeBrace + 1)
        }

        return cleaned
    }

    /**
     * Sanitizes common JSON issues from LLMs:
     * - Invalid escape sequences (\*, \#, \~, \_)
     * - Trailing commas before } or ]
     * - Unescaped control characters
     */
    fun sanitizeJson(text: String): String {
        var result = text

        // Fix invalid backslash escapes (keep valid JSON escapes)
        result = Regex("""\\(?!["\\/bfnrtu]|u[0-9a-fA-F]{4})(.)""").replace(result, "$1")

        // Remove trailing commas before } or ]
        result = Regex(""",\s*([}\]])""").replace(result, "$1")

        // Replace unescaped control characters
        result = result.replace("\t", "  ")
        result = result.replace("\u000C", " ")

        return result
    }

    /**
     * Attempts to repair truncated JSON (from MAX_TOKENS responses).
     * Closes open strings, arrays, and objects.
     */
    fun repairTruncatedJson(text: String): String {
        var result = text.trim()

        if (result.endsWith("}")) return result

        // Close open string
        val quoteCount = result.count { it == '"' }
        val escapedQuoteCount = Regex("""\\"""").findAll(result).count()
        val realQuotes = quoteCount - escapedQuoteCount

        if (realQuotes % 2 != 0) {
            if (result.endsWith("\\")) {
                result = result.dropLast(1)
            }
            result += "\""
        }

        // Remove trailing comma or colon
        while (result.endsWith(",") || result.endsWith(":")) {
            result = result.dropLast(1).trim()
        }

        // Close open arrays and objects
        var openBraces = 0
        var openBrackets = 0
        var inString = false
        var prevChar = ' '

        for (char in result) {
            if (char == '"' && prevChar != '\\') {
                inString = !inString
            }
            if (!inString) {
                when (char) {
                    '{' -> openBraces++
                    '}' -> openBraces--
                    '[' -> openBrackets++
                    ']' -> openBrackets--
                }
            }
            prevChar = char
        }

        repeat(maxOf(0, openBrackets)) { result += "]" }
        repeat(maxOf(0, openBraces)) { result += "}" }

        return result
    }

    /**
     * Strips problematic text fields (aiExplanation, marketContext) from JSON
     * to allow parsing critical trading data even when text contains malformed content.
     */
    fun stripTextFields(text: String): String {
        var cleaned = text
        for (field in listOf("aiExplanation", "marketContext")) {
            cleaned = Regex(""""$field"\s*:\s*"(?:[^"\\]|\\.)*"(?:\s*,)?""")
                .replace(cleaned, "")
        }
        return sanitizeJson(cleaned)
    }

    /**
     * Full repair pipeline: extract → sanitize → try truncation repair.
     */
    fun fullRepair(rawText: String): String {
        var text = extractJsonObject(rawText)
        text = sanitizeJson(text)
        text = repairTruncatedJson(text)
        return text
    }
}
