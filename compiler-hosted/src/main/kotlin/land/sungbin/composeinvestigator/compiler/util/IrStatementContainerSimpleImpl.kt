/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.util

import land.sungbin.fastlist.fastForEach
import org.jetbrains.kotlin.ir.IrElementBase
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.expressions.IrStatementContainer
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

public class IrStatementContainerSimpleImpl(
  statements: List<IrStatement>,
  override val startOffset: Int = UNDEFINED_OFFSET,
  override val endOffset: Int = UNDEFINED_OFFSET,
) : IrStatementContainer, IrElementBase() {
  override val statements: MutableList<IrStatement> = statements as? MutableList<IrStatement> ?: statements.toMutableList()
  override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D): R = visitor.visitElement(this, data)

  public fun dump(): String = buildString {
    appendLine("[")
    statements.fastForEach { statement ->
      appendLine("  ${statement.dump()}")
    }
    appendLine("]")
  }

  public fun dumpKotlinLike(): String = buildString {
    appendLine("{")
    statements.fastForEach { statement ->
      appendLine("  ${statement.dumpKotlinLike()}")
    }
    appendLine("}")
  }
}
