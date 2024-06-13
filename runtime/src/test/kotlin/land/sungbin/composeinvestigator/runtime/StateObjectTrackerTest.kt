/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("TestFunctionName")

package land.sungbin.composeinvestigator.runtime

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.rememberTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.RecomposeScope
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mock.compositionTest
import androidx.compose.runtime.mock.expectChanges
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import kotlin.test.BeforeTest
import kotlin.test.Test
import land.sungbin.composeinvestigator.runtime.ComposeStateObjectValueGetter.getCurrentValue
import land.sungbin.composeinvestigator.runtime.affect.AffectedComposable

@Suppress("UnrememberedMutableState", "UnrememberedAnimatable", "TransitionPropertiesLabel", "InfiniteTransitionLabel")
@OptIn(ExperimentalTransitionApi::class)
class StateObjectTrackerTest {
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

  private val log = mutableListOf<STATE>()

  init {
    ComposeInvestigatorConfig.stateChangedListener = StateChangedListener { composable, name, previousValue, newValue ->
      log.add(STATE(composable = composable, name = name, previousValue = previousValue, newValue = newValue))
    }
  }

  @BeforeTest
  fun clear() {
    log.clear()
    StateObjectTrackManager.clear()
    ComposeStateObjectValueGetter.clear()
  }

  @Test
  fun retrieveStateValueFromStateObject() = compositionTest {
    var state: MutableState<Float>? = null
    var animatable: Animatable<Float, AnimationVector1D>? = null
    var animationState: AnimationState<Float, AnimationVector1D>? = null
    var transitionState: State<Float>? = null
    var infiniteTransition: InfiniteTransition? = null

    compose {
      state = mutableStateOf(1000f)
      animatable = Animatable(1000f)
      animationState = AnimationState(initialValue = 1000f)
      transitionState = rememberTransition(transitionState = MutableTransitionState(1000f)).animateFloat { it }
      infiniteTransition = rememberInfiniteTransition()
    }

    assertThat(ComposeStateObjectGetter(state!!)?.getCurrentValue()).isEqualTo(1000f)
    assertThat(ComposeStateObjectGetter(animatable!!)?.getCurrentValue()).isEqualTo(1000f)
    assertThat(ComposeStateObjectGetter(animationState!!)?.getCurrentValue()).isEqualTo(1000f)
    assertThat(ComposeStateObjectGetter(transitionState!!)?.getCurrentValue()).isEqualTo(1000f)
    assertThat(ComposeStateObjectGetter(infiniteTransition!!)?.getCurrentValue()).isEqualTo(null) // The initial state produces null.
  }

  @Test
  fun receiveCallbackWhenStateChanged() = compositionTest {
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

    assertThat(log).containsExactlyInAnyOrder(
      listOf(
        STATE(composable = AffectedComposable("stringState"), name = "stringState", previousValue = "string", newValue = "string string"),
        STATE(composable = AffectedComposable("intState"), name = "intState", previousValue = 0, newValue = 1),
        STATE(composable = AffectedComposable("floatState"), name = "floatState", previousValue = 0f, newValue = 1f),
      ),
    )

    recomposeScope!!.invalidate()
    expectChanges()

    assertThat(log, name = "No duplicate logs").containsExactlyInAnyOrder(
      listOf(
        STATE(composable = AffectedComposable("stringState"), name = "stringState", previousValue = "string", newValue = "string string"),
        STATE(composable = AffectedComposable("stringState"), name = "stringState", previousValue = "string string", newValue = "string string string"),
        STATE(composable = AffectedComposable("intState"), name = "intState", previousValue = 0, newValue = 1),
        STATE(composable = AffectedComposable("intState"), name = "intState", previousValue = 1, newValue = 2),
        STATE(composable = AffectedComposable("floatState"), name = "floatState", previousValue = 0f, newValue = 1f),
        STATE(composable = AffectedComposable("floatState"), name = "floatState", previousValue = 1f, newValue = 2f),
      ),
    )
  }

  // TODO changes in DerivedState are not caught by Snapshot.registerApplyObserver,
  //  so it doesn't detect when they've changed. To resolve the #123 bug, only
  //  test that no exceptions are thrown for now.
  @Test
  fun noExceptionThrownEvenDerivedStateIsPresent() = compositionTest {
    val firstNumber = mutableIntStateOf(0)
    val secondNumber = mutableLongStateOf(1000)
    val summedNumber = derivedStateOf { firstNumber.intValue + secondNumber.longValue }

    var recomposeScope: RecomposeScope? = null
    var runCount = 0

    compose {
      recomposeScope = currentRecomposeScope

      summedNumber.registerStateObjectTracking(
        composer = currentComposer,
        composable = AffectedComposable("TEST"),
        composableKeyName = "KeyName",
        stateName = "summedNumber",
      )

      SideEffect {
        firstNumber.intValue++
        if (runCount++ > 0) secondNumber.longValue++
      }
    }

    recomposeScope!!.invalidate()
    expectChanges()
  }

  @Test
  fun registeredCallbacksAreReleasedWhenComposableReachesForgottenLifecycle() = compositionTest {
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

    assertThat(log, name = "Targeted for state tracking when in Remembered state").containsExactlyInAnyOrder(
      listOf(
        STATE(composable = AffectedComposable("stringState"), name = "stringState", previousValue = "string", newValue = "string string"),
        STATE(composable = AffectedComposable("intState"), name = "intState", previousValue = 0, newValue = 1),
        STATE(composable = AffectedComposable("floatState"), name = "floatState", previousValue = 0f, newValue = 1f),
      ),
    )

    mount = false
    expectChanges()

    assertThat(log, name = "When Forgotten, all registered states tracking targets in that group are cleared.").containsExactlyInAnyOrder(
      listOf(
        STATE(composable = AffectedComposable("stringState"), name = "stringState", previousValue = "string", newValue = "string string"),
        STATE(composable = AffectedComposable("intState"), name = "intState", previousValue = 0, newValue = 1),
        STATE(composable = AffectedComposable("floatState"), name = "floatState", previousValue = 0f, newValue = 1f),
      ),
    )
  }
}
