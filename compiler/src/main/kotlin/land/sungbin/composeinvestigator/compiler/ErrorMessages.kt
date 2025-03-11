// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

internal object ErrorMessages {
  const val COMPOSE_INVESTIGATOR_NOT_GENERATED =
    "Files that are '@file:NoInvestigation' or does not contain any Composables will not generate " +
      "a ComoposeInvestigator."

  const val COMPOSABLE_SCOPED_API_MUST_CALL_WITHIN_COMPOSABLE =
    "@ComposableScope API can only be called in a Composable function."

  const val SUPPORTS_K2_ONLY = "ComposeInvestigator plugin supports only Kotlin 2.0.0 or higher."
}
