/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.frontend

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import land.sungbin.composeinvestigator.compiler._compilation.AbstractCompilerTest
import land.sungbin.composeinvestigator.compiler._compilation.FirAnalysisResult
import land.sungbin.composeinvestigator.compiler._source.source
import land.sungbin.composeinvestigator.runtime.NoInvestigation
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneType

class InvalidationTraceTableInstantiationValidatorTest : AbstractCompilerTest() {
  @Test fun noneComposable() {
    val analyze = analyze(source("frontend/traceTableInstantiation/noneComposable.kt"))
    assertContains(analyze.fileAnnotations(), NoInvestigation::class.qualifiedName!!)
  }

  @Test fun singleComposableFunction() {
    val analyze = analyze(source("frontend/traceTableInstantiation/singleComposableFunction.kt"))

    // TODO assertNotContains (KT-53336)
    assertFalse(analyze.fileAnnotations().contains(NoInvestigation::class.qualifiedName!!))
  }

  @Test fun singleComposableLambda() {
    val analyze = analyze(source("frontend/traceTableInstantiation/singleComposableLambda.kt"))

    // TODO assertNotContains (KT-53336)
    assertFalse(analyze.fileAnnotations().contains(NoInvestigation::class.qualifiedName!!))
  }

  private fun FirAnalysisResult.fileAnnotations() = result.outputs.first().fir.first().annotations.mapNotNull { annotation ->
    annotation.annotationTypeRef.coneType.classId?.asFqNameString()
  }
}
