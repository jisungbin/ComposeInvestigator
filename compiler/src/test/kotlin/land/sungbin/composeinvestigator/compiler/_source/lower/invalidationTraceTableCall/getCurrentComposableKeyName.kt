/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("unused")

package land.sungbin.composeinvestigator.compiler._source.lower.invalidationTraceTableCall

import androidx.compose.runtime.Composable
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracer

private fun getCurrentComposableKeyNameWithinRegularFunction() {
  currentComposableInvalidationTracer.currentComposableKeyName
}

@Composable private fun getCurrentComposableKeyNameWithinComposableFunction() {
  currentComposableInvalidationTracer.currentComposableKeyName

  @Composable fun nested() {
    currentComposableInvalidationTracer.currentComposableKeyName
  }
}
