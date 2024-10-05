// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("unused")

package land.sungbin.composeinvestigator.compiler._source.frontend.traceTableApiUsage

import androidx.compose.runtime.Composable
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracer

private val t = currentComposableInvalidationTracer
private val a = Unit.run {
  t.currentComposableName
  t.currentComposableKeyName
}

@Composable private fun C() {}
