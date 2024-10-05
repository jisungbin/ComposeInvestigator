// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.frontend

import kotlin.test.Test
import land.sungbin.composeinvestigator.compiler._compilation.AbstractCompilerTest
import land.sungbin.composeinvestigator.compiler._compilation.FirAnalysisResult
import land.sungbin.composeinvestigator.runtime.NoInvestigation
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldNotContain
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneType

// TODO I probably need more test cases, but I'm unsure which conditions to add...
class InvalidationTraceTableInstantiationValidatorTest : AbstractCompilerTest(sourceRoot = "frontend/traceTableInstantiation") {
  @Test fun noneComposable() {
    val analyze = analyze(source("noneComposable.kt"))
    analyze.fileAnnotations() shouldContain NO_INVESTIGATION_FQN
  }

  @Test fun singleComposableFunction() {
    val analyze = analyze(source("singleComposableFunction.kt"))
    analyze.fileAnnotations() shouldNotContain NO_INVESTIGATION_FQN
  }

  @Test fun singleComposableLambda() {
    val analyze = analyze(source("singleComposableLambda.kt"))
    analyze.fileAnnotations() shouldNotContain NO_INVESTIGATION_FQN
  }

  private fun FirAnalysisResult.fileAnnotations() =
    result.outputs
      .single().fir
      .single().annotations
      .mapNotNull { annotation ->
        annotation.annotationTypeRef.coneType.classId?.asFqNameString()
      }

  companion object {
    val NO_INVESTIGATION_FQN = NoInvestigation::class.qualifiedName!!
  }
}
