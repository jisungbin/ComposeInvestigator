/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.frontend

import kotlin.test.Test
import land.sungbin.composeinvestigator.compiler._assert.assertDiagnostics
import land.sungbin.composeinvestigator.compiler._compilation.AbstractCompilerTest
import land.sungbin.composeinvestigator.compiler._source.source
import land.sungbin.composeinvestigator.compiler.frontend.ComposeInvestigatorErrors.ILLEGAL_COMPOSABLE_NAME

class ComposableNameCallCheckerTest : AbstractCompilerTest() {
  @Test fun composableFunction() {
    val analyze = analyze(source("frontend/composableNameCall/composableFunction.kt"))
    analyze.assertDiagnostics(ILLEGAL_COMPOSABLE_NAME)
  }

  @Test fun composableLambda() {
    val analyze = analyze(source("frontend/composableNameCall/composableLambda.kt"))
    analyze.assertDiagnostics(ILLEGAL_COMPOSABLE_NAME)
  }

  @Test fun inlineComposableFunction() {
    val analyze = analyze(source("frontend/composableNameCall/inlineComposableFunction.kt"))
    analyze.assertDiagnostics(ILLEGAL_COMPOSABLE_NAME)
  }

  @Test fun inlineComposableLambda() {
    val analyze = analyze(source("frontend/composableNameCall/inlineComposableLambda.kt"))
    analyze.assertDiagnostics(ILLEGAL_COMPOSABLE_NAME)
  }

  @Test fun inlineNormalFunction() {
    val analyze = analyze(source("frontend/composableNameCall/inlineNormalFunction.kt"))
    analyze.assertDiagnostics(ILLEGAL_COMPOSABLE_NAME) {
      """
error: a ComposableName can only be used in a Composable function.
  ComposableName("")
  ^^^^^^^^^^^^^^^^^^
      """.trim()
    }
  }

  @Test fun inlineNormalLambda() {
    val analyze = analyze(source("frontend/composableNameCall/inlineNormalLambda.kt"))
    analyze.assertDiagnostics(ILLEGAL_COMPOSABLE_NAME) {
      """
error: a ComposableName can only be used in a Composable function.
  l { ComposableName("") }
      ^^^^^^^^^^^^^^^^^^
      """.trim()
    }
  }

  @Test fun normalFunction() {
    val analyze = analyze(source("frontend/composableNameCall/normalFunction.kt"))
    analyze.assertDiagnostics(ILLEGAL_COMPOSABLE_NAME) {
      """
error: a ComposableName can only be used in a Composable function.
  ComposableName("")
  ^^^^^^^^^^^^^^^^^^
      """.trim()
    }
  }

  @Test fun normalLambda() {
    val analyze = analyze(source("frontend/composableNameCall/normalLambda.kt"))
    analyze.assertDiagnostics(ILLEGAL_COMPOSABLE_NAME) {
      """
error: a ComposableName can only be used in a Composable function.
  l { ComposableName("") }
      ^^^^^^^^^^^^^^^^^^
      """.trim()
    }
  }
}
