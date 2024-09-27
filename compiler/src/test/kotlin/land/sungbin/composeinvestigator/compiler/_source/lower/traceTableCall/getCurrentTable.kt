/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("unused", "UnusedVariable")

package land.sungbin.composeinvestigator.compiler._source.lower.traceTableCall

import androidx.compose.runtime.Composable
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationTraceTable
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracer

@Composable private fun getCurrentTable() {
  currentComposableInvalidationTracer
  val inVariable = currentComposableInvalidationTracer

  inFunctionDefaultArgument()

  @Composable fun nested() {
    currentComposableInvalidationTracer
  }
}

@Composable private fun inFunctionDefaultArgument(
  table: ComposableInvalidationTraceTable = currentComposableInvalidationTracer,
) = Unit
