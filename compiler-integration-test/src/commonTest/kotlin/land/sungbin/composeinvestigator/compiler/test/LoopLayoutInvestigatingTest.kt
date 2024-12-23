// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.test

import androidx.compose.runtime.RecomposeScope
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import assertk.assertThat
import composemock.action
import composemock.runCompose
import composemock.runningCompose
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import land.sungbin.composeinvestigator.compiler.test.TestConfiguration.logs
import land.sungbin.composeinvestigator.compiler.test.assertion.assertInvestigations
import land.sungbin.composeinvestigator.compiler.test.util.awaitInvalidation
import land.sungbin.composeinvestigator.runtime.ChangedArgument
import land.sungbin.composeinvestigator.runtime.InvalidationResult
import land.sungbin.composeinvestigator.runtime.Stability
import land.sungbin.composeinvestigator.runtime.ValueArgument

class LoopLayoutInvestigatingTest {
  @BeforeTest fun prepare() {
    TestConfiguration.reset()
    loopLayoutTable.reset()
  }

  @Test fun initialComposition() {
    runCompose { LoopLayout() }

    assertThat(logs).assertInvestigations(
      Investigated(loopLayout(), InvalidationResult.InitialComposition),
      Investigated(loopText(), InvalidationResult.InitialComposition),
      Investigated(loopText(), InvalidationResult.InitialComposition),
      Investigated(loopText(), InvalidationResult.InitialComposition),
      Investigated(loopText(), InvalidationResult.InitialComposition),
      Investigated(loopText(), InvalidationResult.InitialComposition),
    )
  }

  @Test fun skipRecomposition() = runTest {
    var recomposeScope: RecomposeScope? = null

    runningCompose {
      recomposeScope = currentRecomposeScope
      LoopLayout()
    } action {
      recomposeScope!!.invalidate()
      awaitInvalidation()

      assertThat(logs).assertInvestigations(
        Investigated(loopLayout(), InvalidationResult.InitialComposition),
        Investigated(loopText(), InvalidationResult.InitialComposition),
        Investigated(loopText(), InvalidationResult.InitialComposition),
        Investigated(loopText(), InvalidationResult.InitialComposition),
        Investigated(loopText(), InvalidationResult.InitialComposition),
        Investigated(loopText(), InvalidationResult.InitialComposition),
        Investigated(loopLayout(), InvalidationResult.Skipped),
      )
    }
  }

  @Test fun recomposition() = runTest {
    var value by mutableIntStateOf(0)

    runningCompose { LoopLayout(value) } action {
      value++
      awaitInvalidation()

      assertThat(logs).assertInvestigations(
        Investigated(loopLayout(), InvalidationResult.InitialComposition),
        Investigated(loopText(), InvalidationResult.InitialComposition),
        Investigated(loopText(), InvalidationResult.InitialComposition),
        Investigated(loopText(), InvalidationResult.InitialComposition),
        Investigated(loopText(), InvalidationResult.InitialComposition),
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
        Investigated(loopText(), InvalidationResult.Skipped),
        Investigated(loopText(), InvalidationResult.Skipped),
        Investigated(
          loopText(),
          InvalidationResult.ArgumentChanged(
            listOf(
              ChangedArgument(
                previous = ValueArgument(
                  name = "calucation",
                  type = "kotlin.Function0",
                  valueString = "() -> kotlin.String",
                  valueHashCode = 0,
                  stability = Stability.Stable,
                ),
                new = ValueArgument(
                  name = "calucation",
                  type = "kotlin.Function0",
                  valueString = "() -> kotlin.String",
                  valueHashCode = 0,
                  stability = Stability.Stable,
                ),
              ),
            ),
          ),
        ),
        Investigated(loopText(), InvalidationResult.Skipped),
        Investigated(
          loopText(),
          InvalidationResult.ArgumentChanged(
            listOf(
              ChangedArgument(
                previous = ValueArgument(
                  name = "calucation",
                  type = "kotlin.Function0",
                  valueString = "() -> kotlin.String",
                  valueHashCode = 0,
                  stability = Stability.Stable,
                ),
                new = ValueArgument(
                  name = "calucation",
                  type = "kotlin.Function0",
                  valueString = "() -> kotlin.String",
                  valueHashCode = 0,
                  stability = Stability.Stable,
                ),
              ),
            ),
          ),
        ),
      )
    }
  }
}
