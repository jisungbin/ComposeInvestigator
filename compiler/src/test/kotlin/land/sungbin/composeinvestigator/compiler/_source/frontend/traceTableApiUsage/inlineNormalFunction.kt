// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("unused", "NOTHING_TO_INLINE")

package land.sungbin.composeinvestigator.compiler._source.frontend.traceTableApiUsage

import land.sungbin.composeinvestigator.runtime.ComposableName
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracer

private inline fun inlineNormalFunction() {
  val t = currentComposableInvalidationTracer
  t.currentComposableName
  t.currentComposableName = ComposableName("")
  currentComposableInvalidationTracer.currentComposableKeyName
}
