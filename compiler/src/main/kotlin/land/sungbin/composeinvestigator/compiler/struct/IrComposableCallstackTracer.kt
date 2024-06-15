/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.struct

import java.lang.ref.WeakReference
import land.sungbin.composeinvestigator.compiler.ITERABLE_TO_LIST_FQN
import land.sungbin.composeinvestigator.compiler.STACK_FQN
import land.sungbin.composeinvestigator.compiler.Stack_POP
import land.sungbin.composeinvestigator.compiler.Stack_PUSH
import land.sungbin.composeinvestigator.compiler.fromFqName
import land.sungbin.composeinvestigator.compiler.origin.ComposableCallstackTracerSyntheticOrigin
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
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.load.kotlin.PackagePartClassUtils
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

public class IrComposableCallstackTracer private constructor(public val prop: IrProperty) {
  private var pushSymbol: IrSimpleFunctionSymbol? = null
  private var popSymbol: IrSimpleFunctionSymbol? = null
  private var toListSymbol: IrSimpleFunctionSymbol? = null

  public fun irCopy(): IrCall =
    IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = toListSymbol!!,
    ).also { fn ->
      fn.extensionReceiver = propGetter()
    }

  public fun irPush(name: IrConst<String>): IrCall =
    IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = pushSymbol!!,
    ).also { fn ->
      fn.dispatchReceiver = propGetter()
    }.apply {
      putValueArgument(0, name)
    }

  public fun irPop(): IrCall =
    IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = popSymbol!!,
    ).also { fn ->
      fn.dispatchReceiver = propGetter()
    }

  public companion object {
    private fun IrComposableCallstackTracer.init(context: IrPluginContext) {
      val owner = prop.backingField!!.type.classOrFail
      pushSymbol = owner.getSimpleFunction(Stack_PUSH.asString())!!
      popSymbol = owner.getSimpleFunction(Stack_POP.asString())!!
      toListSymbol =
        context
          .referenceFunctions(CallableId.fromFqName(ITERABLE_TO_LIST_FQN))
          .single { symbol ->
            symbol.owner.extensionReceiverParameter?.type?.classFqName == context.irBuiltIns.iterableClass.defaultType.classFqName &&
              symbol.owner.valueParameters.isEmpty()
          }
    }

    public fun from(context: IrPluginContext, prop: IrProperty): IrComposableCallstackTracer =
      IrComposableCallstackTracer(prop).apply { init(context) }

    public fun create(context: IrPluginContext, currentFile: IrFile): IrComposableCallstackTracer =
      IrComposableCallstackTracer(irComposableCallstackTracerProp(context, currentFile)).apply { init(context) }
  }
}

public fun IrComposableCallstackTracer.propGetter(
  startOffset: Int = UNDEFINED_OFFSET,
  endOffset: Int = UNDEFINED_OFFSET,
): IrCall = IrCallImpl.fromSymbolOwner(
  startOffset = startOffset,
  endOffset = endOffset,
  symbol = prop.getter!!.symbol,
)

private var stackClassSymbol: WeakReference<IrClassSymbol>? = null

private fun irComposableCallstackTracerProp(context: IrPluginContext, currentFile: IrFile): IrProperty {
  val fileName = currentFile.fileEntry.name.split('/').last()
  val shortName = PackagePartClassUtils.getFilePartShortName(fileName)
  val propName = Name.identifier("ComposableCallstackTracerImpl\$$shortName")

  val superSymbol = stackClassSymbol?.get() ?: (
    context.referenceClass(ClassId.topLevel(STACK_FQN))!!
      .also { symbol -> stackClassSymbol = WeakReference(symbol) }
    )

  return context.irFactory.buildProperty {
    visibility = DescriptorVisibilities.INTERNAL
    name = propName
    origin = ComposableCallstackTracerSyntheticOrigin
  }.also { prop ->
    prop.parent = currentFile
    prop.backingField = context.irFactory.buildField {
      name = propName
      isStatic = true
      isFinal = true
      type = superSymbol.defaultType.classOrFail.typeWith(context.irBuiltIns.stringType)
      visibility = DescriptorVisibilities.INTERNAL
    }.also { field ->
      field.parent = currentFile
      field.correspondingPropertySymbol = prop.symbol
      field.initializer = context.irFactory.createExpressionBody(
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
      visibility = DescriptorVisibilities.INTERNAL
      origin = IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR
    }.also { getter ->
      getter.body = DeclarationIrBuilder(context, getter.symbol).irBlockBody {
        +irReturn(irGetField(null, prop.backingField!!))
      }
    }
  }
}
