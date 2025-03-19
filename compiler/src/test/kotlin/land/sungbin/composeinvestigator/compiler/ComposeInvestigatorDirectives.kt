// Copyright 2025 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

import org.jetbrains.kotlin.test.directives.model.SimpleDirectivesContainer

object ComposeInvestigatorDirectives : SimpleDirectivesContainer() {
  val INVESTIGATOR_FEATURES by
    valueDirective("[ComposeInvestigator] Feature flags to enable. If omitted, all flags are enabled.") {
      it.split(',').map(FeatureFlag::valueOf)
    }

  val COMPOSE_FEATURES by
    valueDirective("[Compose] Feature flags to enable") { it.split(',').map(ComposeFeatureFlag::valueOf) }
}

enum class ComposeFeatureFlag {
  LiveLiterals,
  StrongSkipping,
  IntrinsicRemember,
  OptimizeNonSkippingGroups,
  PausableComposition,
}
