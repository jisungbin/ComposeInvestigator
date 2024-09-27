/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("unused")

package land.sungbin.composeinvestigator.compiler._source.lower.invalidationProcessAndSkip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key

private fun use(any: Any) = any.hashCode()

@Composable private fun blockComposable(any: Any, any2: Any) {
  key(any, any2) {
    use(any)
    use(any2)
  }
}

@Composable private fun blockStableComposable(any: Int, any2: Int) {
  key(any, any2) {
    use(any)
    use(any2)
  }
}

@Composable private fun expressionComposable(any: Any, any2: Any) =
  key(any, any2) {
    use(any) + use(any2)
  }
