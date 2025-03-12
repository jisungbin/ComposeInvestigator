// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.struct

import java.lang.ref.WeakReference
import land.sungbin.composeinvestigator.compiler.InvestigatorClassIds
import land.sungbin.composeinvestigator.compiler.InvestigatorNames
import land.sungbin.composeinvestigator.compiler.lower.irError
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.jvm.functionByName
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
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrValueAccessExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.fromSymbolOwner
import org.jetbrains.kotlin.ir.irAttribute
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.load.kotlin.PackagePartClassUtils
import org.jetbrains.kotlin.name.Name

/**
 * Helper class to make the `ComposeInvestigator` class easier to handle in IR.
 *
 * @param property The property where `ComposeInvestigator` is instantiated. It can be
 * created with the [irComposeInvestigatorProperty] function.
 *
 * @constructor Use the [IrComposeInvestigator.create]
 */
public class IrComposeInvestigator private constructor(internal val property: IrProperty) {
  private lateinit var getComposableNameSymbol: IrSimpleFunctionSymbol
  private lateinit var registerStateObjectSymbol: IrSimpleFunctionSymbol
  private lateinit var computeInvalidationReasonSymbol: IrSimpleFunctionSymbol

  /** Returns an [IrCall] that invokes the getter of [property]. */
  public fun irPropertyGetter(
    startOffset: Int = UNDEFINED_OFFSET,
    endOffset: Int = UNDEFINED_OFFSET,
  ): IrCall =
    IrCallImpl.fromSymbolOwner(
      startOffset = startOffset,
      endOffset = endOffset,
      symbol = property.getter!!.symbol,
    ).apply {
      origin = IrStatementOrigin.GET_PROPERTY
    }

  public fun irGetComposableName(
    compoundKey: IrCall, // Expected to get irCompoundKeyHash
    default: IrConst,
  ): IrCall =
    IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = getComposableNameSymbol,
    ).apply {
      dispatchReceiver = irPropertyGetter()
      putValueArgument(0, compoundKey)
      putValueArgument(1, default)
    }

  /** Returns an [IrCall] that invokes `ComposeInvestigator#registerStateObject`. */
  public fun irRegisterStateObject(
    value: IrExpression,
    name: IrConst,
  ): IrCall =
    IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = registerStateObjectSymbol,
    ).apply {
      dispatchReceiver = irPropertyGetter()
      type = value.type
      putTypeArgument(0, value.type)
      putValueArgument(0, value)
      putValueArgument(1, name)
    }

  /** Returns an [IrCall] that invokes `ComposableInvalidationTraceTable#computeInvalidationReason`. */
  public fun irComputeInvalidationReason(
    compoundKey: IrCall, // Expected to get irCompoundKeyHash
    arguments: IrValueAccessExpression,
  ): IrCall =
    IrCallImpl.fromSymbolOwner(
      startOffset = UNDEFINED_OFFSET,
      endOffset = UNDEFINED_OFFSET,
      symbol = computeInvalidationReasonSymbol,
    ).apply {
      dispatchReceiver = irPropertyGetter()
      putValueArgument(0, compoundKey)
      putValueArgument(1, arguments)
    }

  public companion object {
    /** Creates a new instance of `ComposeInvestigator` in the [given file][targetFile]. */
    public fun create(context: IrPluginContext, targetFile: IrFile): IrComposeInvestigator =
      IrComposeInvestigator(irComposeInvestigatorProperty(context, targetFile)).apply {
        val symbol = property.backingField!!.type.classOrFail
        getComposableNameSymbol = symbol.functionByName(InvestigatorNames.getComposableName.asString())
        registerStateObjectSymbol = symbol.functionByName(InvestigatorNames.registerStateObject.asString())
        computeInvalidationReasonSymbol = symbol.functionByName(InvestigatorNames.computeInvalidationReason.asString())
      }
  }
}

public var IrFile.irComposeInvestigator: IrComposeInvestigator? by irAttribute(followAttributeOwner = false)

public fun IrFile.irComposeInvestigator(): IrComposeInvestigator =
  this.irComposeInvestigator ?: irError("ComposeInvestigator is not instantiated")

@Volatile private var composeInvestigatorSymbolCache: WeakReference<IrClassSymbol>? = null

/**
 * Returns a [IrProperty] that initializes `ComposableInvalidationTraceTable`.
 *
 * @param currentFile The parent element of the property to be returned.
 */
private fun irComposeInvestigatorProperty(context: IrPluginContext, currentFile: IrFile): IrProperty {
  val fileName = currentFile.fileEntry.name.substringAfterLast('/')
  val shortName = PackagePartClassUtils.getFilePartShortName(fileName)
  val propName = Name.identifier("ComposeInvestigatorImpl$$shortName")

  val targetSymbol = composeInvestigatorSymbolCache?.get() ?: (
    context.referenceClass(InvestigatorClassIds.ComposeInvestigator)!!
      .also { symbol -> composeInvestigatorSymbolCache = WeakReference(symbol) }
    )

  return context.irFactory.buildProperty {
    visibility = DescriptorVisibilities.PRIVATE
    name = propName
  }.also { property ->
    property.parent = currentFile
    property.backingField = context.irFactory.buildField {
      name = propName
      isStatic = true
      isFinal = true
      type = targetSymbol.defaultType
      visibility = DescriptorVisibilities.PRIVATE
    }.also { backing ->
      backing.parent = currentFile
      backing.correspondingPropertySymbol = property.symbol
      backing.initializer = context.irFactory.createExpressionBody(
        startOffset = SYNTHETIC_OFFSET,
        endOffset = SYNTHETIC_OFFSET,
        expression = IrConstructorCallImpl.fromSymbolOwner(
          type = targetSymbol.defaultType,
          constructorSymbol = targetSymbol.constructors.single(),
        ),
      )
    }
    property.addGetter {
      returnType = targetSymbol.defaultType
      visibility = DescriptorVisibilities.PRIVATE
      origin = IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR
    }.also { getter ->
      getter.body = DeclarationIrBuilder(context, getter.symbol).irBlockBody {
        +irReturn(irGetField(receiver = null, field = property.backingField!!))
      }
    }
  }
}
