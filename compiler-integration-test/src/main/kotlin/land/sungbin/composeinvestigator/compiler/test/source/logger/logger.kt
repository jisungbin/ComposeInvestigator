/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.source.logger

import land.sungbin.composeinvestigator.runtime.ComposableInvalidationLogger
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationType
import land.sungbin.composeinvestigator.runtime.StateChangedListener
import land.sungbin.composeinvestigator.runtime.affect.AffectedComposable

data class StateNameValue(val name: String, val previousValue: Any?, val newValue: Any?)

val invalidationLog = mutableMapOf<AffectedComposable, MutableList<ComposableInvalidationType>>()
val stateChangeLog = mutableMapOf<AffectedComposable, MutableList<StateNameValue>>()

val invalidationLogger = ComposableInvalidationLogger { composable, type ->
  invalidationLog.getOrPut(composable, ::mutableListOf).add(type)
}

val stateChangeLogger = StateChangedListener { composable, name, previousValue, newValue ->
  val stateLog = StateNameValue(name = name, previousValue = previousValue, newValue = newValue)
  stateChangeLog.getOrPut(composable, ::mutableListOf).add(stateLog)
}

fun findInvalidationLog(composableName: String): List<ComposableInvalidationType> =
  invalidationLog.filterKeys { composable -> composable.name == composableName }.values.flatten()

fun findStateChangeLog(composableName: String): List<StateNameValue> =
  stateChangeLog.filterKeys { composable -> composable.name == composableName }.values.flatten()

fun clearInvalidationLog() {
  invalidationLog.clear()
}

fun clearStateChangeLog() {
  stateChangeLog.clear()
}
