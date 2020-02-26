package com.rxhttp.compiler

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.asTypeName
import rxhttp.wrapper.annotation.Converter
import java.util.*
import javax.lang.model.element.VariableElement

class ConverterAnnotatedClass {

    private val mElementMap = LinkedHashMap<String, VariableElement>()

    fun add(variableElement: VariableElement) {
        val annotation = variableElement.getAnnotation(Converter::class.java)
        var name: String = annotation.name
        if (name.isEmpty()) {
            name = variableElement.simpleName.toString()
        }
        mElementMap[name] = variableElement
    }

    val methodList: List<FunSpec>
        get() {
            val funList = ArrayList<FunSpec>()
            for ((key, value) in mElementMap) {
                funList.add(
                    FunSpec.builder("set$key")
                        .addModifiers(KModifier.PUBLIC)
                        .addStatement("""
                            this.localConverter = %T.%L
                            ?: throw IllegalArgumentException("converter can not be null")
                        """.trimIndent(),
                            value.enclosingElement.asType().asTypeName(),
                            value.simpleName.toString())
                        .addStatement("return this as R")
                        .returns(RxHttpGenerator.r)
                        .build())
            }
            funList.add(
                FunSpec.builder("setConverter")
                    .addModifiers(KModifier.PROTECTED)
                    .addKdoc("给Param设置转换器，此方法会在请求发起前，被RxHttp内部调用\n")
                    .addParameter("param", RxHttpGenerator.p)
                    .addStatement("param.tag(IConverter::class.java,localConverter)")
                    .addStatement("return this as R")
                    .returns(RxHttpGenerator.r)
                    .build())
            return funList
        }
}