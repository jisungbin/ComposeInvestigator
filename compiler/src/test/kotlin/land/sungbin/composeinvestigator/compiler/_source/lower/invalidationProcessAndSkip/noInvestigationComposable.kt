// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("unused")

package land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip

import androidx.compose.runtime.Composable
import land.sungbin.composeinvestigator.runtime.NoInvestigation

private fun use(any: Any) = any.hashCode()

@Composable @NoInvestigation
private fun noInvestigationBlockComposable(any: Any, any2: Any) {
  use(any)
  use(any2)
}

@Composable @NoInvestigation
private fun noInvestigationBlockStableComposable(any: Int, any2: Int) {
  use(any)
  use(any2)
}

@Composable @NoInvestigation
private fun noInvestigationExpressionComposable(any: Any, any2: Any) =
  use(any) + use(any2)
