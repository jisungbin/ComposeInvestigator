// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("unused")

package land.sungbin.composeinvestigator.compiler._source.frontend.traceTableApiUsage

import land.sungbin.composeinvestigator.runtime.ComposableName
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracer

private fun normalFunction() {
  val t = currentComposableInvalidationTracer
  t.currentComposableName
  t.currentComposableName = ComposableName("")
  currentComposableInvalidationTracer.currentComposableKeyName
}
