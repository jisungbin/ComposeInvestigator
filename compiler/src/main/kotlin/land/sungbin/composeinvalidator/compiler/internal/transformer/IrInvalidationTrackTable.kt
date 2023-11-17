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
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.createParameterDeclarations
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.util.superClass
import org.jetbrains.kotlin.load.kotlin.PackagePartClassUtils
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

internal class IrInvalidationTrackTable private constructor(val irClass: IrClass) {
  private var putParamsIfAbsentFn: IrSimpleFunction? = null

  private var _paramInfoSymbol: IrClassSymbol? = null
  val paramInfoSymbol: IrClassSymbol get() = _paramInfoSymbol!!

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
    val putParamsIfAbsent =
      putParamsIfAbsentFn ?: (irClass.superClass!!.declarations.filterIsInstance<IrSimpleFunction>()
        .single { fn -> fn.name.asString() == "putParamsIfAbsent" }
        .also { fn -> putParamsIfAbsentFn = fn })

    return IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = putParamsIfAbsent.symbol,
    ).also { fn ->
      fn.dispatchReceiver = IrGetValueImpl(
        startOffset = UNDEFINED_OFFSET,
        endOffset = UNDEFINED_OFFSET,
        symbol = irClass.thisReceiver!!.symbol,
      )
    }.apply {
      putValueArgument(0, name)
      putValueArgument(1, paramInfos)
    }
  }

  internal companion object {
    fun create(context: IrPluginContext, currentFile: IrFile): IrInvalidationTrackTable =
      IrInvalidationTrackTable(irComposableInvalidationTrackTableClass(context, currentFile))
        .also { clz ->
          clz._paramInfoSymbol = context.referenceClass(ClassId.topLevel(PARAMETER_INFO_FQN))!!
        }
  }
}

private var composableInvalidationTrackTableSymbol: IrClassSymbol? = null

private fun irComposableInvalidationTrackTableClass(
  context: IrPluginContext,
  currentFile: IrFile,
): IrClass =
  context.irFactory
    .buildClass {
      kind = ClassKind.OBJECT
      visibility = DescriptorVisibilities.INTERNAL

      val fileName = currentFile.fileEntry.name.split('/').last()
      val shortName = PackagePartClassUtils.getFilePartShortName(fileName)
      name = Name.identifier("ComposableInvalidationTrackTableImpl\$$shortName")
    }.also { clz ->
      val superClass = composableInvalidationTrackTableSymbol ?: (
        context.referenceClass(ClassId.topLevel(COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN))!!
          .also { symbol -> composableInvalidationTrackTableSymbol = symbol }
        )

      clz.also {
        clz.createParameterDeclarations()
        clz.superTypes = listOf(superClass.defaultType)
      }.addConstructor {
        isPrimary = true
        visibility = DescriptorVisibilities.INTERNAL
      }.also { ctor ->
        ctor.body =
          DeclarationIrBuilder(context, clz.symbol).irBlockBody {
            +irDelegatingConstructorCall(superClass.owner.primaryConstructor!!)
            +IrInstanceInitializerCallImpl(
              startOffset = startOffset,
              endOffset = endOffset,
              classSymbol = clz.symbol,
              type = clz.defaultType,
            )
          }
      }
    }
