package de.telekom.usp

sealed class PathElement(val text: String) {

    init {
        require(text.isNotBlank()) { "Empty path names not allowed" }
    }

    /**
     * Determines whether this element is always the last element in a path. This is true for
     * parameter, command and event elements.
     */
    abstract val isTerminal: Boolean

    override fun hashCode(): Int {
        return text.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PathElement

        return text == other.text
    }

    override fun toString(): String {
        return text
    }

    companion object {

        private val WILDCARD = Object("*.")

        private val nameRegex = Regex("""[A-Za-z_][A-Za-z_0-9]*""")

        internal fun from(text: String): PathElement {
            return if (text.startsWith("[") && (text.endsWith("]."))) {
                Search(text)
            } else if (text.endsWith(".")) {
                when (text) {
                    "*." -> WILDCARD
                    "Device." -> Device.first()
                    else -> Object(text)
                }
            } else if (text.endsWith("()")) {
                Command(text)
            } else if (text.endsWith("!")) {
                Event(text)
            } else {
                Parameter(text)
            }
        }

        internal fun isValidName(name: String) = nameRegex.matches(name)
    }

    class Object internal constructor(text: String) : PathElement(text) {

        /**
         * If this is a wildcard object ('*.') instance is zero, if it is an instance object, it
         * matches the instance number, i.e. 2 for '2.', otherwise it is `null`.
         */
        val instance: Int?

        /**
         * The complete reffollow string if there is any, otherwise `null`. Valid examples are:
         * '#*+' or '#2+' or just '+'
         */
        val refFollow: String?

        init {
            val name = text.substring(0, text.length - 1)
            instance = if (name == "*") 0 else name.toIntOrNull()

            if (name != "*" && instance == null) {
                val ref = refRegex.find(name)
                if (ref != null) {
                    refFollow = ref.value
                    require(isValidName(name.substring(0, name.length - refFollow.length))) {
                        "'$text' is not a valid object name"
                    }
                } else {
                    refFollow = null
                    require(isValidName(name)) { "'$text' is not a valid object name" }
                }
            } else {
                refFollow = null
            }
        }

        override val isTerminal = false

        companion object {

            // Matches for example '#*+' or '#2+' or just '+'
            private val refRegex = Regex("""(#(\*|\d+))?\+$""")
        }
    }

    class Search internal constructor(text: String) : PathElement(text) {


        override val isTerminal = false

        /**
         * The search expression, i.e. the text without leading "[" and trailing "]".
         */
        val expression = text.substring(1..<text.lastIndexOf(']'))

        /**
         * The search expression components, i.e. the parts which are separated by "&&"
         */
        val components: List<String>
            get() = expression.split("&&")
    }

    class Parameter internal constructor(text: String) : PathElement(text) {

        init {
            require(isValidName(text)) { "'$text' is not a valid parameter name" }
        }

        override val isTerminal = true
    }

    class Command internal constructor(text: String) : PathElement(text) {

        init {
            require(text.length > 2 || isValidName(text.substring(0, text.length - 2))) {
                "'$text' is not a valid command name"
            }
        }

        override val isTerminal = true
    }

    class Event internal constructor(text: String) : PathElement(text) {

        init {
            require(text.length > 1 || isValidName(text.substring(0, text.length - 1))) {
                "'$text' is not a valid event name"
            }
        }

        override val isTerminal = true
    }
}