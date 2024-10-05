// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
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
