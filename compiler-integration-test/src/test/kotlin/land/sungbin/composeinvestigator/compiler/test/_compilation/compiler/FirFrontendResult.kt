/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test._compilation.compiler

import org.jetbrains.kotlin.backend.jvm.JvmGeneratorExtensions
import org.jetbrains.kotlin.fir.pipeline.Fir2IrActualizedResult

class FirFrontendResult(
  val firResult: Fir2IrActualizedResult,
  val generatorExtensions: JvmGeneratorExtensions,
)
