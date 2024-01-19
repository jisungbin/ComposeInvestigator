/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.invalidation.internal.tracker.affect

import land.sungbin.composeinvestigator.compiler.base.AFFECTED_FIELD_FQN
import land.sungbin.composeinvestigator.compiler.base.AffectedField_VALUE_PARAMETER
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.name.ClassId

public object IrAffectedField {
  private var affectedFieldSymbol: IrClassSymbol? = null
  private var valueParameterSymbol: IrClassSymbol? = null

  public val irAffectedField: IrClassSymbol get() = affectedFieldSymbol!!

  public fun init(context: IrPluginContext) {
    affectedFieldSymbol = context.referenceClass(ClassId.topLevel(AFFECTED_FIELD_FQN))!!
    valueParameterSymbol = affectedFieldSymbol!!.owner.sealedSubclasses.single { clz ->
      clz.owner.name == AffectedField_VALUE_PARAMETER
    }
  }

  public fun irValueParameter(
    name: IrExpression,
    typeFqName: IrExpression,
    valueString: IrExpression,
    valueHashCode: IrExpression,
    stability: IrExpression,
  ): IrConstructorCall = IrConstructorCallImpl.fromSymbolOwner(
    type = valueParameterSymbol!!.defaultType,
    constructorSymbol = valueParameterSymbol!!.constructors.single(),
  ).apply {
    putValueArgument(0, name)
    putValueArgument(1, typeFqName)
    putValueArgument(2, valueString)
    putValueArgument(3, valueHashCode)
    putValueArgument(4, stability)
  }
}
