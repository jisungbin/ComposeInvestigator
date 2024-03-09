/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.source.table.callback

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentRecomposeScope
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracker

@Composable
fun RegisterListener_InvalidationSkippedRoot() {
  val recomposeScope = currentRecomposeScope
  val tracker = currentComposableInvalidationTracker

  tracker.registerListener(keyName = tracker.currentComposableKeyName) { _, composable, type ->
    invalidationListensViaManualRegister.getOrPut(composable, ::mutableListOf).add(type)
  }

  Button(onClick = recomposeScope::invalidate) {}
  RegisterListener_InvalidationSkippedChild()
}

@Composable
private fun RegisterListener_InvalidationSkippedChild() {
  val tracker = currentComposableInvalidationTracker

  tracker.registerListener(keyName = tracker.currentComposableKeyName) { _, composable, type ->
    invalidationListensViaManualRegister.getOrPut(composable, ::mutableListOf).add(type)
  }

  Text(text = "Child")
}
