/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.frontend

import kotlin.test.Ignore
import kotlin.test.Test
import land.sungbin.composeinvestigator.compiler._assert.assertDiagnostics
import land.sungbin.composeinvestigator.compiler._compilation.AbstractCompilerTest
import land.sungbin.composeinvestigator.compiler._source.source
import land.sungbin.composeinvestigator.compiler.frontend.ComposeInvestigatorErrors.API_ACCESS_IN_NO_INVESTIGATION_FILE
import land.sungbin.composeinvestigator.compiler.frontend.ComposeInvestigatorErrors.ILLEGAL_COMPOSABLE_SCOPE_CALL

@Ignore("Need to rewrite logic to check if current function is in composable scope")
class InvalidationTraceTableApiUsageCheckerTest : AbstractCompilerTest() {
  @Test fun composableFunction() {
    val analyze = analyze(source("frontend/traceTableApiUsage/composableFunction.kt"))
    analyze.assertDiagnostics(API_ACCESS_IN_NO_INVESTIGATION_FILE)
    analyze.assertDiagnostics(ILLEGAL_COMPOSABLE_SCOPE_CALL)
  }

  @Test fun composableLambda() {
    val analyze = analyze(source("frontend/traceTableApiUsage/composableLambda.kt"))
    analyze.assertDiagnostics(API_ACCESS_IN_NO_INVESTIGATION_FILE)
    analyze.assertDiagnostics(ILLEGAL_COMPOSABLE_SCOPE_CALL)
  }

  @Test fun inlineComposableFunction() {
    val analyze = analyze(source("frontend/traceTableApiUsage/inlineComposableFunction.kt"))
    analyze.assertDiagnostics(API_ACCESS_IN_NO_INVESTIGATION_FILE)
    analyze.assertDiagnostics(ILLEGAL_COMPOSABLE_SCOPE_CALL)
  }

  @Test fun inlineComposableLambda() {
    val analyze = analyze(source("frontend/traceTableApiUsage/inlineComposableLambda.kt"))
    analyze.assertDiagnostics(API_ACCESS_IN_NO_INVESTIGATION_FILE)
    analyze.assertDiagnostics(ILLEGAL_COMPOSABLE_SCOPE_CALL)
  }

  @Test fun inlineNormalFunction() {
    val analyze = analyze(source("frontend/traceTableApiUsage/inlineNormalFunction.kt"))
    analyze.assertDiagnostics(API_ACCESS_IN_NO_INVESTIGATION_FILE) {
      """
error: files that are '@file:NoInvestigation' or does not contain any Composables will not generate a ComposableInvalidationTraceTable.
  with(currentComposableInvalidationTracer) {
       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
=====
error: files that are '@file:NoInvestigation' or does not contain any Composables will not generate a ComposableInvalidationTraceTable.
    currentComposableName
    ^^^^^^^^^^^^^^^^^^^^^
=====
error: files that are '@file:NoInvestigation' or does not contain any Composables will not generate a ComposableInvalidationTraceTable.
    currentComposableName = ComposableName("")
    ^^^^^^^^^^^^^^^^^^^^^
=====
error: files that are '@file:NoInvestigation' or does not contain any Composables will not generate a ComposableInvalidationTraceTable.
    currentComposableKeyName
    ^^^^^^^^^^^^^^^^^^^^^^^^
      """
    }
    analyze.assertDiagnostics(ILLEGAL_COMPOSABLE_SCOPE_CALL) {
      """
error: @ComposableScope API can only be used in a Composable function.
    currentComposableName
    ^^^^^^^^^^^^^^^^^^^^^
=====
error: @ComposableScope API can only be used in a Composable function.
    currentComposableName = ComposableName("")
    ^^^^^^^^^^^^^^^^^^^^^
=====
error: @ComposableScope API can only be used in a Composable function.
    currentComposableKeyName
    ^^^^^^^^^^^^^^^^^^^^^^^^
      """
    }
  }

  @Test fun inlineNormalLambda() {
    val analyze = analyze(source("frontend/traceTableApiUsage/inlineNormalLambda.kt"))
    analyze.assertDiagnostics(API_ACCESS_IN_NO_INVESTIGATION_FILE) {
      """
error: files that are '@file:NoInvestigation' or does not contain any Composables will not generate a ComposableInvalidationTraceTable.
    with(currentComposableInvalidationTracer) {
         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
=====
error: files that are '@file:NoInvestigation' or does not contain any Composables will not generate a ComposableInvalidationTraceTable.
      currentComposableName
      ^^^^^^^^^^^^^^^^^^^^^
=====
error: files that are '@file:NoInvestigation' or does not contain any Composables will not generate a ComposableInvalidationTraceTable.
      currentComposableName = ComposableName("")
      ^^^^^^^^^^^^^^^^^^^^^
=====
error: files that are '@file:NoInvestigation' or does not contain any Composables will not generate a ComposableInvalidationTraceTable.
      currentComposableKeyName
      ^^^^^^^^^^^^^^^^^^^^^^^^
      """
    }
    analyze.assertDiagnostics(ILLEGAL_COMPOSABLE_SCOPE_CALL) {
      """
error: @ComposableScope API can only be used in a Composable function.
      currentComposableName
      ^^^^^^^^^^^^^^^^^^^^^
=====
error: @ComposableScope API can only be used in a Composable function.
      currentComposableName = ComposableName("")
      ^^^^^^^^^^^^^^^^^^^^^
=====
error: @ComposableScope API can only be used in a Composable function.
      currentComposableKeyName
      ^^^^^^^^^^^^^^^^^^^^^^^^
      """
    }
  }

  @Test fun normalFunction() {
    val analyze = analyze(source("frontend/traceTableApiUsage/normalFunction.kt"))
    analyze.assertDiagnostics(API_ACCESS_IN_NO_INVESTIGATION_FILE) {
      """
error: files that are '@file:NoInvestigation' or does not contain any Composables will not generate a ComposableInvalidationTraceTable.
  with(currentComposableInvalidationTracer) {
       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
=====
error: files that are '@file:NoInvestigation' or does not contain any Composables will not generate a ComposableInvalidationTraceTable.
    currentComposableName
    ^^^^^^^^^^^^^^^^^^^^^
=====
error: files that are '@file:NoInvestigation' or does not contain any Composables will not generate a ComposableInvalidationTraceTable.
    currentComposableName = ComposableName("")
    ^^^^^^^^^^^^^^^^^^^^^
=====
error: files that are '@file:NoInvestigation' or does not contain any Composables will not generate a ComposableInvalidationTraceTable.
    currentComposableKeyName
    ^^^^^^^^^^^^^^^^^^^^^^^^
      """
    }
    analyze.assertDiagnostics(ILLEGAL_COMPOSABLE_SCOPE_CALL) {
      """
error: @ComposableScope API can only be used in a Composable function.
    currentComposableName
    ^^^^^^^^^^^^^^^^^^^^^
=====
error: @ComposableScope API can only be used in a Composable function.
    currentComposableName = ComposableName("")
    ^^^^^^^^^^^^^^^^^^^^^
=====
error: @ComposableScope API can only be used in a Composable function.
    currentComposableKeyName
    ^^^^^^^^^^^^^^^^^^^^^^^^
      """
    }
  }

  @Test fun normalLambda() {
    val analyze = analyze(source("frontend/traceTableApiUsage/normalLambda.kt"))
    analyze.assertDiagnostics(API_ACCESS_IN_NO_INVESTIGATION_FILE) {
      """
error: files that are '@file:NoInvestigation' or does not contain any Composables will not generate a ComposableInvalidationTraceTable.
    with(currentComposableInvalidationTracer) {
         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
=====
error: files that are '@file:NoInvestigation' or does not contain any Composables will not generate a ComposableInvalidationTraceTable.
      currentComposableName
      ^^^^^^^^^^^^^^^^^^^^^
=====
error: files that are '@file:NoInvestigation' or does not contain any Composables will not generate a ComposableInvalidationTraceTable.
      currentComposableName = ComposableName("")
      ^^^^^^^^^^^^^^^^^^^^^
=====
error: files that are '@file:NoInvestigation' or does not contain any Composables will not generate a ComposableInvalidationTraceTable.
      currentComposableKeyName
      ^^^^^^^^^^^^^^^^^^^^^^^^
      """
    }
    analyze.assertDiagnostics(ILLEGAL_COMPOSABLE_SCOPE_CALL) {
      """
error: @ComposableScope API can only be used in a Composable function.
      currentComposableName
      ^^^^^^^^^^^^^^^^^^^^^
=====
error: @ComposableScope API can only be used in a Composable function.
      currentComposableName = ComposableName("")
      ^^^^^^^^^^^^^^^^^^^^^
=====
error: @ComposableScope API can only be used in a Composable function.
      currentComposableKeyName
      ^^^^^^^^^^^^^^^^^^^^^^^^
      """
    }
  }
}
