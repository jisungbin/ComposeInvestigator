/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.internal.tracker.affect

import land.sungbin.composeinvestigator.compiler.internal.AFFECTED_COMPOSABLE_FQN
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.name.ClassId

public object IrAffectedComposable {
  private var affectedComposableSymbol: IrClassSymbol? = null

  public fun init(context: IrPluginContext) {
    affectedComposableSymbol = context.referenceClass(ClassId.topLevel(AFFECTED_COMPOSABLE_FQN))!!
  }

  public fun irAffectedComposable(
    composableName: IrExpression,
    packageName: IrExpression,
    filePath: IrExpression,
    startLine: IrExpression,
    startColumn: IrExpression,
  ): IrConstructorCallImpl = IrConstructorCallImpl.fromSymbolOwner(
    type = affectedComposableSymbol!!.defaultType,
    constructorSymbol = affectedComposableSymbol!!.constructors.single(),
  ).apply {
    putValueArgument(0, composableName)
    putValueArgument(1, packageName)
    putValueArgument(2, filePath)
    putValueArgument(3, startLine)
    putValueArgument(4, startColumn)
  }
}
