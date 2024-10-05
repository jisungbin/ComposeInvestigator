// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler._assert

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail
import land.sungbin.composeinvestigator.compiler._compilation.FirAnalysisResult
import org.jetbrains.kotlin.diagnostics.AbstractKtDiagnosticFactory

private const val SEPARATOR = "\n=====\n"

fun FirAnalysisResult.assertNoDiagnostic(diagnostic: AbstractKtDiagnosticFactory) {
  val results = diagnostics.getOrElse(diagnostic.name, ::emptyList)
  assertTrue(
    results.isEmpty(),
    "Expected no diagnostic but found diagnostics:\n${results.joinToString(SEPARATOR)}",
  )
}

fun FirAnalysisResult.assertDiagnostics(
  diagnostic: AbstractKtDiagnosticFactory,
  expectMessages: () -> String,
) {
  val results = diagnostics.getOrElse(diagnostic.name, ::emptyList)
  if (results.isEmpty())
    fail("Expected diagnostics message but no diagnostic was found.")

  val actualMessages = results.joinToString(SEPARATOR)
  assertEquals(expectMessages().trim(), actualMessages)
}
