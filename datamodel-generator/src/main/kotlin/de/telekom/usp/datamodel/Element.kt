package de.telekom.usp.datamodel

import org.w3c.dom.Element
import org.w3c.dom.Node

inline fun <R> Element.withFirstChildNamed(tagName: String, handler: Element.() -> R): R? {
    val list = getElementsByTagName(tagName)
    return if (list.length > 0) {
        list.item(0).asElement(handler)
    } else {
        null
    }
}

fun Element.firstChildOrNull(predicate: (Element) -> Boolean): Element? {
    for (index in 0 until childNodes.length) {
        childNodes.item(index).asElement {
            if (predicate(this)) {
                return this
            }
        }
    }
    return null
}

fun Element.withChildren(predicate: (Element) -> Boolean, handler: Element.() -> Unit) {
    val children = childNodes
    for (index in 0 until children.length) {
        val child = children.item(index)
        if ((child.nodeType == Node.ELEMENT_NODE) && predicate(child as Element)) {
            handler(child)
        }
    }
}

fun <R> Element.mapChildren(childName: String, transform: (Element) -> R): List<R> {
    val list = getElementsByTagName(childName)

    return if (list.length > 0) {
        buildList {
            for (index in 0 until list.length) {
                val node = list.item(index)
                if (node.nodeType == Node.ELEMENT_NODE) {
                    add(transform(node as Element))
                }
            }
        }
    } else {
        emptyList()
    }
}

fun Element.childDescriptionText(): String? {
    return withFirstChildNamed("description") {
        textContent.trimIndent().replace('\n', ' ')
    }
}

