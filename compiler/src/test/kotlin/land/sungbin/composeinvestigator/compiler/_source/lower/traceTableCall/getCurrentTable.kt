// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
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
