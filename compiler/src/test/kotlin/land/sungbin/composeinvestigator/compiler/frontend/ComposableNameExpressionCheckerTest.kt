// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.frontend

import kotlin.test.Test
import land.sungbin.composeinvestigator.compiler._assert.assertDiagnostics
import land.sungbin.composeinvestigator.compiler._assert.assertNoDiagnostic
import land.sungbin.composeinvestigator.compiler._compilation.AbstractCompilerTest
import land.sungbin.composeinvestigator.compiler.frontend.ComposeInvestigatorErrors.UNSUPPORTED_COMPOSABLE_NAME

class ComposableNameExpressionCheckerTest : AbstractCompilerTest(sourceRoot = "frontend/composableNameExpression") {
  @Test fun stringHardcodeExpression() {
    val analyze = analyze(source("stringHardcodeExpression.kt"))
    analyze.assertNoDiagnostic(UNSUPPORTED_COMPOSABLE_NAME)
  }

  @Test fun wrongExpressions() {
    val analyze = analyze(source("wrongExpressions.kt"))
    analyze.assertDiagnostics(UNSUPPORTED_COMPOSABLE_NAME) {
      """
error: currently, only string hardcodes are supported as arguments to ComposableName.
  ComposableName(42.toString())
                 ^^^^^^^^^^^^^^
=====
error: currently, only string hardcodes are supported as arguments to ComposableName.
  ComposableName("My" + "Composable")
                 ^^^^^^^^^^^^^^^^^^^^
=====
error: currently, only string hardcodes are supported as arguments to ComposableName.
  ComposableName("My${'$'}{"Composable"}")
                 ^^^^^^^^^^^^^^^^^^^^
=====
error: currently, only string hardcodes are supported as arguments to ComposableName.
  ComposableName(String())
                 ^^^^^^^^^
      """
    }
  }
}
