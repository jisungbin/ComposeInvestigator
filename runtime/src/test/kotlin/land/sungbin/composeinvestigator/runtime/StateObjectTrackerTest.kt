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
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import land.sungbin.composeinvestigator.runtime.affect.AffectedComposable
import land.sungbin.composeinvestigator.runtime.mock.compositionTest
import land.sungbin.composeinvestigator.runtime.mock.expectChanges

class StateObjectTrackerTest : ShouldSpec() {
  private data class STATE(
    val composable: AffectedComposable,
    val name: String,
    val previousValue: Any?,
    val newValue: Any?,
  )

  private fun AffectedComposable(name: String) =
    AffectedComposable(
      name = name,
      pkg = "TestPackage",
      filePath = "TestFilePath",
      startLine = 0,
      startColumn = 0,
    )

  init {
    val log = mutableListOf<STATE>()

    ComposeInvestigatorConfig.stateChangedListener = StateChangedListener { composable, name, previousValue, newValue ->
      log.add(STATE(composable = composable, name = name, previousValue = previousValue, newValue = newValue))
    }

    beforeAny {
      log.clear()
      StateObjectTrackManager.clear()
      ComposeStateObjectValueGetter.clear()
    }

    should("Receive a callback when the state changes") {
      compositionTest {
        val stringState = mutableStateOf("string")
        val intState = mutableIntStateOf(0)
        val floatState = mutableFloatStateOf(0f)

        val untrackStringState = mutableStateOf("string")
        val untrackIntState = mutableIntStateOf(0)
        val untrackFloatState = mutableFloatStateOf(0f)

        var recomposeScope: RecomposeScope? = null

        compose {
          recomposeScope = currentRecomposeScope

          listOf(
            "stringState" to stringState,
            "intState" to intState,
            "floatState" to floatState,
          ).forEach { (name, state) ->
            val composable = AffectedComposable(name)
            state.registerStateObjectTracking(
              composer = currentComposer,
              composable = composable,
              composableKeyName = "KeyName",
              stateName = name,
            )
          }

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
          STATE(composable = AffectedComposable("stringState"), name = "stringState", previousValue = "string", newValue = "string string"),
          STATE(composable = AffectedComposable("intState"), name = "intState", previousValue = 0, newValue = 1),
          STATE(composable = AffectedComposable("floatState"), name = "floatState", previousValue = 0f, newValue = 1f),
        )

        recomposeScope!!.invalidate()
        expectChanges()

        withClue("No duplicate logs") {
          log shouldContainExactlyInAnyOrder listOf(
            STATE(composable = AffectedComposable("stringState"), name = "stringState", previousValue = "string", newValue = "string string"),
            STATE(composable = AffectedComposable("stringState"), name = "stringState", previousValue = "string string", newValue = "string string string"),
            STATE(composable = AffectedComposable("intState"), name = "intState", previousValue = 0, newValue = 1),
            STATE(composable = AffectedComposable("intState"), name = "intState", previousValue = 1, newValue = 2),
            STATE(composable = AffectedComposable("floatState"), name = "floatState", previousValue = 0f, newValue = 1f),
            STATE(composable = AffectedComposable("floatState"), name = "floatState", previousValue = 1f, newValue = 2f),
          )
        }
      }
    }
    // TODO: Should lazily added states also support change tracking? I'm not sure if this
    //  is a meaningful feature and how to implement it (do lazily added states even exist?).
    xshould("Receive state change callbacks even when a tracking target is added lazily") {
      compositionTest {
        val stringState = mutableStateOf("string")
        val intState = mutableIntStateOf(0)
        val floatState = mutableFloatStateOf(0f)

        val stringState2 = mutableStateOf("string2")
        val intState2 = mutableIntStateOf(100)
        val floatState2 = mutableFloatStateOf(100f)

        var count = 1
        var recomposeScope: RecomposeScope? = null

        compose {
          recomposeScope = currentRecomposeScope

          when (count) {
            1 -> {
              listOf(
                "stringState" to stringState,
                "intState" to intState,
                "floatState" to floatState,
              ).forEach { (name, state) ->
                val composable = AffectedComposable(name)
                state.registerStateObjectTracking(
                  composer = currentComposer,
                  composable = composable,
                  composableKeyName = "KeyName",
                  stateName = name,
                )
              }
            }
            2 -> {
              listOf(
                "stringState2" to stringState2,
                "intState2" to intState2,
                "floatState2" to floatState2,
              ).forEach { (name, state) ->
                val composable = AffectedComposable(name)
                state.registerStateObjectTracking(
                  composer = currentComposer,
                  composable = composable,
                  composableKeyName = "KeyName",
                  stateName = name,
                )
              }
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
            STATE(composable = AffectedComposable("stringState"), name = "stringState", previousValue = "string", newValue = "string string"),
            STATE(composable = AffectedComposable("intState"), name = "intState", previousValue = 0, newValue = 1),
            STATE(composable = AffectedComposable("floatState"), name = "floatState", previousValue = 0f, newValue = 1f),
          )
        }

        count++
        recomposeScope!!.invalidate()
        expectChanges()

        withClue("The second set of states should be tracked.") {
          log shouldContainExactlyInAnyOrder listOf(
            STATE(composable = AffectedComposable("stringState"), name = "stringState", previousValue = "string", newValue = "string string"),
            STATE(composable = AffectedComposable("stringState"), name = "stringState", previousValue = "string string", newValue = "string string string"),
            STATE(composable = AffectedComposable("intState"), name = "intState", previousValue = 0, newValue = 1),
            STATE(composable = AffectedComposable("intState"), name = "intState", previousValue = 1, newValue = 2),
            STATE(composable = AffectedComposable("floatState"), name = "floatState", previousValue = 0f, newValue = 1f),
            STATE(composable = AffectedComposable("floatState"), name = "floatState", previousValue = 1f, newValue = 2f),
            STATE(composable = AffectedComposable("stringState2"), name = "stringState2", previousValue = "string2 string2", newValue = "string2 string2 string2"),
            STATE(composable = AffectedComposable("intState2"), name = "intState2", previousValue = 101, newValue = 102),
            STATE(composable = AffectedComposable("floatState2"), name = "floatState2", previousValue = 101f, newValue = 102f),
          )
        }
      }
    }
    should("Registered callbacks are released when the composable reaches the Forgotten lifecycle") {
      compositionTest {
        var mount by mutableStateOf(true)

        val stringState = mutableStateOf("string")
        val intState = mutableIntStateOf(0)
        val floatState = mutableFloatStateOf(0f)

        @Composable
        fun Unmountable() {
          listOf(
            "stringState" to stringState,
            "intState" to intState,
            "floatState" to floatState,
          ).forEach { (name, state) ->
            val composable = AffectedComposable(name)
            state.registerStateObjectTracking(
              composer = currentComposer,
              composable = composable,
              composableKeyName = "KeyName",
              stateName = name,
            )
          }
        }

        compose {
          if (mount) Unmountable()
          SideEffect {
            stringState.value += " string"
            intState.intValue++
            floatState.floatValue++
          }
        }

        withClue("Targeted for state tracking when in Remembered state") {
          log shouldContainExactlyInAnyOrder listOf(
            STATE(composable = AffectedComposable("stringState"), name = "stringState", previousValue = "string", newValue = "string string"),
            STATE(composable = AffectedComposable("intState"), name = "intState", previousValue = 0, newValue = 1),
            STATE(composable = AffectedComposable("floatState"), name = "floatState", previousValue = 0f, newValue = 1f),
          )
        }

        mount = false
        expectChanges()

        withClue("When Forgotten, all registered states tracking targets in that group are cleared.") {
          log shouldContainExactlyInAnyOrder listOf(
            STATE(composable = AffectedComposable("stringState"), name = "stringState", previousValue = "string", newValue = "string string"),
            STATE(composable = AffectedComposable("intState"), name = "intState", previousValue = 0, newValue = 1),
            STATE(composable = AffectedComposable("floatState"), name = "floatState", previousValue = 0f, newValue = 1f),
          )
        }
      }
    }
  }
}
