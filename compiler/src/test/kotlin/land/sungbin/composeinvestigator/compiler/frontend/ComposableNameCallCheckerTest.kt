package land.sungbin.composeinvestigator.compiler.frontend

import com.intellij.openapi.util.TextRange
import kotlin.test.Test
import kotlin.test.assertEquals
import land.sungbin.composeinvestigator.compiler._compilation.AbstractCompilerTest
import land.sungbin.composeinvestigator.compiler._compilation.AnalysisResult.Diagnostic
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

    assertEquals(
      mapOf("COMPOSABLE_NAME_ONLY_HARDCODED" to listOf(Diagnostic(TextRange(236, 268)))),
      diagnostics,
    )
  }
}