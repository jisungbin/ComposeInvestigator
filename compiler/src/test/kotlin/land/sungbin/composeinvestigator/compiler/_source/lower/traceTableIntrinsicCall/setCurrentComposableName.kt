// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("unused", "UnusedVariable")

package land.sungbin.composeinvestigator.compiler._source.lower.traceTableIntrinsicCall

import androidx.compose.runtime.Composable
import land.sungbin.composeinvestigator.runtime.ComposableName
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracer

@Composable private fun setCurrentComposableName() {
  currentComposableInvalidationTracer.currentComposableName = ComposableName("AA!")

  @Composable fun nested() {
    currentComposableInvalidationTracer.currentComposableName = ComposableName("BB!")
  }
}

@Composable private fun inFunctionDefaultArgument(
  content: @Composable () -> Unit = {
    currentComposableInvalidationTracer.currentComposableName = ComposableName("CC!")
  },
) = Unit
