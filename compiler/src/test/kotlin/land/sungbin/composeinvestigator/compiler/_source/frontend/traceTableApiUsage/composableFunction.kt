// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("unused")

package land.sungbin.composeinvestigator.compiler._source.frontend.traceTableApiUsage

import androidx.compose.runtime.Composable
import land.sungbin.composeinvestigator.runtime.ComposableName
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracer

@Composable private fun composableFunction() {
  val t = currentComposableInvalidationTracer
  t.currentComposableName
  t.currentComposableName = ComposableName("")
  currentComposableInvalidationTracer.currentComposableKeyName
}
