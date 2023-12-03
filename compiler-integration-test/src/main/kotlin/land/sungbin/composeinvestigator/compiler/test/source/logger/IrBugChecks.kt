/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("UNUSED_VARIABLE", "CanBeVal")

package land.sungbin.composeinvestigator.compiler.test.source.logger

import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun NestedLocalStateCapture() {
  var state1 by remember { mutableStateOf("Unit") }
  Text(text = state1)

  Surface {
    val state2 = remember { mutableStateOf("Unit") }
    Text(text = state2.value)

    Surface {
      val state3 by remember { mutableStateOf("Unit") }
      Text(text = state3)
    }
  }
}
