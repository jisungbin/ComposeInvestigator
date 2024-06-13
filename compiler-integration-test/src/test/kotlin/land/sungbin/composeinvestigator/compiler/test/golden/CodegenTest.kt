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
  @Test fun callstack_tracking_codegen() {
    verifyGoldenIrTransform(source = sourceString("codegen/CallstackTracking.kt"))
  }

  @Test fun state_tracking_codegen() {
    verifyGoldenIrTransform(
      source = sourceString("codegen/StateTracking.kt"),
      flags = Flags.COMPOSE or Flags.NO_CALLSTACK_TRACKING,
    )
  }

  @Test fun no_state_tracking_codegen() {
    verifyGoldenIrTransform(
      source = sourceString("codegen/NoStateTracking.kt"),
      flags = Flags.COMPOSE or Flags.NO_CALLSTACK_TRACKING,
    )
  }

  @Test fun invalidation_tracking_codegen() {
    verifyGoldenIrTransform(
      source = sourceString("codegen/InvalidationTracking.kt"),
      flags = Flags.COMPOSE or Flags.NO_CALLSTACK_TRACKING,
    )
  }

  @Test fun no_invalidation_tracking_file_level_codegen() {
    verifyGoldenIrTransform(source = sourceString("codegen/NoInvalidationTrackingFileLevel.kt"))
  }

  @Test fun no_invalidation_tracking_function_level_codegen() {
    verifyGoldenIrTransform(source = sourceString("codegen/NoInvalidationTrackingFunctionLevel.kt"))
  }

  @Test fun table_intrinsic_call_codegen() {
    verifyGoldenIrTransform(
      source = sourceString("codegen/TableIntrinsicCall.kt"),
      flags = Flags.NONE,
    )
  }
}
