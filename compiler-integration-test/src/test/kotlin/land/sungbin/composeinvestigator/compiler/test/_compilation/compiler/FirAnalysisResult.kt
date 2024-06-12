package land.sungbin.composeinvestigator.compiler.test._compilation.compiler

import org.jetbrains.kotlin.diagnostics.impl.BaseDiagnosticsCollector
import org.jetbrains.kotlin.fir.pipeline.FirResult
import org.jetbrains.kotlin.psi.KtFile

class FirAnalysisResult(
  val firResult: FirResult,
  override val files: List<KtFile>,
  val reporter: BaseDiagnosticsCollector,
) : AnalysisResult {
  override val diagnostics: Map<String, List<AnalysisResult.Diagnostic>>
    get() = reporter.diagnostics.groupBy(
      keySelector = { it.psiElement.containingFile.name },
      valueTransform = { AnalysisResult.Diagnostic(factoryName = it.factoryName, textRanges = it.textRanges) },
    )
}