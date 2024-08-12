/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.struct

import land.sungbin.composeinvestigator.compiler.VALUE_PARAMETER_FQN
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.utils.addToStdlib.cast

public class IrAffectedComposable(context: IrPluginContext) {
  private val irAffectedComposable = context.referenceClass(ClassId.topLevel(VALUE_PARAMETER_FQN))!!

  public fun irAffectedComposable(
    name: IrConst<String>,
    pkg: IrConst<String>,
    filename: IrConst<String>,
  ): IrConstructorCallImpl = IrConstructorCallImpl.fromSymbolOwner(
    type = irAffectedComposable.defaultType,
    constructorSymbol = irAffectedComposable.constructors.single(),
  ).apply {
    putValueArgument(0, name)
    putValueArgument(1, pkg)
    putValueArgument(2, filename)
  }

  public fun getName(target: IrConstructorCall): IrConst<String> = target.getValueArgument(0)!!.cast()

  public fun copyWith(
    target: IrConstructorCall,
    name: IrConst<String>,
  ): IrConstructorCallImpl = IrConstructorCallImpl.fromSymbolOwner(
    type = irAffectedComposable.defaultType,
    constructorSymbol = irAffectedComposable.constructors.single(),
  ).apply {
    putValueArgument(0, name)
    putValueArgument(1, target.getValueArgument(1)!!)
    putValueArgument(2, target.getValueArgument(2)!!)
  }
}
