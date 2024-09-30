/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("unused")

package land.sungbin.composeinvestigator.compiler._source.frontend.traceTableApiUsage

import land.sungbin.composeinvestigator.runtime.ComposableInvalidationTraceTable
import land.sungbin.composeinvestigator.runtime.ComposableName
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracer

private fun normalLambda() {
  val t = currentComposableInvalidationTracer
  l(t) {
    currentComposableName
    it.currentComposableKeyName
    currentComposableInvalidationTracer.currentComposableName = ComposableName("")
  }
}

private fun l(
  t: ComposableInvalidationTraceTable,
  b: ComposableInvalidationTraceTable.(ComposableInvalidationTraceTable) -> Unit,
) = Unit
