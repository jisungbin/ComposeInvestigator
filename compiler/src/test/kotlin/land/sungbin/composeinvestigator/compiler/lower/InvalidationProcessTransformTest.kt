/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.lower

import kotlin.test.Test
import land.sungbin.composeinvestigator.compiler.FeatureFlag
import land.sungbin.composeinvestigator.compiler._compilation.AbstractCompilerTest
import land.sungbin.composeinvestigator.compiler._source.source
import org.jetbrains.kotlin.utils.addToStdlib.enumSetOf

class InvalidationProcessTransformTest : AbstractCompilerTest(enumSetOf(FeatureFlag.InvalidationProcessTracing)) {
  @Test fun movableComposable() = irTest(source("lower/invalidationProcessAndSkip/movableComposable.kt")) {
    """

    """
  }

  @Test fun noGroupComposable() = irTest(source("lower/invalidationProcessAndSkip/noGroupComposable.kt")) {
    """

    """
  }

  @Test fun noInvestigationComposable() = irTest(source("lower/invalidationProcessAndSkip/noInvestigationComposable.kt")) {
    """

    """
  }

  @Test fun noInvestigationFile() = irTest(source("lower/invalidationProcessAndSkip/noInvestigationFile.kt")) {
    """

    """
  }

  @Test fun readonlyComposable() = irTest(source("lower/invalidationProcessAndSkip/readonlyComposable.kt")) {
    """

    """
  }

  @Test fun replaceableComposable() = irTest(source("lower/invalidationProcessAndSkip/replaceableComposable.kt")) {
    """

    """
  }

  @Test fun restartableComposable() = irTest(source("lower/invalidationProcessAndSkip/restartableComposable.kt")) {
    """

    """
  }
}
