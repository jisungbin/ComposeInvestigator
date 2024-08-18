/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

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
private fun expressionComposable(any: Any, any2: Any) =
  use(any) + use(any2)
