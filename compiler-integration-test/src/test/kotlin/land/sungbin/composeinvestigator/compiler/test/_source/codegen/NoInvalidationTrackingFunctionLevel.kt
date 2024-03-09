/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("TestFunctionName")

package land.sungbin.composeinvestigator.compiler.test._source.codegen

import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import land.sungbin.composeinvestigator.runtime.NoInvestigation

@Suppress("unused")
@Composable
private fun Main() {
  var state by remember { mutableIntStateOf(0) }
  Button(onClick = { state++ }) {}
  Counter(count = state)
  Tv(title = "state", value = state)
  Hello()
}

@[NoInvestigation Composable Suppress("UnrememberedMutableState")]
private fun Counter(count: Int) {
  val state = mutableIntStateOf(count)
  Text(text = state.toString())
}

@[NoInvestigation Composable Suppress("SameParameterValue")]
private fun Tv(title: String, value: Any) {
  Text(text = "[$title] $value")
}

@Composable
private fun Hello() {
  BasicText(text = "Hello, World!")
}
