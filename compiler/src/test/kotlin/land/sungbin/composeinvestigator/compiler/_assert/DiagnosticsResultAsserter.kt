/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler._assert

import kotlin.test.assertEquals
import kotlin.test.fail
import land.sungbin.composeinvestigator.compiler._compilation.DiagnosticsResult
import org.jetbrains.kotlin.diagnostics.AbstractKtDiagnosticFactory

fun DiagnosticsResult.assertDiagnostics(
  diagnostic: AbstractKtDiagnosticFactory,
  expectMessage: (() -> String)? = null,
) {
  val diagnostics = diagnostics.getOrElse(diagnostic.name, ::emptyList)

  if (diagnostics.isEmpty() && expectMessage == null) return
  if (diagnostics.isEmpty() && expectMessage != null)
    fail("Expected diagnostic message but no diagnostic was found.")

  val actualMessage = diagnostics.single().message

  if (expectMessage == null)
    fail("Expected no diagnostic message but found: \n$actualMessage")

  assertEquals(expectMessage(), actualMessage)
}
