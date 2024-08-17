/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.frontend

import com.intellij.openapi.util.TextRange
import kotlin.test.Test
import kotlin.test.assertEquals
import land.sungbin.composeinvestigator.compiler._compilation.AbstractCompilerTest
import land.sungbin.composeinvestigator.compiler._compilation.DiagnosticsResult.Diagnostic
import land.sungbin.composeinvestigator.compiler._source.source

class ComposableNameCallCheckerTest : AbstractCompilerTest() {
  @Test fun hardcodeComposableName() {
    val diagnostics = analyze(source("frontend/composableNameCall/hardcodeComposableName.kt"))
      .diagnostics

    assertEquals(emptyMap(), diagnostics)
  }

  @Test fun expressionComposableName() {
    val diagnostics = analyze(source("frontend/composableNameCall/expressionComposableName.kt"))
      .diagnostics
    val expect = Diagnostic(
      message = """
|/expressionComposableName.kt:17:18: error: currently, only string hardcodes are supported as arguments to ComposableName.
|  ComposableName(Random.nextBoolean().toString())
|                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
      """.trimMargin(),
      ranges = listOf(TextRange(476, 508)),
    )

    assertEquals(mapOf("COMPOSABLE_NAME_ONLY_HARDCODED" to listOf(expect)), diagnostics)
  }
}
