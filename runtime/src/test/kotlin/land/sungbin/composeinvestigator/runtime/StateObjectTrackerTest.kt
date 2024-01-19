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
