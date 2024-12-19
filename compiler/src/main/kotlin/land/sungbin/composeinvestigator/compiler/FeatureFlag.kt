// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

import land.sungbin.composeinvestigator.compiler.lower.InvalidationProcessTracingFirstTransformer
import land.sungbin.composeinvestigator.compiler.lower.InvalidationSkipTracingLastTransformer
import land.sungbin.composeinvestigator.compiler.lower.InvalidationTraceTableIntrinsicCallTransformer
import land.sungbin.composeinvestigator.compiler.lower.StateInitializerFirstTransformer

/**
 * Defines the feature that ComposeInvestigator will operate on.
 *
 * @property InvalidationProcessTracing [InvalidationProcessTracingFirstTransformer]
 * @property InvalidationSkipTracing [InvalidationSkipTracingLastTransformer]
 * @property InvalidationTraceTableIntrinsicCall [InvalidationTraceTableIntrinsicCallTransformer]
 * @property StateInitializerTracking [StateInitializerFirstTransformer]
 *
 * @param phase 0: [ComposeInvestigatorFirstPhaseExtension], 1: [ComposeInvestigatorLastPhaseExtension]
 */
public enum class FeatureFlag(public val phase: Int) {
  InvalidationProcessTracing(0),
  InvalidationSkipTracing(1),
  InvalidationTraceTableIntrinsicCall(0),
  StateInitializerTracking(0),
}
