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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Suppress("UnrememberedMutableState", "UnrememberedAnimatable")
@Composable
private fun UnrememberedStates() {
  val state = object : State<Int> {
    override val value: Int = 0
  }
  val mutableState = mutableStateOf(0)
  val animatable = Animatable(0f)
  val animationState = AnimationState(
    typeConverter = Float.VectorConverter,
    initialValue = 0f,
  )
}

@Composable
private fun RememberedStates() {
  val state = remember {
    object : State<Int> {
      override val value: Int = 0
    }
  }
  val mutableState = remember { mutableStateOf(0) }
  val animatable = remember { Animatable(0f) }
  val animationState = remember {
    AnimationState(
      typeConverter = Float.VectorConverter,
      initialValue = 0f,
    )
  }
  val transitionAnimationState =
    rememberTransition(transitionState = MutableTransitionState(0f))
      .animateFloat(label = "unremembered") { prevState -> prevState * 2 }
  val infiniteTransition = rememberInfiniteTransition(label = "unremembered")
}
