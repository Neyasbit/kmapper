package com.syouth.kmapper.processor.convertors

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toTypeName
import com.syouth.kmapper.processor.base.Bundle
import com.syouth.kmapper.processor.base.PathHolder
import com.syouth.kmapper.processor.base.checkSameTypeWithNullabilitySufficient
import com.syouth.kmapper.processor.convertors.models.AssignableStatement

internal class UserDefinedMethodConverter(
    private val from: KSType,
    private val to: KSType,
    method: KSFunctionDeclaration
) : TypeConvertor {

    private val methodSpec = FunSpec
        .builder(method.simpleName.asString())
        .returns(to.toTypeName())
        .addParameter("from", from.toTypeName())
        .build()

    override fun isSupported(from: KSType?, to: KSType, targetPath: PathHolder?): Boolean {
        if (from == null) return false
        return checkSameTypeWithNullabilitySufficient(from, this.from) &&
            to.makeNotNullable() == this.to.makeNotNullable()
    }

    override fun buildConversionStatement(
        fromParameterSpec: ParameterSpec?,
        from: KSType?,
        to: KSType,
        targetPath: PathHolder?,
        bundle: Bundle
    ): AssignableStatement = AssignableStatement(
        code = buildCodeBlock {
            if (fromParameterSpec == null) error("From object name can't be null here")
            add("%N(%N)", methodSpec, fromParameterSpec)
            if (this@UserDefinedMethodConverter.to.nullability == Nullability.NULLABLE &&
                to.nullability == Nullability.NOT_NULL
            ) {
                add(
                    " ?: throw %T(\"Can·not·assign·null·to·non·null·value\")",
                    ClassName("kotlin", "IllegalStateException")
                )
            }
        },
        requiresObjectToConvertFrom = true
    )
}
