/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.golden

import land.sungbin.composeinvestigator.compiler.test._compilation.AbstractIrTransformTest
import land.sungbin.composeinvestigator.compiler.test._source.sourceString
import org.junit.Test

class CodegenTest : AbstractIrTransformTest() {
  @Test fun invalidation_tracing_codegen() {
    verifyGoldenIrTransform(source = sourceString("codegen/InvalidationTracing.kt"))
  }

  @Test fun no_invalidation_tracing_file_level_codegen() {
    verifyGoldenIrTransform(source = sourceString("codegen/NoInvalidationTracingFileLevel.kt"))
  }

  @Test fun no_invalidation_tracing_function_level_codegen() {
    verifyGoldenIrTransform(source = sourceString("codegen/NoInvalidationTracingFunctionLevel.kt"))
  }

  @Test fun table_intrinsic_call_codegen() {
    verifyGoldenIrTransform(
      source = sourceString("codegen/TableIntrinsicCall.kt"),
      flags = Flags.NONE,
    )
  }
}
