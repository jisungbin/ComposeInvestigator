/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler._assert

import kotlin.test.assertEquals
import kotlin.test.fail
import land.sungbin.composeinvestigator.compiler._compilation.FirAnalysisResult
import org.jetbrains.kotlin.diagnostics.AbstractKtDiagnosticFactory

fun FirAnalysisResult.assertDiagnostic(
  diagnostic: AbstractKtDiagnosticFactory,
  expectMessage: (() -> String)? = null,
) {
  val diagnostics = diagnostics.getOrElse(diagnostic.name, ::emptyList)

  if (diagnostics.isEmpty() && expectMessage == null) return
  if (diagnostics.isEmpty() && expectMessage != null)
    fail("Expected diagnostic message but no diagnostic was found.")

  val actualMessage = diagnostics.single()

  if (expectMessage == null)
    fail("Expected no diagnostic message but found: \n$actualMessage")

  assertEquals(expectMessage().trim(), actualMessage)
}

fun FirAnalysisResult.assertDiagnostics(
  diagnostic: AbstractKtDiagnosticFactory,
  expectMessages: (() -> String)? = null,
) {
  val diagnostics = diagnostics.getOrElse(diagnostic.name, ::emptyList)

  if (diagnostics.isEmpty() && expectMessages == null) return
  if (diagnostics.isEmpty() && expectMessages != null)
    fail("Expected diagnostics message but no diagnostic was found.")

  val actualMessages = diagnostics.joinToString("\n=====\n")

  if (expectMessages == null)
    fail("Expected no diagnostic message but found: \n$actualMessages")

  assertEquals(expectMessages().trim(), actualMessages)
}
