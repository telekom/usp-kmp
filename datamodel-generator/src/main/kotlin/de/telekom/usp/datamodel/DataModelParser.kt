package de.telekom.usp.datamodel

import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

class DataModelParser {

    val dataTypes = mutableListOf<DataType>()

    fun parse(): DataModelParser {
        val document =
            this::class.java.getResourceAsStream("/tr-181-2-17-0-usp-full.xml").use { stream ->
                DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream)
            }

        document.documentElement.normalize()
        val root = document.documentElement
        val typeElements = root.getElementsByTagName("dataType")

        for (index in 0 until typeElements.length) {
            typeElements.item(index).asElement {
                if (hasAttribute("name")) {
                    val type = firstChildOrNull { it.nodeName != "description" }
                    val typeName = if (isIntPairType(type)) "packedInts" else type?.nodeName
                    val enumerations = parseEnumerations(this)
                    val patterns = parsePatterns(this)

                    dataTypes.add(
                        DataType(
                            getAttribute("name"),
                            getAttribute("base"),
                            typeName,
                            childDescriptionText(),
                            enumerations,
                            patterns
                        )
                    )
                }
            }
        }
        return this
    }

    private fun parseEnumerations(element: Element): List<Enumeration> {
        return element.withFirstChildNamed("string") {
            mapChildren("enumeration") {
                Enumeration(
                    it.getAttribute("value"),
                    it.getAttribute("code").toIntOrNull(),
                    it.childDescriptionText()
                )
            }
        } ?: emptyList()
    }

    private fun parsePatterns(element: Element): List<String> {
        return element.mapChildren("pattern") { it.getAttribute("value") }
    }

    private fun isIntPairType(element: Element?): Boolean {
        return element?.nodeName == "list" && element.getAttribute("minItems") == "2" && element.getAttribute(
            "maxItems"
        ) == "2"
    }
}
