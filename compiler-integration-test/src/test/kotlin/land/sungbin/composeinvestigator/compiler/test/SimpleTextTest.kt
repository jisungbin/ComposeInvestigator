/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test

import androidx.compose.runtime.RecomposeScope
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mock.compositionTest
import androidx.compose.runtime.mock.expectChanges
import androidx.compose.runtime.mock.expectNoChanges
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsOnly
import kotlin.test.BeforeTest
import kotlin.test.Test
import land.sungbin.composeinvestigator.compiler.test.TestConfiguration.logs
import land.sungbin.composeinvestigator.runtime.ChangedArgument
import land.sungbin.composeinvestigator.runtime.InvalidationResult
import land.sungbin.composeinvestigator.runtime.Stability
import land.sungbin.composeinvestigator.runtime.ValueArgument

class SimpleTextTest {
  @BeforeTest fun prepare() {
    TestConfiguration.reset()
    simpleTextTable.reset()
  }

  @Test fun initialComposition() = compositionTest {
    compose { SimpleText() }
    assertThat(logs).containsOnly(Investigated(simpleText(), InvalidationResult.InitialComposition))
  }

  @Test fun skipRecomposition() = compositionTest {
    var recomposeScope: RecomposeScope? = null

    compose {
      recomposeScope = currentRecomposeScope
      SimpleText()
    }

    recomposeScope!!.invalidate()
    expectNoChanges()

    assertThat(logs).containsExactly(
      Investigated(simpleText(), InvalidationResult.InitialComposition),
      Investigated(simpleText(), InvalidationResult.Skipped),
    )
  }

  @Test fun recomposition() = compositionTest {
    var value by mutableIntStateOf(0)

    compose { SimpleText(value.toString()) }

    value++
    expectChanges()

    assertThat(logs).containsExactly(
      Investigated(simpleText(), InvalidationResult.InitialComposition),
      Investigated(
        simpleText(),
        InvalidationResult.ArgumentChanged(
          listOf(
            ChangedArgument(
              previous = ValueArgument(
                name = "value",
                type = "kotlin.String",
                valueString = "0",
                valueHashCode = 48,
                stability = Stability.Stable,
              ),
              new = ValueArgument(
                name = "value",
                type = "kotlin.String",
                valueString = "1",
                valueHashCode = 49,
                stability = Stability.Stable,
              ),
            ),
          ),
        ),
      ),
    )
  }
}
