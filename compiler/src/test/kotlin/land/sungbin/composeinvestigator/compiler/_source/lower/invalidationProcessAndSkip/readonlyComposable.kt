// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("unused")

package land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

private fun use(any: Any) = any.hashCode()

@Composable @ReadOnlyComposable
private fun blockComposable(any: Any, any2: Any) {
  use(any)
  use(any2)
}

@Composable @ReadOnlyComposable
private fun blockStableComposable(any: Int, any2: Int) {
  use(any)
  use(any2)
}

@Composable @ReadOnlyComposable
private fun expressionComposable(any: Any, any2: Any) =
  use(any) + use(any2)
