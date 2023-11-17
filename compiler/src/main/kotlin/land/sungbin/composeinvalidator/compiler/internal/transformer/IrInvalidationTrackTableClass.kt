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
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.createParameterDeclarations
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.load.kotlin.PackagePartClassUtils
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

internal class IrInvalidationTrackTableClass private constructor(private val value: IrClass? = null) {
  private var putParamsIfAbsentFn: IrSimpleFunction? = null

  var paramInfoSymbol: IrClassSymbol? = null

  fun get(): IrClass = requireNotNull(value) { "IrInvalidationTrackTableClass is not created" }

  fun obtainParameterInfo(
    name: IrConst<String>,
    stability: IrExpression,
    valueString: IrConst<String>,
    hashCode: IrCall,
  ): IrConstructorCall {
    val paramInfo = paramInfoSymbol!!

    return IrConstructorCallImpl
      .fromSymbolOwner(
        type = paramInfo.owner.defaultType,
        constructorSymbol = paramInfo.constructors.single(),
      )
      .apply {
        putValueArgument(0, name)
        putValueArgument(1, stability)
        putValueArgument(2, valueString)
        putValueArgument(3, hashCode)
      }
  }

  fun irPutParamsIfAbsent(name: IrConst<String>, paramInfos: IrVararg): IrCall {
    val table = get()
    val putParamsIfAbsent =
      putParamsIfAbsentFn ?: (table.declarations.filterIsInstance<IrSimpleFunction>()
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
        symbol = table.thisReceiver!!.symbol,
      )
    }.apply {
      putValueArgument(0, name)
      putValueArgument(1, paramInfos)
    }
  }

  internal companion object {
    fun empty(): IrInvalidationTrackTableClass = IrInvalidationTrackTableClass()

    fun create(context: IrPluginContext, currentFile: IrFile): IrInvalidationTrackTableClass =
      IrInvalidationTrackTableClass(irComposableInvalidationTrackTableClass(context, currentFile))
        .also { clz ->
          clz.paramInfoSymbol = context.referenceClass(ClassId.topLevel(PARAMETER_INFO_FQN))!!
        }
  }
}

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
    }
    .also { clz ->
      val superClass = context.referenceClass(ClassId.topLevel(COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN))!!
      clz.superTypes = listOf(superClass.defaultType)

      clz.createParameterDeclarations()
      clz
        .addConstructor { isPrimary = true }
        .also { ctor ->
          ctor.body =
            DeclarationIrBuilder(context, clz.symbol).irBlockBody {
              +irDelegatingConstructorCall(
                context
                  .irBuiltIns
                  .anyClass
                  .owner
                  .primaryConstructor!!
              )
            }
        }
    }
