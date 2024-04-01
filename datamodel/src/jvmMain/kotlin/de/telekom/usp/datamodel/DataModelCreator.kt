package de.telekom.usp.datamodel

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.reflect.KClass

class DataModelCreator {

    private val dataTypes = mutableListOf<DataTypeTemplate>()

    private val types = mutableSetOf<String>()

    private val baseType = ClassName("de.telekom.usp.datamodel", "DataType")

    private val root by lazy {
        File(".", "src/commonMain/kotlin")
    }

    fun readXml(): DataModelCreator {
        val document =
            this::class.java.getResourceAsStream("/tr-181-2-17-0-usp-full.xml").use { stream ->
                DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream)
            }

        document.documentElement.normalize()
        val root = document.documentElement
        println("Root name: <${root.nodeName}>")
        val dataTypes = root.getElementsByTagName("dataType")

        for (index in 0 until dataTypes.length) {
            dataTypes.item(index).asElement {
                if (hasAttribute("name")) {
                    val type = firstChildOrNull { it.nodeName != "description" }
                    val enumerations = parseEnumerations(this)
                    this@DataModelCreator.dataTypes.add(
                        DataTypeTemplate(
                            getAttribute("name"),
                            getAttribute("base"),
                            type?.nodeName,
                            childDescriptionText(),
                            enumerations
                        )
                    )
                    if (type != null) {
                        types.add(type.nodeName)
                    }
                }
            }
        }
        println(this.types)

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

    fun createInterface(): DataModelCreator {
        val type = TypeSpec.interfaceBuilder(baseType)
        FileSpec.builder(baseType).addType(type.build()).build().writeTo(root)

        return this
    }

    fun createEnumerations(): DataModelCreator {
        dataTypes.filter { it.enumerations.isNotEmpty() }.forEach {
            createEnumeration(it).writeTo(root)
        }

        return this
    }

    fun createValueClasses(): DataModelCreator {
        dataTypes.filter { it.enumerations.isEmpty() && !isBaseClass(it.name) }
            .forEach { template ->
                createValueClass(template).writeTo(System.out)
            }
        return this
    }

    private fun createValueClass(dataType: DataTypeTemplate): FileSpec {
        val typeString = if (dataType.base.isNullOrBlank()) {
            dataType.type!!
        } else {
            dataTypes.find { it.name == dataType.base }?.type!!
        }

        val valueClass = classForType(typeString)
        val className = ClassName("de.telekom.usp.datamodel", dataType.name)
        val propName = "wrapped"

        val type = TypeSpec.classBuilder(className)
            .addAnnotation(JvmInline::class)
            .addModifiers(KModifier.VALUE)
            .addProperty(
                PropertySpec.builder(propName, valueClass)
                    .initializer("%L", propName)
                    .build()
            )
            .addSuperinterface(baseType)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(propName, valueClass)
                    .build()
            )

        return FileSpec.builder(className).addType(type.build()).build()
    }

    private fun isBaseClass(name: String) = dataTypes.firstOrNull { it.base == name } != null

    private fun classForType(type: String): KClass<out Any> {
        return when (type) {
            "string" -> String::class
            "int" -> Int::class
            "unsignedInt" -> Int::class
            "unsignedLong" -> Long::class
            "list" -> List::class
            "hexBinary" -> ByteArray::class
            else -> throw IllegalArgumentException("Unexpected type: $type")
        }
    }

    private fun createEnumeration(dataType: DataTypeTemplate): FileSpec {
        val className = ClassName("de.telekom.usp.datamodel", dataType.name)
        val hasCode = dataType.hasCode

        val type = TypeSpec.enumBuilder(className)
            .addProperty(
                PropertySpec.builder("text", String::class)
                    .initializer("text")
                    .build()
            )
            .addSuperinterface(baseType)

        val companion = TypeSpec.companionObjectBuilder()
            .addFunction(
                FunSpec.builder("from")
                    .addParameter("text", String::class)
                    .returns(className.copy(nullable = true))
                    .addCode(
                        """
                    return entries.firstOrNull { it.text·==·text }
                    """.trimIndent()
                    ).build()
            )

        if (hasCode) {
            type.primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("text", String::class)
                    .addParameter("code", Int::class)
                    .build()
            )
                .addProperty(
                    PropertySpec.builder("code", Int::class)
                        .initializer("code")
                        .build()
                )
            companion.addFunction(
                FunSpec.builder("from")
                    .addParameter("code", Int::class)
                    .returns(className.copy(nullable = true))
                    .addCode(
                        """
                    return entries.firstOrNull { it.code·==·code }
                    """.trimIndent()
                    ).build()
            )
        } else {
            type.primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("text", String::class)
                    .build()
            )
        }

        type.addType(companion.build())

        if (!dataType.description.isNullOrBlank()) {
            type.addKdoc("%L", dataType.description)
        }

        dataType.enumerations.forEach {
            val typeSpec =
                TypeSpec.anonymousClassBuilder().addSuperclassConstructorParameter("%S", it.value)
            if (hasCode) {
                typeSpec.addSuperclassConstructorParameter("%L", it.code!!)
            }
            if (!it.description.isNullOrBlank()) {
                typeSpec.addKdoc("%L", it.description)
            }

            type.addEnumConstant(toEnumName(it.value), typeSpec.build())
        }

        return FileSpec.builder(className).addType(type.build()).build()
    }

    private fun toEnumName(name: String): String {
        if (name.length == 1) {
            return when (name.first()) {
                '-' -> "DIMENSIONLESS"
                '%' -> "PERCENT"
                '$' -> "DOLLAR"
                'A' -> "AMPERE"
                'C' -> "COULOMB"
                'F' -> "FARAD"
                'g' -> "GRAM"
                'h' -> "HOUR"
                'H' -> "HENRY"
                'J' -> "JOULE"
                'K' -> "KELVIN"
                'l' -> "LITER"
                'm' -> "METER"
                'N' -> "NEWTON"
                'T' -> "TESLA"
                's' -> "SECOND"
                'S' -> "SIEMENS"
                'V' -> "VOLT"
                'W' -> "WATT"
                else -> throw IllegalArgumentException("Unexpected single character name: '$name'")
            }
        }

        return buildString {
            // Insert an underscore, when switching from lower to upper case or between digit/non-digit:
            name.windowed(2, 1, true) { chars ->
                append(chars[0])

                if (chars.length > 1 &&
                    ((chars[0].isLowerCase() && chars[1].isUpperCase()) ||
                            (chars[0].isDigit() && !chars[1].isDigit()) ||
                            (!chars[0].isDigit() && chars[1].isDigit()))
                ) {
                    append('_')
                }
            }
        }.uppercase().replace('-', '_')
    }
}

data class DataTypeTemplate(
    val name: String,
    val base: String?,
    val type: String?,
    val description: String?,
    val enumerations: List<Enumeration>
) {
    val hasCode: Boolean
        get() = enumerations.isNotEmpty() && enumerations[0].code != null
}

data class Enumeration(val value: String, val code: Int?, val description: String?)

fun main() {
    DataModelCreator().readXml().createInterface().createEnumerations().createValueClasses()
}