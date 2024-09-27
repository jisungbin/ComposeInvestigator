/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("unused")

package land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import kotlin.random.Random

private fun use(any: Any) = any.hashCode()

@Composable @NonRestartableComposable
private fun blockComposable(any: Any, any2: Any) {
  when (Random.nextBoolean()) {
    true -> use(any)
    else -> use(any2)
  }
}

@Composable @NonRestartableComposable
private fun blockStableComposable(any: Int, any2: Int) {
  when (Random.nextBoolean()) {
    true -> use(any)
    else -> use(any2)
  }
}

@Composable @NonRestartableComposable
private fun expressionComposable(any: Any, any2: Any) =
  when (Random.nextBoolean()) {
    true -> use(any)
    else -> use(any2)
  }
