/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("TestFunctionName", "UNUSED_VARIABLE", "unused")
@file:OptIn(ExperimentalTransitionApi::class)

package land.sungbin.composeinvestigator.compiler.test._source.codegen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.rememberTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import land.sungbin.composeinvestigator.runtime.NoInvestigation

@Suppress("UnrememberedMutableState", "UnrememberedAnimatable")
@Composable
private fun UnrememberedStates() {
  @NoInvestigation val state = object : State<Int> {
    override val value: Int = 0
  }

  @NoInvestigation val mutableState = mutableStateOf(0)

  @NoInvestigation val animatable = Animatable(0f)

  @NoInvestigation val animationState = AnimationState(
    typeConverter = Float.VectorConverter,
    initialValue = 0f,
  )
}

@Suppress("UnrememberedMutableState", "UnrememberedAnimatable")
@Composable
private fun UnrememberedStatesDelegation() {
  @NoInvestigation val state by object : State<Int> {
    override val value: Int = 0
  }

  @NoInvestigation val mutableState by mutableStateOf(0)

  @NoInvestigation val animationState by AnimationState(
    typeConverter = Float.VectorConverter,
    initialValue = 0f,
  )
}

@Composable
private fun RememberedStates() {
  @NoInvestigation val state = remember {
    object : State<Int> {
      override val value: Int = 0
    }
  }

  @NoInvestigation val mutableState = remember { mutableStateOf(0) }

  @NoInvestigation val animatable = remember { Animatable(0f) }

  @NoInvestigation val animationState = remember {
    AnimationState(
      typeConverter = Float.VectorConverter,
      initialValue = 0f,
    )
  }

  @NoInvestigation val transitionAnimationState =
    rememberTransition(transitionState = MutableTransitionState(0f))
      .animateFloat(label = "unremembered") { prevState -> prevState * 2 }

  @NoInvestigation val infiniteTransition = rememberInfiniteTransition(label = "unremembered")
}

@Composable
private fun RememberedStatesDelegation() {
  @NoInvestigation val state by remember {
    object : State<Int> {
      override val value: Int = 0
    }
  }

  @NoInvestigation val mutableState by remember { mutableStateOf(0) }

  @NoInvestigation val animationState by remember {
    AnimationState(
      typeConverter = Float.VectorConverter,
      initialValue = 0f,
    )
  }

  @NoInvestigation val transitionAnimationState
    by rememberTransition(transitionState = MutableTransitionState(0f))
      .animateFloat(label = "unremembered") { prevState -> prevState * 2 }
}
