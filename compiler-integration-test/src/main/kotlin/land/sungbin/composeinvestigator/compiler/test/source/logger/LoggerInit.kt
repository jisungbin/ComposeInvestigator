/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.source.logger

import land.sungbin.composeinvestigator.runtime.ComposableInvalidationLogger
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationType
import land.sungbin.composeinvestigator.runtime.affect.AffectedComposable

val invalidationLog = mutableMapOf<AffectedComposable, MutableList<ComposableInvalidationType>>()

fun clearInvalidationLog() {
  invalidationLog.clear()
}

fun findInvalidationLog(composableName: String): List<ComposableInvalidationType> =
  invalidationLog.filterKeys { composable -> composable.name == composableName }.values.flatten()

@Suppress("unused")
@ComposableInvalidationLogger
fun invalidationLogger(composable: AffectedComposable, type: ComposableInvalidationType) {
  invalidationLog.getOrPut(composable, ::mutableListOf).add(type)
}
