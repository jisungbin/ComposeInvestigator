// Copyright 2025 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

import org.jetbrains.kotlin.test.directives.model.SimpleDirectivesContainer

object ComposeInvestigatorDirectives : SimpleDirectivesContainer() {
  val INVESTIGATOR_FEATURE_FLAG by enumDirective<FeatureFlag>("[ComposeInvestigator] Feature flags to enable. If omitted, all flags are enabled.")
  val COMPOSE_FEATURE_FLAG by enumDirective<ComposeFeatureFlag>("[Compose] Feature flags to enable")
}

@Suppress("unused")
enum class ComposeFeatureFlag {
  LiveLiterals,
  StrongSkipping,
  IntrinsicRemember,
  OptimizeNonSkippingGroups,
  PausableComposition,
}
