/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

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
