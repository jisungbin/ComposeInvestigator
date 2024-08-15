/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.struct

import java.lang.ref.WeakReference
import land.sungbin.composeinvestigator.compiler.COMPOSABLE_INVALIDATION_TRACE_TABLE_FQN
import land.sungbin.composeinvestigator.compiler.ComposableInvalidationTraceTable_COMPUTE_INVALIDATION_REASON
import land.sungbin.composeinvestigator.compiler.REGISTER_STATE_OBJECT_FQN
import land.sungbin.composeinvestigator.compiler.fromFqName
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
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrValueAccessExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.load.kotlin.PackagePartClassUtils
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

public class IrInvalidationTraceTable private constructor(private val prop: IrProperty) {
  private lateinit var registerStateObject: IrSimpleFunctionSymbol
  private lateinit var computeInvalidationReasonSymbol: IrSimpleFunctionSymbol

  internal val rawProp get() = prop

  public fun propGetter(
    startOffset: Int = UNDEFINED_OFFSET,
    endOffset: Int = UNDEFINED_OFFSET,
  ): IrCall = IrCallImpl.fromSymbolOwner(
    startOffset = startOffset,
    endOffset = endOffset,
    symbol = prop.getter!!.symbol,
  )

  public fun irRegisterStateObject(
    value: IrExpression,
    name: IrConst<String>,
  ): IrCall = IrCallImpl.fromSymbolOwner(
    startOffset = UNDEFINED_OFFSET,
    endOffset = UNDEFINED_OFFSET,
    symbol = registerStateObject,
  ).also { fn ->
    fn.dispatchReceiver = propGetter()
    fn.type = value.type
  }.apply {
    putValueArgument(0, value)
    putValueArgument(1, name)
  }

  public fun irComputeInvalidationReason(
    keyName: IrConst<String>,
    arguments: IrValueAccessExpression,
  ): IrCall = IrCallImpl.fromSymbolOwner(
    startOffset = UNDEFINED_OFFSET,
    endOffset = UNDEFINED_OFFSET,
    symbol = computeInvalidationReasonSymbol,
  ).also { fn ->
    fn.dispatchReceiver = propGetter()
  }.apply {
    putValueArgument(0, keyName)
    putValueArgument(1, arguments)
  }

  public companion object {
    public fun create(context: IrPluginContext, currentFile: IrFile): IrInvalidationTraceTable =
      IrInvalidationTraceTable(irInvalidationTraceTableProp(context, currentFile)).also { table ->
        val symbol = table.prop.backingField!!.type.classOrFail
        table.registerStateObject = context.referenceFunctions(CallableId.fromFqName(REGISTER_STATE_OBJECT_FQN)).single()
        table.computeInvalidationReasonSymbol = symbol.getSimpleFunction(ComposableInvalidationTraceTable_COMPUTE_INVALIDATION_REASON.asString())!!
      }
  }
}

@Volatile private var invalidationTraceTableClassSymbol: WeakReference<IrClassSymbol>? = null

private fun irInvalidationTraceTableProp(context: IrPluginContext, currentFile: IrFile): IrProperty {
  val fileName = currentFile.fileEntry.name.split('/').last()
  val shortName = PackagePartClassUtils.getFilePartShortName(fileName)
  val propName = Name.identifier("ComposableInvalidationTraceTableImpl\$$shortName")

  val tableSymbol = invalidationTraceTableClassSymbol?.get() ?: (
    context.referenceClass(ClassId.topLevel(COMPOSABLE_INVALIDATION_TRACE_TABLE_FQN))!!
      .also { symbol -> invalidationTraceTableClassSymbol = WeakReference(symbol) }
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
      type = tableSymbol.defaultType
      visibility = DescriptorVisibilities.PRIVATE
    }.also { field ->
      field.parent = currentFile
      field.correspondingPropertySymbol = prop.symbol
      field.initializer = context.irFactory.createExpressionBody(
        startOffset = SYNTHETIC_OFFSET,
        endOffset = SYNTHETIC_OFFSET,
        expression = IrConstructorCallImpl.fromSymbolOwner(
          type = tableSymbol.defaultType,
          constructorSymbol = tableSymbol.constructors.single(),
        ),
      )
    }
    prop.addGetter {
      returnType = tableSymbol.defaultType
      visibility = DescriptorVisibilities.PRIVATE
      origin = IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR
    }.also { getter ->
      getter.body = DeclarationIrBuilder(context, getter.symbol).irBlockBody {
        +irReturn(irGetField(receiver = null, field = prop.backingField!!))
      }
    }
  }
}
