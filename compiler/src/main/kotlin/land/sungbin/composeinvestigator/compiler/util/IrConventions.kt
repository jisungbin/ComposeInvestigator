/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.util

import land.sungbin.fastlist.fastForEach
import org.jetbrains.kotlin.ir.expressions.IrStatementContainer
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.dumpKotlinLike

internal fun IrStatementContainer.dump() = buildString {
  appendLine("[")
  statements.fastForEach { statement ->
    appendLine("  ${statement.dump()}")
  }
  appendLine("]")
}

internal fun IrStatementContainer.dumpKotlinLike() = buildString {
  appendLine("{")
  statements.fastForEach { statement ->
    appendLine("  ${statement.dumpKotlinLike()}")
  }
  appendLine("}")
}
