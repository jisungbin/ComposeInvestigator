/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.source.logger

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentRecomposeScope
import land.sungbin.composeinvestigator.runtime.ComposableName
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracker

val invalidationSkippedFileTable = currentComposableInvalidationTracker

@Composable
fun InvalidationSkippedRoot() {
  val recomposeScope = currentRecomposeScope

  Button(onClick = recomposeScope::invalidate) {}
  InvalidationSkippedChild()
}

@Composable
private fun InvalidationSkippedChild() {
  Text(text = "Child")
}

@Composable
fun InvalidationSkippedRoot_CustomName() {
  currentComposableInvalidationTracker.currentComposableName = ComposableName("InvalidationSkippedRoot_custom_name")
  val recomposeScope = currentRecomposeScope

  Button(onClick = recomposeScope::invalidate) {}
  InvalidationSkippedChild_CustomName()
}

@Composable
private fun InvalidationSkippedChild_CustomName() {
  currentComposableInvalidationTracker.currentComposableName = ComposableName("InvalidationSkippedChild_custom_name")
  Text(text = "Child")
}
