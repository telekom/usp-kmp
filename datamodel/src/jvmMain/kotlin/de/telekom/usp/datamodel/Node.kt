package de.telekom.usp.datamodel

import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.StringWriter
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


inline fun <R> Node.asElement(handler: Element.() -> R): R? {
    return if (nodeType == Node.ELEMENT_NODE) {
        handler(this as Element)
    } else {
        null
    }
}

fun Node.asString(): String {
    val writer = StringWriter()
    val transformer: Transformer = TransformerFactory.newInstance().newTransformer()
    transformer.transform(DOMSource(this), StreamResult(writer))
    return writer.toString()
}