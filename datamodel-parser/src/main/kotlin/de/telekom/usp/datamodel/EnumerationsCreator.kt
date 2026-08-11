/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.telekom.usp.datamodel

import com.squareup.kotlinpoet.*
import java.io.File

class EnumerationsCreator(private val dataTypes: List<DataType>) {

    fun createIn(root: File): Int {
        var count = 0
        dataTypes.filter { it.enumerations.isNotEmpty() }.forEach {
            createEnumeration(it).writeTo(root)
            count++
        }
        return count
    }

    private fun createEnumeration(dataType: DataType): FileSpec {
        val className = ClassName(PACKAGE_NAME, dataType.name)
        val hasCode = dataType.hasCode

        val type = TypeSpec.enumBuilder(className)
            .addProperty(
                PropertySpec.builder("text", String::class)
                    .initializer("text")
                    .build()
            )
            .addSuperinterface(DataTypeClassName)
            .addAnnotation(GeneratedClassName)

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

        return FileSpec.uspBuilder(className).addType(type.build()).build()
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