package de.telekom.usp.datamodel

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File

const val PACKAGE_NAME = "de.telekom.usp.types"
const val INDENT = "    "

fun main() {
    val parser = DataModelParser().parse()
    val root = File(".", "../core/src/commonMain/kotlin")

    val baseType = ClassName(PACKAGE_NAME, "DataType")
    val baseTypeSpec = TypeSpec.interfaceBuilder(baseType)
    FileSpec.builder(baseType)
        .indent(INDENT)
        .addType(baseTypeSpec.build())
        .build()
        .writeTo(root)

    val enums = EnumerationsCreator(parser.dataTypes, baseType).createIn(root)
    println("Created $enums enumeration classes")

    val classes = ValueClassesCreator(parser.dataTypes, baseType).createIn(root)
    println("Created $classes value classes")
}