package de.telekom.usp.datamodel

import com.squareup.kotlinpoet.*
import okio.ByteString
import java.io.File
import kotlin.reflect.KClass

class ValueClassesCreator(private val dataTypes: List<DataType>, private val baseType: ClassName) {

    fun createIn(root: File): Int {
        createHelperClass(root)

        var count = 0
        dataTypes.filter { it.enumerations.isEmpty() && !isBaseClass(it.name) }
            .forEach { template ->
                createValueClass(template).writeTo(root)
                count++
            }

        return count
    }

    private fun createValueClass(dataType: DataType): FileSpec {
        val typeString = if (dataType.base.isNullOrBlank()) {
            dataType.type!!
        } else {
            dataTypes.find { it.name == dataType.base }?.type!!
        }

        val valueClass = classForType(typeString)
        val propertyClass = if (valueClass != PackedInts::class) valueClass else Long::class
        val className = ClassName(PACKAGE_NAME, dataType.name)
        val fileSpec = FileSpec.builder(className)
        val propName = "wrapped"
        val constructorModifiers =
            if (valueClass == PackedInts::class) arrayOf(KModifier.INTERNAL) else emptyArray()

        val type = TypeSpec.classBuilder(className)
            .addAnnotation(JvmInline::class)
            .addModifiers(KModifier.VALUE)
            .addProperty(
                PropertySpec.builder(propName, propertyClass)
                    .initializer("%L", propName)
                    .build()
            )
            .addSuperinterface(baseType)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addModifiers(*constructorModifiers)
                    .addParameter(propName, propertyClass)
                    .build()
            )

        if (!dataType.description.isNullOrBlank()) {
            type.addKdoc("%L", dataType.description)
        }

        // Add additional type specific constructors and functions
        when (valueClass) {
            String::class -> {
                type.addFunction(
                    FunSpec.builder("toString")
                        .addModifiers(KModifier.OVERRIDE)
                        .returns(String::class)
                        .addCode("return $propName")
                        .build()
                )
                if (dataType.patterns.isNotEmpty()) {
                    val companion = TypeSpec.companionObjectBuilder()
                    val patterns = dataType.patterns
                    val patternVars = mutableListOf<String>()
                    val containsBlankPattern = patterns.contains("")

                    companion.addProperty(
                        PropertySpec.builder("allowEmpty", Boolean::class)
                            .addModifiers(KModifier.CONST)
                            .initializer(if (containsBlankPattern) "true" else "false")
                            .build()
                    )
                    patterns.forEachIndexed { index, pattern ->
                        if (pattern.isNotBlank()) {
                            companion.addProperty(
                                PropertySpec.builder("pattern$index", Regex::class)
                                    .initializer("\"\"\"$pattern\"\"\".toRegex()")
                                    .build()
                            )
                            patternVars.add("pattern$index")
                        }
                    }
                    type.addType(companion.build())
                }
            }

            Int::class -> {
                type.addFunction(
                    FunSpec.constructorBuilder()
                        .addParameter("text", String::class)
                        .callThisConstructor("text.toInt()")
                        .build()
                )
                type.addFunction(
                    FunSpec.builder("toString")
                        .addModifiers(KModifier.OVERRIDE)
                        .returns(String::class)
                        .addCode("return $propName.toString()")
                        .build()
                )
            }

            UInt::class -> {
                type.addFunction(
                    FunSpec.constructorBuilder()
                        .addParameter("text", String::class)
                        .callThisConstructor("text.toUInt()")
                        .build()
                )
                type.addFunction(
                    FunSpec.builder("toString")
                        .addModifiers(KModifier.OVERRIDE)
                        .returns(String::class)
                        .addCode("return $propName.toString()")
                        .build()
                )
            }

            ULong::class -> {
                type.addFunction(
                    FunSpec.constructorBuilder()
                        .addParameter("text", String::class)
                        .callThisConstructor("text.toULong()")
                        .build()
                )
                type.addFunction(
                    FunSpec.builder("toString")
                        .addModifiers(KModifier.OVERRIDE)
                        .returns(String::class)
                        .addCode("return $propName.toString()")
                        .build()
                )
            }

            ByteString::class -> {
                val decodeHex = ClassName("okio.ByteString.Companion", "decodeHex")
                type.addFunction(
                    FunSpec.constructorBuilder()
                        .addParameter("text", String::class)
                        .callThisConstructor(CodeBlock.of("text.%T()", decodeHex))
                        .build()
                )
                type.addFunction(
                    FunSpec.builder("toString")
                        .addModifiers(KModifier.OVERRIDE)
                        .returns(String::class)
                        .addCode("return $propName.hex()")
                        .build()
                )
            }

            PackedInts::class -> {
                fileSpec.addFunction(
                    FunSpec.builder(dataType.name)
                        .addParameter("text", String::class)
                        .returns(className)
                        .addCode(
                            """
                            val (value1, value2) = text.split(",").map { it.trim().toInt() }
                            return ${dataType.name}(packInts(value1, value2))
                        """.trimIndent()
                        )
                        .build()
                )
                type.addFunction(
                    FunSpec.constructorBuilder()
                        .addParameter("value1", Int::class)
                        .addParameter("value2", Int::class)
                        .callThisConstructor("packInts(value1, value2)")
                        .build()
                )
                type.addProperty(
                    PropertySpec.builder("value1", Int::class)
                        .getter(
                            FunSpec.getterBuilder()
                                .addCode("return unpackInt1($propName)")
                                .build()
                        )
                        .build()
                )
                type.addProperty(
                    PropertySpec.builder("value2", Int::class)
                        .getter(
                            FunSpec.getterBuilder()
                                .addCode("return unpackInt2($propName)")
                                .build()
                        )
                        .build()
                )
                type.addFunction(
                    FunSpec.builder("component1")
                        .addModifiers(KModifier.OPERATOR)
                        .returns(Int::class)
                        .addCode("return value1")
                        .build()
                )
                type.addFunction(
                    FunSpec.builder("component2")
                        .addModifiers(KModifier.OPERATOR)
                        .returns(Int::class)
                        .addCode("return value2")
                        .build()
                )
                type.addFunction(
                    FunSpec.builder("toString")
                        .addModifiers(KModifier.OVERRIDE)
                        .returns(String::class)
                        .addCode("return \"\$value1,\$value2\"")
                        .build()
                )
            }
        }

        return fileSpec.addType(type.build()).build()
    }

    private fun createHelperClass(root: File) {
        val className = ClassName(PACKAGE_NAME, "InlineClassHelper")
        val fileSpec = FileSpec.builder(className)

        fileSpec.addFunction(
            FunSpec.builder("packInts")
                .addModifiers(KModifier.INLINE)
                .addParameter("val1", Int::class)
                .addParameter("val2", Int::class)
                .returns(Long::class)
                .addCode("return val1.toLong().shl(32) or (val2.toLong() and 0xFFFFFFFF)")
                .build()
        )
        fileSpec.addFunction(
            FunSpec.builder("unpackInt1")
                .addModifiers(KModifier.INLINE)
                .addParameter("value", Long::class)
                .returns(Int::class)
                .addCode("return value.shr(32).toInt()")
                .build()
        )
        fileSpec.addFunction(
            FunSpec.builder("unpackInt2")
                .addModifiers(KModifier.INLINE)
                .addParameter("value", Long::class)
                .returns(Int::class)
                .addCode("return value.and(0xFFFFFFFF).toInt()")
                .build()
        )

        fileSpec.build().writeTo(root)
    }

    private fun isBaseClass(name: String) = dataTypes.firstOrNull { it.base == name } != null

    private fun classForType(type: String): KClass<out Any> {
        return when (type) {
            "string" -> String::class
            "int" -> Int::class
            "unsignedInt" -> UInt::class
            "unsignedLong" -> ULong::class
            "packedInts" -> PackedInts::class
            "hexBinary" -> ByteString::class
            else -> throw IllegalArgumentException("Unexpected type: '$type'")
        }
    }
}