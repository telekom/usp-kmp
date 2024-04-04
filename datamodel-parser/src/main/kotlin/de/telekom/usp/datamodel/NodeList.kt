package de.telekom.usp.datamodel

import org.w3c.dom.Node
import org.w3c.dom.NodeList

fun NodeList.filter(predicate: (Node) -> Boolean): List<Node> {
    return buildList {
        for (index in 0 until length) {
            if (predicate(item(index))) {
                add(item(index))
            }
        }
    }
}