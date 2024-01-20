/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.source.table.callback

import land.sungbin.composeinvestigator.runtime.ComposableInvalidationType
import land.sungbin.composeinvestigator.runtime.affect.AffectedComposable

val invalidationListensViaEffects = mutableMapOf<AffectedComposable, MutableList<ComposableInvalidationType>>()
val invalidationListensViaManualRegister = mutableMapOf<AffectedComposable, MutableList<ComposableInvalidationType>>()

fun findInvalidationListensViaEffects(composableName: String): List<ComposableInvalidationType> =
  invalidationListensViaEffects.filterKeys { composable -> composable.name == composableName }.values.flatten()

fun findInvalidationListensViaManualRegister(composableName: String): List<ComposableInvalidationType> =
  invalidationListensViaManualRegister.filterKeys { composable -> composable.name == composableName }.values.flatten()

fun clearInvalidationListensViaEffectsLog() {
  invalidationListensViaEffects.clear()
}

fun clearInvalidationListensViaManualRegisterLog() {
  invalidationListensViaManualRegister.clear()
}
