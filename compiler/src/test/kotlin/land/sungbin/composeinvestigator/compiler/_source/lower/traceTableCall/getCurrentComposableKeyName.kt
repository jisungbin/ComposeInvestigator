// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("unused", "UnusedVariable")

package land.sungbin.composeinvestigator.compiler._source.lower.traceTableCall

import androidx.compose.runtime.Composable
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracer

@Composable private fun getCurrentComposableKeyName() {
  currentComposableInvalidationTracer.currentComposableKeyName
  val inVariable = currentComposableInvalidationTracer.currentComposableKeyName

  inFunctionDefaultArgument()

  @Composable fun nested() {
    currentComposableInvalidationTracer.currentComposableKeyName
  }
}

@Composable private fun inFunctionDefaultArgument(
  keyed: String = currentComposableInvalidationTracer.currentComposableKeyName,
) = Unit
