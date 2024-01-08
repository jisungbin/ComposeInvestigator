/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("TestFunctionName")

package land.sungbin.composeinvestigator.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.RecomposeScope
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.StateObject
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.mockk.every
import io.mockk.mockk
import land.sungbin.composeinvestigator.runtime.mock.compositionTest
import land.sungbin.composeinvestigator.runtime.mock.expectChanges

class StateObjectTrackerTest : ShouldSpec() {
  data class STATE(val name: String, val previousValue: Any?, val newValue: Any?)

  init {
    val log = mutableListOf<STATE>()
    val listener = StateChangedListener { name, previousValue, newValue ->
      log.add(STATE(name = name, previousValue = previousValue, newValue = newValue))
    }

    beforeAny {
      log.clear()
      StateObjectTrackManager.clear()
      ComposeStateObjectValueGetter.clear()
    }

    should("Receive a callback when the state changes") {
      compositionTest {
        val table = mockk<ComposableInvalidationTrackTable> {
          every { currentComposableKeyName } returns "TestTable"
        }

        val stringState = mutableStateOf("string")
        val intState = mutableIntStateOf(0)
        val floatState = mutableFloatStateOf(0f)

        val untrackStringState = mutableStateOf("string")
        val untrackIntState = mutableIntStateOf(0)
        val untrackFloatState = mutableFloatStateOf(0f)

        compose {
          table.registerStateObjectTracking(
            composer = currentComposer,
            listener = listener,
            stateObjectFields = arrayOf(
              "stringState" to stringState as StateObject,
              "intState" to intState as StateObject,
              "floatState" to floatState as StateObject,
            ),
          )

          SideEffect {
            stringState.value += " string"
            intState.intValue++
            floatState.floatValue++

            untrackStringState.value += " string"
            untrackIntState.intValue++
            untrackFloatState.floatValue++
          }
        }

        log shouldContainExactlyInAnyOrder listOf(
          STATE(name = "stringState", previousValue = "string", newValue = "string string"),
          STATE(name = "intState", previousValue = 0, newValue = 1),
          STATE(name = "floatState", previousValue = 0f, newValue = 1f),
        )
      }
    }
    // TODO: Should lazily added states also support change tracking? I'm not sure if this
    //  is a meaningful feature and how to implement it (do lazily added states even exist?).
    xshould("Receive state change callbacks even when a tracking target is added lazily") {
      compositionTest {
        val table = mockk<ComposableInvalidationTrackTable> {
          every { currentComposableKeyName } returns "TestTable"
        }

        val stringState = mutableStateOf("string")
        val intState = mutableIntStateOf(0)
        val floatState = mutableFloatStateOf(0f)

        val stringState2 = mutableStateOf("string2")
        val intState2 = mutableIntStateOf(100)
        val floatState2 = mutableFloatStateOf(100f)

        var key = 1
        var recomposeScope: RecomposeScope? = null

        compose {
          recomposeScope = currentRecomposeScope

          when (key) {
            1 -> {
              table.registerStateObjectTracking(
                composer = currentComposer,
                listener = listener,
                stateObjectFields = arrayOf(
                  "stringState" to stringState as StateObject,
                  "intState" to intState as StateObject,
                  "floatState" to floatState as StateObject,
                ),
              )
            }
            2 -> {
              table.registerStateObjectTracking(
                composer = currentComposer,
                listener = listener,
                stateObjectFields = arrayOf(
                  "stringState2" to stringState2 as StateObject,
                  "intState2" to intState2 as StateObject,
                  "floatState2" to floatState2 as StateObject,
                ),
              )
            }
          }

          SideEffect {
            stringState.value += " string"
            intState.intValue++
            floatState.floatValue++

            stringState2.value += " string2"
            intState2.intValue++
            floatState2.floatValue++
          }
        }

        withClue("The second set of states should not be tracked yet.") {
          log shouldContainExactlyInAnyOrder listOf(
            STATE(name = "stringState", previousValue = "string", newValue = "string string"),
            STATE(name = "intState", previousValue = 0, newValue = 1),
            STATE(name = "floatState", previousValue = 0f, newValue = 1f),
          )
        }

        key++
        recomposeScope!!.invalidate()
        expectChanges()

        withClue("The second set of states should be tracked.") {
          log shouldContainExactlyInAnyOrder listOf(
            STATE(name = "stringState", previousValue = "string", newValue = "string string"),
            STATE(name = "stringState", previousValue = "string string", newValue = "string string string"),
            STATE(name = "intState", previousValue = 0, newValue = 1),
            STATE(name = "intState", previousValue = 1, newValue = 2),
            STATE(name = "floatState", previousValue = 0f, newValue = 1f),
            STATE(name = "floatState", previousValue = 1f, newValue = 2f),
            STATE(name = "stringState2", previousValue = "string2 string2", newValue = "string2 string2 string2"),
            STATE(name = "intState2", previousValue = 101, newValue = 102),
            STATE(name = "floatState2", previousValue = 101f, newValue = 102f),
          )
        }
      }
    }
    should("Registered callbacks are released when the composable reaches the Forgotten lifecycle") {
      compositionTest {
        val table = mockk<ComposableInvalidationTrackTable> {
          every { currentComposableKeyName } returns "TestTable"
        }

        var mount by mutableStateOf(true)

        val stringState = mutableStateOf("string")
        val intState = mutableIntStateOf(0)
        val floatState = mutableFloatStateOf(0f)

        @Composable
        fun Unmountable() {
          table.registerStateObjectTracking(
            composer = currentComposer,
            listener = listener,
            stateObjectFields = arrayOf(
              "stringState" to stringState as StateObject,
              "intState" to intState as StateObject,
              "floatState" to floatState as StateObject,
            ),
          )
        }

        compose {
          if (mount) Unmountable()
          SideEffect {
            stringState.value += " string"
            intState.intValue++
            floatState.floatValue++
          }
        }

        log shouldContainExactlyInAnyOrder listOf(
          STATE(name = "stringState", previousValue = "string", newValue = "string string"),
          STATE(name = "intState", previousValue = 0, newValue = 1),
          STATE(name = "floatState", previousValue = 0f, newValue = 1f),
        )

        mount = false
        expectChanges()

        log shouldContainExactlyInAnyOrder listOf(
          STATE(name = "stringState", previousValue = "string", newValue = "string string"),
          STATE(name = "intState", previousValue = 0, newValue = 1),
          STATE(name = "floatState", previousValue = 0f, newValue = 1f),
        )
      }
    }
  }
}
