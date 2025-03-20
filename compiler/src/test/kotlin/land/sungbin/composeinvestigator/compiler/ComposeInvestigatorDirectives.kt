// Copyright 2025 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

import org.jetbrains.kotlin.test.directives.model.SimpleDirectivesContainer

object ComposeInvestigatorDirectives : SimpleDirectivesContainer() {
  val INVESTIGATOR_FEATURES by
    valueDirective("Feature flags of ComposeInvestigator compiler to enable. If omitted, all flags are enabled.") {
      it.split(',').map(FeatureFlag::valueOf)
    }

  val WITH_COMPOSE by directive("Whether to enable Compose compiler")

  val COMPOSE_FEATURES by
    valueDirective("Feature flags of Compose compiler to enable") { it.split(',').map(ComposeFeatureFlag::valueOf) }
}

enum class ComposeFeatureFlag {
  LiveLiterals,
  StrongSkipping,
  IntrinsicRemember,
  OptimizeNonSkippingGroups,
  PausableComposition,
}
