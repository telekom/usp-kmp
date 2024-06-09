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

    /**
     * Determines whether this element is resolved (like a fixed object path) or not resolved, like
     * a wildcard or a search expression.
     */
    abstract val isResolved: Boolean

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
     *           `null`
     * @property refFollow the reference following directive if there is any, otherwise `null`
     */
    class Object internal constructor(
        text: String,
        val instance: Int? = null,
        val refFollow: ReferenceFollowing? = null
    ) : PathElement(text) {

        override val isTerminal = false

        override val isResolved = instance != 0 && refFollow == null
    }

    class Expression internal constructor(text: String) : PathElement(text) {

        override val isTerminal = false

        override val isResolved = false

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

        override val isResolved = true
    }

    class Command internal constructor(text: String) : PathElement(text) {

        override val isTerminal = true

        override val isResolved = true
    }

    class Event internal constructor(text: String) : PathElement(text) {

        override val isTerminal = true

        override val isResolved = true
    }

    /**
     * The placeholder path element is reserved for SupportedDataModelPath instances only.
     */
    object Placeholder : PathElement("{i}.") {

        override val isTerminal = false

        override val isResolved = false
    }
}

fun List<PathElement>.isResolved(): Boolean {
    return all { it.isResolved }
}