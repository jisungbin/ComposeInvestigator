/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.struct

import land.sungbin.composeinvestigator.compiler.AFFECTED_COMPOSABLE_FQN
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.utils.addToStdlib.cast

public class IrAffectedComposable(context: IrPluginContext) {
  private val irAffectedComposable = context.referenceClass(ClassId.topLevel(AFFECTED_COMPOSABLE_FQN))!!

  public fun irAffectedComposable(
    composableName: IrConst<String>,
    packageName: IrConst<String>,
    filePath: IrConst<String>,
    startLine: IrConst<Int>,
    startColumn: IrConst<Int>,
  ): IrConstructorCallImpl = IrConstructorCallImpl.fromSymbolOwner(
    type = irAffectedComposable.defaultType,
    constructorSymbol = irAffectedComposable.constructors.single(),
  ).apply {
    putValueArgument(0, composableName)
    putValueArgument(1, packageName)
    putValueArgument(2, filePath)
    putValueArgument(3, startLine)
    putValueArgument(4, startColumn)
  }

  public fun getComposableName(target: IrConstructorCall): IrConst<String> = target.getValueArgument(0)!!.cast()

  public fun copyWith(
    target: IrConstructorCall,
    composableName: IrConst<String>,
  ): IrConstructorCallImpl = IrConstructorCallImpl.fromSymbolOwner(
    type = irAffectedComposable.defaultType,
    constructorSymbol = irAffectedComposable.constructors.single(),
  ).apply {
    putValueArgument(0, composableName)
    putValueArgument(1, target.getValueArgument(1)!!)
    putValueArgument(2, target.getValueArgument(2)!!)
    putValueArgument(3, target.getValueArgument(3)!!)
    putValueArgument(4, target.getValueArgument(4)!!)
  }
}
