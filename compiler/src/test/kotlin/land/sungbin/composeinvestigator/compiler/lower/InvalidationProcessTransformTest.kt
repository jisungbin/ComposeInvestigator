/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.lower

import land.sungbin.composeinvestigator.compiler.FeatureFlag
import land.sungbin.composeinvestigator.compiler._extension.AbstractGoldenTest
import land.sungbin.composeinvestigator.compiler._extension.GoldenVerification
import org.jetbrains.kotlin.utils.addToStdlib.enumSetOf

@GoldenVerification("lower", "invalidationProcessAndSkip")
class InvalidationProcessTransformTest : AbstractGoldenTest(enumSetOf(FeatureFlag.InvalidationProcessTracing))
