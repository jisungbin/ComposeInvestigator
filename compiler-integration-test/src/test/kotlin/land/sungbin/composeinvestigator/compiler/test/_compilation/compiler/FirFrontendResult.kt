package land.sungbin.composeinvestigator.compiler.test._compilation.compiler

import org.jetbrains.kotlin.backend.jvm.JvmGeneratorExtensions
import org.jetbrains.kotlin.fir.pipeline.Fir2IrActualizedResult

class FirFrontendResult(
  val firResult: Fir2IrActualizedResult,
  val generatorExtensions: JvmGeneratorExtensions,
)
