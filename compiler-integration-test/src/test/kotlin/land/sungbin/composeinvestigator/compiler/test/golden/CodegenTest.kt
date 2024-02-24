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

class CodegenTest : AbstractIrTransformTest(useFir = false) {
  @Test fun callstack_tracking_codegen() {
    verifyGoldenIrTransform(source = sourceString("codegen/CallstackTracking.kt"))
  }
}
