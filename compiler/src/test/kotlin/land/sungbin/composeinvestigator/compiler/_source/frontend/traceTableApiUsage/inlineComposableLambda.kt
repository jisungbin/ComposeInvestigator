// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("unused", "NOTHING_TO_INLINE")

package land.sungbin.composeinvestigator.compiler._source.frontend.traceTableApiUsage

import androidx.compose.runtime.Composable
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationTraceTable
import land.sungbin.composeinvestigator.runtime.ComposableName
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracer

@Composable private inline fun inlineComposableLambda() {
  val t = currentComposableInvalidationTracer
  l(t) {
    currentComposableName
    it.currentComposableKeyName
    currentComposableInvalidationTracer.currentComposableName = ComposableName("")
  }
}

private inline fun l(
  t: ComposableInvalidationTraceTable,
  b: @Composable ComposableInvalidationTraceTable.(ComposableInvalidationTraceTable) -> Unit,
) = Unit
