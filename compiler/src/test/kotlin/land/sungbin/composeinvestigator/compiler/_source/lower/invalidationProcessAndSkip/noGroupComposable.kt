// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("unused")

package land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExplicitGroupsComposable

private fun use(any: Any) = any.hashCode()

@Composable @ExplicitGroupsComposable
private fun blockComposable(any: Any, any2: Any) {
  use(any)
  use(any2)
}

@Composable @ExplicitGroupsComposable
private fun blockStableComposable(any: Int, any2: Int) {
  use(any)
  use(any2)
}

@Composable @ExplicitGroupsComposable
private fun expressionComposable(any: Any, any2: Any) =
  use(any) + use(any2)
