/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:OptIn(ComposeInvestigatorCompilerApi::class)

package land.sungbin.composeinvestigator.compiler.test.source.table.listener

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentRecomposeScope
import land.sungbin.composeinvestigator.runtime.AffectedComposable
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationType
import land.sungbin.composeinvestigator.runtime.ComposeInvestigatorCompilerApi
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracker

val invalidationListens = mutableMapOf<AffectedComposable, MutableList<ComposableInvalidationType>>()

fun findInvalidationListens(composableName: String): List<ComposableInvalidationType> =
  invalidationListens.filterKeys { composable -> composable.name == composableName }.values.flatten()

@Composable
fun RegisterListener_InvalidationSkippedRoot() {
  val recomposeScope = currentRecomposeScope
  val tracker = currentComposableInvalidationTracker

  tracker.registerListener(keyName = tracker.currentComposableKeyName) { composable, type ->
    invalidationListens.getOrPut(composable, ::mutableListOf).add(type)
  }

  Button(onClick = { recomposeScope.invalidate() }) {}
  RegisterListener_InvalidationSkippedChild()
}

@Composable
private fun RegisterListener_InvalidationSkippedChild() {
  val tracker = currentComposableInvalidationTracker

  tracker.registerListener(keyName = tracker.currentComposableKeyName) { composable, type ->
    invalidationListens.getOrPut(composable, ::mutableListOf).add(type)
  }

  Text(text = "Child")
}