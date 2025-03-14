// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

public enum class FeatureFlag(public val phase: Int) {
  InvalidationProcessTracing(0),
  InvalidationSkipTracing(1),
  ComposeInvestigatorIntrinsicCall(0),
  StateInitializerTracking(0),
}
