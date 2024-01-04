package de.telekom.usp

sealed class PathElement(val text: String) {

    init {
        require(text.isNotBlank()) { "Empty path names not allowed" }
    }

    /**
     * Determines whether this element must always be the last element in a path. This is true for
     * parameter, command and event path elements.
     */
    abstract val isTerminal: Boolean

    override fun hashCode(): Int {
        return text.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        return text == (other as PathElement).text
    }

    override fun toString(): String {
        return text
    }

    /**
     * Represents an object reference
     *
     * @property text the text of this object for example 'Device.' or '*.'
     * @property instance if this is a wildcard object (i.e. '*.') instance is zero, if it is an
     *           instance object, it matches the instance number, i.e. 2 for '2.', otherwise it is
     *           `null`.
     * @property refFollow the complete reffollow string if there is any, otherwise `null`.
     *           Valid examples are: `#*+` or `#2+` or just `+`
     */
    class Object internal constructor(
        text: String,
        val instance: Int? = null,
        val refFollow: String? = null
    ) : PathElement(text) {

        override val isTerminal = false

        val isWildcard = instance == 0
    }

    class Expression internal constructor(text: String) : PathElement(text) {

        override val isTerminal = false

        /**
         * The search expression, i.e. the text without leading "[" and trailing "]".
         */
        val value = text.substring(1..<text.lastIndexOf(']'))

        /**
         * The search expression components, i.e. the parts which are separated by "&&"
         */
        val components: List<String>
            get() = value.split("&&")
    }

    class Parameter internal constructor(text: String) : PathElement(text) {

        override val isTerminal = true
    }

    class Command internal constructor(text: String) : PathElement(text) {

        override val isTerminal = true
    }

    class Event internal constructor(text: String) : PathElement(text) {

        override val isTerminal = true
    }
}