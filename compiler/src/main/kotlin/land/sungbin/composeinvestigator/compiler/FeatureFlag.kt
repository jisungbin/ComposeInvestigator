// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

/** @param phase 0: [ComposeInvestigatorFirstPhaseExtension], 1: [ComposeInvestigatorLastPhaseExtension] */
@Suppress("RedundantVisibilityModifier") // FIXME false negative
public enum class FeatureFlag(public val phase: Int) {
  InvalidationProcessTracing(0),
  InvalidationSkipTracing(1),
  InvalidationTraceTableIntrinsicCall(0),
  StateInitializerTracking(0),
}
