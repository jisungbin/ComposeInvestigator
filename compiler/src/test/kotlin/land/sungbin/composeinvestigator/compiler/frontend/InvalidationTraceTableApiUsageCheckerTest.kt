// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.frontend

import kotlin.test.Ignore
import kotlin.test.Test
import land.sungbin.composeinvestigator.compiler._assert.assertDiagnostics
import land.sungbin.composeinvestigator.compiler._assert.assertNoDiagnostic
import land.sungbin.composeinvestigator.compiler._compilation.AbstractCompilerTest
import land.sungbin.composeinvestigator.compiler.frontend.ComposeInvestigatorErrors.API_ACCESS_IN_NO_INVESTIGATION_FILE

// TODO asserts ILLEGAL_COMPOSABLE_SCOPE_CALL diagnostic
class InvalidationTraceTableApiUsageCheckerTest : AbstractCompilerTest(sourceRoot = "frontend/traceTableApiUsage") {
  @Test fun composableFunction() {
    val analyze = analyze(source("composableFunction.kt"))
    analyze.assertNoDiagnostic(API_ACCESS_IN_NO_INVESTIGATION_FILE)
  }

  @Test fun composableLambda() {
    val analyze = analyze(source("composableLambda.kt"))
    analyze.assertNoDiagnostic(API_ACCESS_IN_NO_INVESTIGATION_FILE)
  }

  @Test fun inlineComposableFunction() {
    val analyze = analyze(source("inlineComposableFunction.kt"))
    analyze.assertNoDiagnostic(API_ACCESS_IN_NO_INVESTIGATION_FILE)
  }

  @Test fun inlineComposableLambda() {
    val analyze = analyze(source("inlineComposableLambda.kt"))
    analyze.assertNoDiagnostic(API_ACCESS_IN_NO_INVESTIGATION_FILE)
  }

  @Test fun inlineNormalFunction() {
    val analyze = analyze(source("inlineNormalFunction.kt"))
    analyze.assertDiagnostics(API_ACCESS_IN_NO_INVESTIGATION_FILE) {
      """
error: files that are '@file:NoInvestigation' or does not contain any Composables will not generate a ComposableInvalidationTraceTable.
  val t = currentComposableInvalidationTracer
          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
=====
error: files that are '@file:NoInvestigation' or does not contain any Composables will not generate a ComposableInvalidationTraceTable.
  currentComposableInvalidationTracer.currentComposableKeyName
  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
      """
    }
  }

  @Test fun inlineNormalLambda() {
    val analyze = analyze(source("inlineNormalLambda.kt"))
    analyze.assertDiagnostics(API_ACCESS_IN_NO_INVESTIGATION_FILE) {
      """
error: files that are '@file:NoInvestigation' or does not contain any Composables will not generate a ComposableInvalidationTraceTable.
  val t = currentComposableInvalidationTracer
          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
=====
error: files that are '@file:NoInvestigation' or does not contain any Composables will not generate a ComposableInvalidationTraceTable.
    currentComposableInvalidationTracer.currentComposableName = ComposableName("")
    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
      """
    }
  }

  @Test fun normalFunction() {
    val analyze = analyze(source("normalFunction.kt"))
    analyze.assertDiagnostics(API_ACCESS_IN_NO_INVESTIGATION_FILE) {
      """
error: files that are '@file:NoInvestigation' or does not contain any Composables will not generate a ComposableInvalidationTraceTable.
  val t = currentComposableInvalidationTracer
          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
=====
error: files that are '@file:NoInvestigation' or does not contain any Composables will not generate a ComposableInvalidationTraceTable.
  currentComposableInvalidationTracer.currentComposableKeyName
  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
      """
    }
  }

  @Test fun normalLambda() {
    val analyze = analyze(source("normalLambda.kt"))
    analyze.assertDiagnostics(API_ACCESS_IN_NO_INVESTIGATION_FILE) {
      """
error: files that are '@file:NoInvestigation' or does not contain any Composables will not generate a ComposableInvalidationTraceTable.
  val t = currentComposableInvalidationTracer
          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
=====
error: files that are '@file:NoInvestigation' or does not contain any Composables will not generate a ComposableInvalidationTraceTable.
    currentComposableInvalidationTracer.currentComposableName = ComposableName("")
    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
      """
    }
  }

  @Test fun topLevelApiCallWithComposable() {
    val analyze = analyze(source("topLevelApiCallWithComposable.kt"))
    analyze.assertNoDiagnostic(API_ACCESS_IN_NO_INVESTIGATION_FILE)
  }

  @Test fun topLevelApiCallWithNoComposable() {
    val analyze = analyze(source("topLevelApiCallWithNoComposable.kt"))
    analyze.assertDiagnostics(API_ACCESS_IN_NO_INVESTIGATION_FILE) {
      """
error: files that are '@file:NoInvestigation' or does not contain any Composables will not generate a ComposableInvalidationTraceTable.
private val t = currentComposableInvalidationTracer
                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
      """
    }
  }

  @Ignore("NoComposableFileChecker should run first, but FirExtension has no guaranteed execution order?")
  @Test fun topLevelApiCallWithAllNoInvestigationComposables() {
    val analyze = analyze(source("topLevelApiCallWithAllNoInvestigationComposables.kt"))
    analyze.assertDiagnostics(API_ACCESS_IN_NO_INVESTIGATION_FILE) {
      """
error: files that are '@file:NoInvestigation' or does not contain any Composables will not generate a ComposableInvalidationTraceTable.
private val t = currentComposableInvalidationTracer
                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
      """
    }
  }
}
