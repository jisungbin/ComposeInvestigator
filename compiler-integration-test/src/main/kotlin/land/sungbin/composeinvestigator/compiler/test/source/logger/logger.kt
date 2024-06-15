/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.source.logger

import land.sungbin.composeinvestigator.runtime.ComposableInvalidationLogger
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationType
import land.sungbin.composeinvestigator.runtime.affect.AffectedComposable

val invalidationLog = mutableMapOf<AffectedComposable, MutableList<ComposableInvalidationType>>()

val invalidationLogger = ComposableInvalidationLogger { composable, type ->
  invalidationLog.getOrPut(composable, ::mutableListOf).add(type)
}

fun findInvalidationLog(composableName: String): List<ComposableInvalidationType> =
  invalidationLog
    .filterKeys { composable -> composable.name == composableName }
    .values.flatten()

fun clearInvalidationLog() {
  invalidationLog.clear()
}
