package net.integr.serialization.prettyprint

class PrettyPrintContext(private val enabled: Boolean, private val indent: Int = 0) {
    fun increaseIndent(): PrettyPrintContext {
        return PrettyPrintContext(enabled, indent + 1)
    }

    fun decreaseIndent(): PrettyPrintContext {
        return PrettyPrintContext(enabled, indent - 1)
    }

    fun isEnabled(): Boolean {
        return enabled
    }

    fun withIndent(str: String): String {
        if (!enabled) return str
        return "    ".repeat(indent) + str
    }

    fun withNewLine(str: String): String {
        if (!enabled) return str
        return str + "\n"
    }

    fun withNewLineAndIndent(str: String): String {
        if (!enabled) return str
        return "    ".repeat(indent) + str + "\n"
    }
}