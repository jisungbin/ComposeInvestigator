/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("unused", "UnusedVariable")

package land.sungbin.composeinvestigator.compiler._source.lower.traceTableCall

import androidx.compose.runtime.Composable
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracer

@Composable private fun getCurrentComposableKeyName() {
  currentComposableInvalidationTracer.currentComposableKeyName
  val inVariable = currentComposableInvalidationTracer

  inFunctionDefaultArgument()

  @Composable fun nested() {
    currentComposableInvalidationTracer.currentComposableKeyName
  }
}

@Composable private fun inFunctionDefaultArgument(
  keyed: String = currentComposableInvalidationTracer.currentComposableKeyName,
) = Unit
