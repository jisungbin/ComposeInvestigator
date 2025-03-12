// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.lower

import land.sungbin.composeinvestigator.compiler.InvestigatorCallableIds
import land.sungbin.composeinvestigator.compiler.struct.irComposeInvestigator
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.util.callableId

public class ComposeInvestigatorIntrinsicCallTransformer(
  context: IrPluginContext,
  messageCollector: MessageCollector,
) : ComposeInvestigatorBaseLower(context, messageCollector) {
  override fun visitCall(expression: IrCall): IrExpression =
    when (expression.symbol.owner.callableId) {
      InvestigatorCallableIds.currentComposeInvestigator -> {
        currentFile.irComposeInvestigator().irPropertyGetter(expression.startOffset)
      }
      InvestigatorCallableIds.setComposableName -> {
        if (expression.getValueArgument(1) == null) // compoundKey
          expression.putValueArgument(1, irCompoundKeyHash(irCurrentComposer()))

        super.visitCall(expression)
      }
      InvestigatorCallableIds.getComposableName -> {
        if (expression.getValueArgument(0) == null) // compoundKey
          expression.putValueArgument(0, irCompoundKeyHash(irCurrentComposer()))

        if (expression.getValueArgument(1) == null) // default
          expression.putValueArgument(1, irString(allScopes.lastComposable()!!.name.asString()))

        super.visitCall(expression)
      }
      else -> super.visitCall(expression)
    }
}
