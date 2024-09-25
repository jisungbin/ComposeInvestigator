/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler._assert

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail
import land.sungbin.composeinvestigator.compiler._compilation.FirAnalysisResult
import org.jetbrains.kotlin.diagnostics.AbstractKtDiagnosticFactory

fun FirAnalysisResult.assertNoDiagnostic(diagnostic: AbstractKtDiagnosticFactory) {
  val results = diagnostics.getOrElse(diagnostic.name, ::emptyList)
  assertTrue(results.isEmpty(), "Expected no diagnostic but found diagnostic(s).")
}

fun FirAnalysisResult.assertDiagnostics(
  diagnostic: AbstractKtDiagnosticFactory,
  expectMessages: () -> String,
) {
  val results = diagnostics.getOrElse(diagnostic.name, ::emptyList)
  if (results.isEmpty())
    fail("Expected diagnostics message but no diagnostic was found.")

  val actualMessages = results.joinToString("\n=====\n")
  assertEquals(expectMessages().trim(), actualMessages)
}
