/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.source.table.callback

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentRecomposeScope
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationEffect
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationListener
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationType
import land.sungbin.composeinvestigator.runtime.affect.AffectedComposable
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracker

val invalidationListensViaEffects = mutableMapOf<AffectedComposable, MutableList<ComposableInvalidationType>>()

fun findInvalidationListensViaEffects(composableName: String): List<ComposableInvalidationType> =
  invalidationListensViaEffects.filterKeys { composable -> composable.name == composableName }.values.flatten()

@Composable
fun Effects_InvalidationSkippedRoot() {
  val recomposeScope = currentRecomposeScope
  val table = currentComposableInvalidationTracker
  val currentKeyName = table.currentComposableKeyName

  ComposableInvalidationEffect(table = table, composableKey = currentKeyName) {
    ComposableInvalidationListener { composable, type ->
      invalidationListensViaEffects.getOrPut(composable, ::mutableListOf).add(type)
    }
  }

  Button(onClick = { recomposeScope.invalidate() }) {}
  Effects_InvalidationSkippedChild()
}

@Composable
private fun Effects_InvalidationSkippedChild() {
  val table = currentComposableInvalidationTracker
  val currentKeyName = table.currentComposableKeyName

  ComposableInvalidationEffect(table = table, composableKey = currentKeyName) {
    ComposableInvalidationListener { composable, type ->
      invalidationListensViaEffects.getOrPut(composable, ::mutableListOf).add(type)
    }
  }

  Text(text = "Child")
}
