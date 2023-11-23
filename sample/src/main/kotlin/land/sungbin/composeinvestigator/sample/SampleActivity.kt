/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import land.sungbin.composeinvestigator.runtime.AffectedComposable
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationType
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationLogger

@Suppress("unused")
@ComposableInvalidationLogger
fun composeInvestigateLogger(composable: AffectedComposable, type: ComposableInvalidationType) {
  println("<$composable> $type")
}

class SampleActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      Entry()
    }
  }
}

@Composable
private fun Entry() {
  var currentMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

  Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(
      space = 10.dp,
      alignment = Alignment.CenterVertically,
    ),
  ) {
    Text(text = System.currentTimeMillis().toString())
    Button(onClick = { currentMillis = System.currentTimeMillis() }) {
      Text(text = "Update current time")
    }
    TimeDisplay(currentMillis)
  }
}

@Composable
fun TimeDisplay(milliseconds: Long) {
  Text(text = "Current time: $milliseconds")
}
