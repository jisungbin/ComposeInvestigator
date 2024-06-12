package land.sungbin.composeinvestigator.compiler.test._compilation.compiler

import com.intellij.openapi.util.TextRange
import org.jetbrains.kotlin.psi.KtFile

interface AnalysisResult {
  data class Diagnostic(
    val factoryName: String,
    val textRanges: List<TextRange>,
  )

  val files: List<KtFile>
  val diagnostics: Map<String, List<Diagnostic>>
}