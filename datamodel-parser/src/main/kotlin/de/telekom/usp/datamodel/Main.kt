package de.telekom.usp.datamodel

import com.squareup.kotlinpoet.*
import java.io.File

const val INDENT = "    "
const val PACKAGE_NAME = "de.telekom.usp.types"

val GeneratedClassName = ClassName(PACKAGE_NAME, "Generated")
val DataTypeClassName = ClassName(PACKAGE_NAME, "DataType")
val StringClassName = ClassName(PACKAGE_NAME, "String")

fun main() {
    val parser = DataModelParser().parse()
    val root = File(".", "../usp-core/src/commonMain/kotlin")

    createBaseClassesIn(root)

    val enums = EnumerationsCreator(parser.dataTypes).createIn(root)
    println("Created $enums enumeration classes")

    val classes = ValueClassesCreator(parser.dataTypes).createIn(root)
    println("Created $classes value classes")
}

private fun createBaseClassesIn(root: File) {
    // Generated annotation type:
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

    // Base interface for all types:
    val baseTypeSpec =
        TypeSpec.interfaceBuilder(DataTypeClassName).addAnnotation(GeneratedClassName)
    FileSpec.uspBuilder(DataTypeClassName)
        .addType(baseTypeSpec.build())
        .build()
        .writeTo(root)

    // String extension function for booleans:
    val stringFunSpec = FunSpec.builder("isTrue")
        .receiver(String::class.asTypeName().copy(nullable = true))
        .returns(Boolean::class)
        .addKdoc(
            "%L",
            "Determines whether a USP parameter value represents a boolean value of `true`."
        )
        .addCode(
            """
            return this != null && (this == "true" || this == "1")
        """.trimIndent()
        )
        .build()

    FileSpec.uspBuilder(StringClassName)
        .addFunction(stringFunSpec)
        .build()
        .writeTo(root)
}

fun FileSpec.Companion.uspBuilder(className: ClassName): FileSpec.Builder {
    return builder(className)
        .indent(INDENT)
        .addFileComment("%L", "\nAuto generated code - do not edit!\n")
}