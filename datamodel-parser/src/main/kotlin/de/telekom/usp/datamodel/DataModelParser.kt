/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.telekom.usp.datamodel

import org.w3c.dom.Element
import org.w3c.dom.Node
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

        root.withChildren({ it.nodeName == "model" }) {
            parseModel(this)
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
        return element?.nodeName == "list"
                && element.getAttribute("minItems") == "2"
                && element.getAttribute("maxItems") == "2"
    }

    //
    // Currently not of any use, might be of interest in the future
    //
    private fun parseModel(model: Element) {
        var count = 0
        val paths = mutableListOf<TerminalPath>()
        val instantiablePaths = mutableSetOf<String>()

        model.withChildren({ it.nodeName == "object" }) {
            val basePath = getAttribute("name")

            if (basePath.endsWith("{i}.")) {
                instantiablePaths.add(basePath)
            }
            val printElement: (Element) -> Unit = {
                count++
                // println("${java.lang.String.format("%04d", count)} $basePath${it.getAttribute("name")}")
            }
            childNodes.filter { it.nodeType == Node.ELEMENT_NODE }.forEach {
                val child = it as Element
                val name = basePath + child.getAttribute("name")
                when (val nodeName = child.nodeName) {
                    "parameter" -> {
                        printElement(child)
                        // TODO: read the data type of the parameter
                        paths.add(
                            TerminalPath.Parameter(
                                name,
                                child.childDescriptionText(),
                                child.getAttribute("access")
                            )
                        )
                    }

                    "command" -> {
                        printElement(child)
                        paths.add(
                            TerminalPath.Command(
                                name,
                                child.childDescriptionText(),
                                child.getAttribute("async") == "true"
                            )
                        )
                    }

                    "event" -> {
                        printElement(child)
                        paths.add(
                            TerminalPath.Event(
                                name,
                                child.childDescriptionText(),
                                child.getAttribute("id")
                            )
                        )
                    }

                    "description" -> {}
                    "uniqueKey" -> {}
                    else -> {
                        throw IllegalArgumentException("Unexpected element named: $nodeName")
                    }
                }
            }
        }
        // println("---------- sum of path elements: $count")
        // println("---------- number of instantiable paths: ${instantiablePaths.size}")
    }
}
