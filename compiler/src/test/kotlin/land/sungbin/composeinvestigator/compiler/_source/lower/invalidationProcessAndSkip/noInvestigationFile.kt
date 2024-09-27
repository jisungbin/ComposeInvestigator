/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("unused")
@file:NoInvestigation

package land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip

import androidx.compose.runtime.Composable
import land.sungbin.composeinvestigator.runtime.NoInvestigation

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
