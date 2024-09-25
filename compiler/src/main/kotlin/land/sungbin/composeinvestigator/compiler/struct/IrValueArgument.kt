/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.struct

import land.sungbin.composeinvestigator.compiler.VALUE_ARGUMENT_FQN
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.fromSymbolOwner
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.name.ClassId

public class IrValueArgument(context: IrPluginContext) {
  public val symbol: IrClassSymbol = context.referenceClass(ClassId.topLevel(VALUE_ARGUMENT_FQN))!!

  public operator fun invoke(
    name: IrExpression,
    type: IrExpression,
    valueString: IrExpression,
    valueHashCode: IrExpression,
    stability: IrExpression,
  ): IrConstructorCall = IrConstructorCallImpl.fromSymbolOwner(
    type = symbol.defaultType,
    constructorSymbol = symbol.constructors.single(),
  ).apply {
    putValueArgument(0, name)
    putValueArgument(1, type)
    putValueArgument(2, valueString)
    putValueArgument(3, valueHashCode)
    putValueArgument(4, stability)
  }
}
