package de.telekom.usp.datamodel

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File

const val INDENT = "    "
const val PACKAGE_NAME = "de.telekom.usp.types"

val GeneratedClassName = ClassName(PACKAGE_NAME, "Generated")
val DataTypeClassName = ClassName(PACKAGE_NAME, "DataType")

fun main() {
    val parser = DataModelParser().parse()
    val root = File(".", "../core/src/commonMain/kotlin")

    createBaseClassesIn(root)

    val enums = EnumerationsCreator(parser.dataTypes).createIn(root)
    println("Created $enums enumeration classes")

    val classes = ValueClassesCreator(parser.dataTypes).createIn(root)
    println("Created $classes value classes")
}

private fun createBaseClassesIn(root: File) {
    val genTypeSpec = TypeSpec.annotationBuilder(GeneratedClassName)
        .addKdoc("Used by tools like JaCoCo.")
        .addAnnotation(
            AnnotationSpec.builder(Retention::class)
                .addMember("%L", "AnnotationRetention.RUNTIME")
                .build()
        )

    FileSpec.uspBuilder(GeneratedClassName)
        .addType(genTypeSpec.build())
        .build()
        .writeTo(root)

    val baseTypeSpec =
        TypeSpec.interfaceBuilder(DataTypeClassName).addAnnotation(GeneratedClassName)
    FileSpec.uspBuilder(DataTypeClassName)
        .addType(baseTypeSpec.build())
        .build()
        .writeTo(root)
}

fun FileSpec.Companion.uspBuilder(className: ClassName): FileSpec.Builder {
    return builder(className)
        .indent(INDENT)
        .addFileComment("%L", "\nAuto generated code - do not edit!\n")
}