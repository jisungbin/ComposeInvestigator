/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.internal.callstack

import land.sungbin.composeinvestigator.compiler.util.VerboseLogger
import land.sungbin.composeinvestigator.compiler.util.irString
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.name.Name

@Suppress("unused")
internal class ComposableCallstackTransformer(
  private val context: IrPluginContext,
  private val logger: VerboseLogger,
) : AbstractComposableCallstackLower(context, logger), IrPluginContext by context {
  override fun transformComposableCall(
    parent: Name,
    expression: IrCall,
  ): IrExpression = expression.wrapTryFinally(
    tryResult = expression,
    finallyBlock = irString(parent.asString()),
  ).also {
    logger("[ComposableCall] parent: ${parent.asString()}, expression: ${expression.render()}")
    // logger("[ComposableCall] transformed dump: ${transformed.dump()}")
    // logger("[ComposableCall] transformed dumpKotlinLike: ${transformed.dumpKotlinLike()}")
  }
}
