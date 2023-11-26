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
import land.sungbin.composeinvestigator.runtime.AffectedComposable
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationEffect
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationListener
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationType

val invalidationListensViaEffect = mutableMapOf<AffectedComposable, MutableList<ComposableInvalidationType>>()

fun findInvalidationListensViaEffect(composableName: String): List<ComposableInvalidationType> =
  invalidationListensViaEffect.filterKeys { composable -> composable.name == composableName }.values.flatten()

@Composable
fun EffectListener_InvalidationSkippedRoot() {
  val recomposeScope = currentRecomposeScope

  ComposableInvalidationEffect {
    ComposableInvalidationListener { composable, type ->
      invalidationListensViaEffect.getOrPut(composable, ::mutableListOf).add(type)
    }
  }

  Button(onClick = { recomposeScope.invalidate() }) {}
  EffectListener_InvalidationSkippedChild()
}

@Composable
private fun EffectListener_InvalidationSkippedChild() {
  ComposableInvalidationEffect {
    ComposableInvalidationListener { composable, type ->
      invalidationListensViaEffect.getOrPut(composable, ::mutableListOf).add(type)
    }
  }

  Text(text = "Child")
}