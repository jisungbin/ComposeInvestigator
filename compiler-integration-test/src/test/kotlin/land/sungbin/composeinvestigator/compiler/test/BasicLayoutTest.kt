/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test

import androidx.compose.runtime.RecomposeScope
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.mock.compositionTest
import androidx.compose.runtime.mock.expectNoChanges
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsOnly
import kotlin.test.BeforeTest
import kotlin.test.Test
import land.sungbin.composeinvestigator.compiler.test.TestConfiguration.logs
import land.sungbin.composeinvestigator.runtime.InvalidationReason
import land.sungbin.composeinvestigator.runtime.InvalidationType

class BasicLayoutTest {
  @BeforeTest fun prepare() {
    TestConfiguration.reset()
    basicLayoutTable.reset()
  }

  @Test fun initialComposition() = compositionTest {
    compose { BasicLayout() }
    assertThat(logs).containsOnly(
      Investigated(
        basicLayout(),
        InvalidationType.Processed(InvalidationReason.Initial),
      )
    )
  }

  @Test fun skipRecomposition() = compositionTest {
    var recomposeScope: RecomposeScope? = null

    compose {
      recomposeScope = currentRecomposeScope
      BasicLayout()
    }

    recomposeScope!!.invalidate()
    expectNoChanges()

    assertThat(logs).containsExactly(
      Investigated(
        basicLayout(),
        InvalidationType.Processed(InvalidationReason.Initial),
      ),
      Investigated(
        basicLayout(),
        InvalidationType.Skipped,
      ),
    )
  }
}
