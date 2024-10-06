// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.struct

import java.lang.ref.WeakReference
import land.sungbin.composeinvestigator.compiler.COMPOSABLE_INFORMATION_FQN
import land.sungbin.composeinvestigator.compiler.ComposableInformation_WITH_COMPOUND_KEY
import land.sungbin.composeinvestigator.compiler.fromFqName
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.fromSymbolOwner
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.utils.addToStdlib.cast

/** Helper class to make the `ComposableInformation` class easier to handle in IR. */
public class IrComposableInformation(context: IrPluginContext) {
  private val symbol = context.referenceClass(ClassId.topLevel(COMPOSABLE_INFORMATION_FQN))!!

  /** Create a `ComposableInformation` constructor except for the `compoundKey` value. */
  public operator fun invoke(
    name: IrConst,
    packageName: IrConst,
    fileName: IrConst,
  ): IrConstructorCallImpl = IrConstructorCallImpl.fromSymbolOwner(
    type = symbol.defaultType,
    constructorSymbol = symbol.constructors.single(),
  ).apply {
    putValueArgument(0, name)
    putValueArgument(1, packageName)
    putValueArgument(2, fileName)
  }

  /**
   * Copy the `ComposableInformation` constructor call, changing only `name` to the
   * new value.
   *
   * @param target The target `ComposableInformation` constructor call to copy from.
   * @param name The new value for the `name` parameter.
   */
  public fun copyFrom(
    target: IrConstructorCall,
    name: IrConst,
  ): IrConstructorCallImpl = invoke(
    name = name,
    packageName = target.getValueArgument(1)!!.cast(),
    fileName = target.getValueArgument(2)!!.cast(),
  )

  public companion object {
    private var _withCompoundKeySymbol: WeakReference<IrSimpleFunctionSymbol>? = null

    /** Get the `name` value from the `ComposableInformation` constructor call. */
    public fun getName(target: IrConstructorCall): IrConst = target.getValueArgument(0)!!.cast()

    /** Gets the `ComposableInformation#withCompoundKey` symbol. */
    public fun withCompoundKeySymbol(context: IrPluginContext): IrSimpleFunctionSymbol =
      _withCompoundKeySymbol?.get() ?: run {
        val targetFqn = COMPOSABLE_INFORMATION_FQN.child(ComposableInformation_WITH_COMPOUND_KEY)
        context.referenceFunctions(CallableId.fromFqName(targetFqn)).single()
      }
        .also { symbol -> _withCompoundKeySymbol = WeakReference(symbol) }
  }
}
