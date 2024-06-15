/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.struct

import land.sungbin.composeinvestigator.compiler.AFFECTED_FIELD_FQN
import land.sungbin.composeinvestigator.compiler.AffectedField_VALUE_PARAMETER
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.name.ClassId

public class IrAffectedField(context: IrPluginContext) {
  public val irAffectedField: IrClassSymbol = context.referenceClass(ClassId.topLevel(AFFECTED_FIELD_FQN))!!
  private val valueParameterSymbol = irAffectedField.owner.sealedSubclasses.single { clz ->
    clz.owner.name == AffectedField_VALUE_PARAMETER
  }

  public fun irValueParameter(
    name: IrExpression,
    typeName: IrExpression,
    valueString: IrExpression,
    valueHashCode: IrExpression,
    stability: IrExpression,
  ): IrConstructorCall = IrConstructorCallImpl.fromSymbolOwner(
    type = valueParameterSymbol.defaultType,
    constructorSymbol = valueParameterSymbol.constructors.single(),
  ).apply {
    putValueArgument(0, name)
    putValueArgument(1, typeName)
    putValueArgument(2, valueString)
    putValueArgument(3, valueHashCode)
    putValueArgument(4, stability)
  }
}
