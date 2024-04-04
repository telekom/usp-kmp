package de.telekom.usp.datamodel

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File

const val PACKAGE_NAME = "de.telekom.usp.types"
const val INDENT = "    "

fun main() {
    val baseType = ClassName(PACKAGE_NAME, "DataType")
    val root = File(".", "../datamodel/src/commonMain/kotlin")
    val parser = DataModelParser().parse()

    val baseTypeSpec = TypeSpec.interfaceBuilder(baseType)
    FileSpec.builder(baseType).addType(baseTypeSpec.build()).build().writeTo(root)

    val enums = EnumerationsCreator(parser.dataTypes, baseType).createIn(root)
    println("Created $enums enumeration classes")

    val classes = ValueClassesCreator(parser.dataTypes, baseType).createIn(root)
    println("Created $classes value classes")
}