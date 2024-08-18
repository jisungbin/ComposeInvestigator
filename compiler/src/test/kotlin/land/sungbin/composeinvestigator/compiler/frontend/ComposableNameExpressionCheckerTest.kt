/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.frontend

import kotlin.test.Test
import land.sungbin.composeinvestigator.compiler._assert.assertDiagnostic
import land.sungbin.composeinvestigator.compiler._compilation.AbstractCompilerTest
import land.sungbin.composeinvestigator.compiler._source.source
import land.sungbin.composeinvestigator.compiler.frontend.ComposeInvestigatorErrors.UNSUPPORTED_COMPOSABLE_NAME

class ComposableNameExpressionCheckerTest : AbstractCompilerTest() {
  @Test fun stringHardcodeExpression() {
    val analyze = analyze(source("frontend/composableNameExpression/stringHardcodeExpression.kt"))
    analyze.assertDiagnostic(UNSUPPORTED_COMPOSABLE_NAME)
  }

  @Test fun magicNumberToStringExpression() {
    val analyze = analyze(source("frontend/composableNameExpression/magicNumberToStringExpression.kt"))
    analyze.assertDiagnostic(UNSUPPORTED_COMPOSABLE_NAME) {
      """
error: currently, only string hardcodes are supported as arguments to ComposableName.
  ComposableName(42.toString())
                 ^^^^^^^^^^^^^^
      """.trim()
    }
  }
}
