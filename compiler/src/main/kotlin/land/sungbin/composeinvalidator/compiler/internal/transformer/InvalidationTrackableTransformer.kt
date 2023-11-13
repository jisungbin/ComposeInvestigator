/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

package land.sungbin.composeinvalidator.compiler.internal.transformer

import land.sungbin.composeinvalidator.compiler.internal.COMPOSABLE_FQN
import land.sungbin.composeinvalidator.compiler.util.VerboseLogger
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

internal class InvalidationTrackableTransformer(private val logger: VerboseLogger) : IrElementTransformerVoid() {
  override fun visitFunction(declaration: IrFunction): IrStatement {
    if (!declaration.hasAnnotation(COMPOSABLE_FQN)) return super.visitFunction(declaration)
    logger("visitFunction: ${declaration.name.asString()}")
    // declaration.body?.transformChildrenVoid()
    logger(declaration.body?.dumpKotlinLike())
    return super.visitFunction(declaration)
  }

  // override fun visitBody(body: IrBody): IrBody {
  //   for (statement in body.statements) {
  //     logger(statement.dumpKotlinLike())
  //   }
  //   return super.visitBody(body)
  // }
}
