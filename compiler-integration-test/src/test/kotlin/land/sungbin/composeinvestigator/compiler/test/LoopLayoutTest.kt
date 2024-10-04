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
import kotlin.test.Ignore
import kotlin.test.Test
import land.sungbin.composeinvestigator.compiler.test.TestConfiguration.logs
import land.sungbin.composeinvestigator.runtime.ChangedArgument
import land.sungbin.composeinvestigator.runtime.InvalidationResult
import land.sungbin.composeinvestigator.runtime.Stability
import land.sungbin.composeinvestigator.runtime.ValueArgument

@Ignore("currentCompositeKeyHash")
class LoopLayoutTest {
  @BeforeTest fun prepare() {
    TestConfiguration.reset()
    loopLayoutTable.reset()
  }

  @Test fun initialComposition() = compositionTest {
    compose { LoopLayout() }
    assertThat(logs).containsOnly(
      Investigated(loopLayout(), InvalidationResult.InitialComposition),
      Investigated(loopText(), InvalidationResult.InitialComposition),
    )
  }

  @Test fun skipRecomposition() = compositionTest {
    var recomposeScope: RecomposeScope? = null

    compose {
      recomposeScope = currentRecomposeScope
      LoopLayout()
    }

    recomposeScope!!.invalidate()
    expectNoChanges()

    assertThat(logs).containsExactly(
      Investigated(loopLayout(), InvalidationResult.InitialComposition),
      Investigated(loopText(), InvalidationResult.InitialComposition),
      Investigated(loopLayout(), InvalidationResult.Skipped),
    )
  }

  @Test fun recomposition() = compositionTest {
    var value by mutableIntStateOf(0)

    compose { LoopLayout(value) }

    value++
    expectChanges()

    assertThat(logs).containsExactly(
      Investigated(loopLayout(), InvalidationResult.InitialComposition),
      Investigated(loopText(), InvalidationResult.InitialComposition),
      Investigated(
        loopLayout(),
        InvalidationResult.ArgumentChanged(
          listOf(
            ChangedArgument(
              previous = ValueArgument(
                name = "value",
                type = "kotlin.Int",
                valueString = "0",
                valueHashCode = 0,
                stability = Stability.Stable,
              ),
              new = ValueArgument(
                name = "value",
                type = "kotlin.Int",
                valueString = "1",
                valueHashCode = 1,
                stability = Stability.Stable,
              ),
            ),
          ),
        ),
      ),
      Investigated(loopText(), InvalidationResult.Recomposition),
    )
  }
}
