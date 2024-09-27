/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("unused", "UnusedVariable")

package land.sungbin.composeinvestigator.compiler._source.lower.traceTableCall

import androidx.compose.runtime.Composable
import land.sungbin.composeinvestigator.runtime.ComposableName
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracer

@Composable private fun getCurrentComposableName() {
  currentComposableInvalidationTracer.currentComposableName
  val inVariable = currentComposableInvalidationTracer.currentComposableName

  inFunctionDefaultArgument()

  @Composable fun nested() {
    currentComposableInvalidationTracer.currentComposableName
  }
}

@Composable private fun inFunctionDefaultArgument(
  name: ComposableName = currentComposableInvalidationTracer.currentComposableName,
) = Unit
