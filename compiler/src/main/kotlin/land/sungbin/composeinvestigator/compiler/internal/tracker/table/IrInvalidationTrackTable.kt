/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.internal.tracker.table

import land.sungbin.composeinvestigator.compiler.internal.COMPOSABLE_INVALIDATION_TRACK_TABLE_CALL_LISTENERS_FQN
import land.sungbin.composeinvestigator.compiler.internal.COMPOSABLE_INVALIDATION_TRACK_TABLE_COMPUTE_INVALIDATION_REASON_FQN
import land.sungbin.composeinvestigator.compiler.internal.COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN
import land.sungbin.composeinvestigator.compiler.internal.fromFqName
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
import org.jetbrains.kotlin.ir.expressions.IrDeclarationReference
import org.jetbrains.kotlin.ir.expressions.IrValueAccessExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrExpressionBodyImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.load.kotlin.PackagePartClassUtils
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

public class IrInvalidationTrackTable private constructor(public val prop: IrProperty) {
  private var computeInvalidationReasonSymbol: IrSimpleFunctionSymbol? = null
  private var callListenersSymbol: IrSimpleFunctionSymbol? = null

  public fun irCallListeners(
    key: IrConst<String>,
    composable: IrDeclarationReference,
    type: IrDeclarationReference,
  ): IrCall = IrCallImpl.fromSymbolOwner(
    startOffset = UNDEFINED_OFFSET,
    endOffset = UNDEFINED_OFFSET,
    symbol = callListenersSymbol!!,
  ).also { fn ->
    fn.dispatchReceiver = propGetter()
  }.apply {
    putValueArgument(0, key)
    putValueArgument(1, composable)
    putValueArgument(2, type)
  }

  public fun irComputeInvalidationReason(
    composableKeyName: IrConst<String>,
    fields: IrValueAccessExpression,
  ): IrCall = IrCallImpl.fromSymbolOwner(
    startOffset = UNDEFINED_OFFSET,
    endOffset = UNDEFINED_OFFSET,
    symbol = computeInvalidationReasonSymbol!!,
  ).also { fn ->
    fn.dispatchReceiver = propGetter()
  }.apply {
    putValueArgument(0, composableKeyName)
    putValueArgument(1, fields)
  }

  public companion object {
    public fun create(context: IrPluginContext, currentFile: IrFile): IrInvalidationTrackTable =
      IrInvalidationTrackTable(irInvalidationTrackTableProp(context, currentFile))
        .also { clz ->
          clz.computeInvalidationReasonSymbol =
            context
              .referenceFunctions(CallableId.fromFqName(COMPOSABLE_INVALIDATION_TRACK_TABLE_COMPUTE_INVALIDATION_REASON_FQN))
              .single()
          clz.callListenersSymbol =
            context
              .referenceFunctions(CallableId.fromFqName(COMPOSABLE_INVALIDATION_TRACK_TABLE_CALL_LISTENERS_FQN))
              .single()
        }
  }
}

public fun IrInvalidationTrackTable.propGetter(): IrCall =
  IrCallImpl.fromSymbolOwner(
    startOffset = UNDEFINED_OFFSET,
    endOffset = UNDEFINED_OFFSET,
    symbol = prop.getter!!.symbol,
  )

private var invalidationTrackTableClassSymbol: IrClassSymbol? = null

private fun irInvalidationTrackTableProp(context: IrPluginContext, currentFile: IrFile): IrProperty {
  val fileName = currentFile.fileEntry.name.split('/').last()
  val shortName = PackagePartClassUtils.getFilePartShortName(fileName)
  val propName = Name.identifier("ComposableInvalidationTrackTableImpl\$$shortName")

  val superSymbol = invalidationTrackTableClassSymbol ?: (
    context.referenceClass(ClassId.topLevel(COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN))!!
      .also { symbol -> invalidationTrackTableClassSymbol = symbol })

  return context.irFactory.buildProperty {
    visibility = DescriptorVisibilities.PRIVATE
    name = propName
  }.also { prop ->
    prop.parent = currentFile
    prop.backingField = context.irFactory.buildField {
      name = propName
      isStatic = true
      isFinal = true
      type = superSymbol.defaultType
      visibility = DescriptorVisibilities.PRIVATE
    }.also { field ->
      field.parent = currentFile
      field.correspondingPropertySymbol = prop.symbol
      field.initializer = IrExpressionBodyImpl(
        startOffset = SYNTHETIC_OFFSET,
        endOffset = SYNTHETIC_OFFSET,
        expression = IrConstructorCallImpl.fromSymbolOwner(
          type = superSymbol.defaultType,
          constructorSymbol = superSymbol.constructors.single(),
        ),
      )
    }
    prop.addGetter {
      returnType = superSymbol.defaultType
      visibility = DescriptorVisibilities.PRIVATE
      origin = IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR
    }.also { getter ->
      getter.body = DeclarationIrBuilder(context, getter.symbol).irBlockBody {
        +irReturn(irGetField(null, prop.backingField!!))
      }
    }
  }
}
