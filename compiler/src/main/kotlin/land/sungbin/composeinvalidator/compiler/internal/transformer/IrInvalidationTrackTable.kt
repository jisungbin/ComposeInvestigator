/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

package land.sungbin.composeinvalidator.compiler.internal.transformer

import land.sungbin.composeinvalidator.compiler.internal.COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN
import land.sungbin.composeinvalidator.compiler.internal.PARAMETER_INFO_FQN
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.addGetter
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.builders.declarations.buildProperty
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrExpressionBodyImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.load.kotlin.PackagePartClassUtils
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

internal class IrInvalidationTrackTable private constructor(val prop: IrProperty) {
  private var _paramInfoSymbol: IrClassSymbol? = null
  val paramInfoSymbol: IrClassSymbol get() = _paramInfoSymbol!!

  private var putParamsIfAbsentSymbol: IrSimpleFunctionSymbol? = null

  fun obtainParameterInfo(
    name: IrConst<String>,
    stability: IrExpression,
    valueString: IrExpression,
    hashCode: IrExpression,
  ): IrConstructorCall =
    IrConstructorCallImpl
      .fromSymbolOwner(
        type = paramInfoSymbol.owner.defaultType,
        constructorSymbol = paramInfoSymbol.constructors.single(),
      )
      .apply {
        putValueArgument(0, name)
        putValueArgument(1, stability)
        putValueArgument(2, valueString)
        putValueArgument(3, hashCode)
      }

  fun irPutParamsIfAbsent(name: IrConst<String>, paramInfos: IrVararg): IrCall {
    val propGetter = IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = prop.getter!!.symbol,
    )

    return IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = putParamsIfAbsentSymbol!!,
    ).also { fn ->
      fn.dispatchReceiver = propGetter
    }.apply {
      putValueArgument(0, name)
      putValueArgument(1, paramInfos)
    }
  }

  internal companion object {
    fun create(context: IrPluginContext, currentFile: IrFile): IrInvalidationTrackTable =
      IrInvalidationTrackTable(irInvalidationTrackTableProp(context, currentFile))
        .also { clz ->
          clz._paramInfoSymbol = context.referenceClass(ClassId.topLevel(PARAMETER_INFO_FQN))!!
          clz.putParamsIfAbsentSymbol =
            context
              .referenceFunctions(
                CallableId(
                  packageName = COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN,
                  callableName = Name.identifier("putParamsIfAbsent"),
                ),
              ).single()
        }
  }
}

private var invalidationTrackTableClassSymbol: IrClassSymbol? = null

private fun irInvalidationTrackTableProp(
  context: IrPluginContext,
  currentFile: IrFile,
): IrProperty {
  val fileName = currentFile.fileEntry.name.split('/').last()
  val shortName = PackagePartClassUtils.getFilePartShortName(fileName)
  val propName = Name.identifier("ComposableInvalidationTrackTableImpl\$$shortName")

  val superSymbol = invalidationTrackTableClassSymbol ?: (
    context.referenceClass(ClassId.topLevel(COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN))!!
      .also { symbol -> invalidationTrackTableClassSymbol = symbol }
    )

  return context.irFactory.buildProperty {
    visibility = DescriptorVisibilities.PRIVATE
    name = propName
  }.also { prop ->
    prop.parent = currentFile
    prop.backingField = context.irFactory.buildField {
      name = propName
      isStatic = true
      isFinal = true
      type = superSymbol.owner.defaultType
      visibility = DescriptorVisibilities.PRIVATE
    }.also { field ->
      field.correspondingPropertySymbol = prop.symbol
      field.parent = currentFile
      field.initializer = IrExpressionBodyImpl(
        startOffset = SYNTHETIC_OFFSET,
        endOffset = SYNTHETIC_OFFSET,
        expression = IrConstructorCallImpl.fromSymbolOwner(
          type = superSymbol.owner.defaultType,
          constructorSymbol = superSymbol.constructors.single(),
        ),
      )
    }
    prop.addGetter {
      returnType = superSymbol.owner.defaultType
      visibility = DescriptorVisibilities.PRIVATE
      origin = IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR
    }.also { getter ->
      getter.body = DeclarationIrBuilder(context, getter.symbol).irBlockBody {
        +irReturn(irGetField(null, prop.backingField!!))
      }
    }
  }
}
