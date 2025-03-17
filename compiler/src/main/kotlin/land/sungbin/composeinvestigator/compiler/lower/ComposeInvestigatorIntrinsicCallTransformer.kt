// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.lower

import land.sungbin.composeinvestigator.compiler.InvestigatorCallableIds
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression

public class ComposeInvestigatorIntrinsicCallTransformer(
  context: IrPluginContext,
  messageCollector: MessageCollector,
) : ComposeInvestigatorBaseLower(context, messageCollector) {
  override fun visitCall(expression: IrCall): IrExpression =
    when (expression.symbol.owner.callableIdOrNull) {
      null -> super.visitCall(expression)
      InvestigatorCallableIds.getCurrentComposableName -> {
        if (expression.getValueArgument(0) == null) // compoundKey
          expression.putValueArgument(0, irCompoundKeyHash(irCurrentComposer()))

        if (expression.getValueArgument(1) == null) // default
          expression.putValueArgument(1, irString(allScopes.lastComposable()!!.name.asString()))

        super.visitCall(expression)
      }
      else -> super.visitCall(expression)
    }
}
