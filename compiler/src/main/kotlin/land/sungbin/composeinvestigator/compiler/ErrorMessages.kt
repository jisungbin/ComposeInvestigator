// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

internal object ErrorMessages {
  const val COMPOSABLE_NAME_EXPRESSION_ONLY_HARDCODED =
    "Currently, only string hardcodes are supported as arguments to ComposableName."

  const val TRACE_TABLE_NOT_GENERATED =
    "Files that are '@file:NoInvestigation' or does not contain any Composables will not generate " +
      "a ComposableInvalidationTraceTable."

  const val COMPOSABLE_SCOPED_API_MUST_CALL_WITHIN_COMPOSABLE =
    "@ComposableScope API can only be used in a Composable function."

  const val SUPPORTS_K2_ONLY = "ComposeInvestigator plugin supports only Kotlin 2.0.0 or higher."
}
