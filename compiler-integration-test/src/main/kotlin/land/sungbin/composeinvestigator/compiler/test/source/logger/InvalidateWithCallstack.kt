/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.source.logger

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun ComposableCallstackRoot_House() {
  var count by remember { mutableIntStateOf(0) }

  Column {
    Door(count = count)
    Window(count = count)
    Button(onClick = { count++ }) {}
  }
}

@Composable
fun ComposableCallstackRoot_DarkHouse(hasWindow: Boolean) {
  var count by remember { mutableIntStateOf(0) }

  Column {
    if (hasWindow) Window(count = count)
    Door(count = count)
    Button(onClick = { count++ }) {}
  }
}

@Composable
private fun Door(count: Int) {
  Knock(count)
}

@Composable
private fun Knock(count: Int) {
  Text("Knock knock $count times")
}

@Composable
private fun Window(count: Int) {
  Mirror(count)
}

@Composable
private fun Mirror(count: Int) {
  Spider(count)
}

@Composable
private fun Spider(count: Int) {
  Text("Spider on the mirror $count times")
}
