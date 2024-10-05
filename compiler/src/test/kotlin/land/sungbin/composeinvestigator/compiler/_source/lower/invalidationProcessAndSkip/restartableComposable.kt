// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("unused")

package land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip

import androidx.compose.runtime.Composable

private fun use(any: Any) = any.hashCode()

@Composable private fun blockComposable(any: Any, any2: Any) {
  use(any)
  use(any2)
}

@Composable private fun blockStableComposable(any: Int, any2: Int) {
  use(any)
  use(any2)
}

@Composable private fun expressionComposable(any: Any, any2: Any) =
  use(any) + use(any2)
