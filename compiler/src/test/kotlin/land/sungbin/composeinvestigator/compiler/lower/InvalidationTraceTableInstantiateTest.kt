/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.lower

import kotlin.test.Test
import land.sungbin.composeinvestigator.compiler.FeatureFlag
import land.sungbin.composeinvestigator.compiler._extension.AbstractGoldenTest
import land.sungbin.composeinvestigator.compiler._source.source
import org.jetbrains.kotlin.utils.addToStdlib.enumSetOf

class InvalidationTraceTableInstantiateTest : AbstractGoldenTest(enumSetOf(FeatureFlag.StateInitializerTracking)) {
  @Test fun normalFile() =
    verifyIrGolden(source("lower/invalidationTraceTableInstantiate/normalFile.kt"))

  @Test fun file_name_with_dash() =
    verifyIrGolden(source("lower/invalidationTraceTableInstantiate/file-name-with-dash.kt"))

  @Test fun file_name_with_whitespace() =
    verifyIrGolden(source("lower/invalidationTraceTableInstantiate/file name with whitespace.kt"))
}
