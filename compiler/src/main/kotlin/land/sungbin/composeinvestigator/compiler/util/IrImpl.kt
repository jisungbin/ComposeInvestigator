/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("FunctionName")

package land.sungbin.composeinvestigator.compiler.util

import org.jetbrains.kotlin.ir.IrElementBase
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.expressions.IrStatementContainer
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

internal fun IrStatementContainerImpl(
  startOffset: Int = UNDEFINED_OFFSET,
  endOffset: Int = UNDEFINED_OFFSET,
  statements: List<IrStatement>,
): IrStatementContainer = object : IrStatementContainer, IrElementBase() {
  override val startOffset = startOffset
  override val endOffset = endOffset
  override val statements = statements.toMutableList()
  override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D) = visitor.visitElement(this, data)
}
