/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

package land.sungbin.composeinvalidator.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
  val recomposeScope = currentRecomposeScope

  Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(
      space = 10.dp,
      alignment = Alignment.CenterVertically,
    ),
  ) {
    Text(text = System.currentTimeMillis().toString())
    Button(onClick = { recomposeScope.invalidate() }) {
      Text(text = "Recompose")
    }
    NestedEntry()
  }
}

@Composable
private fun NestedEntry() {
  val recomposeScope = currentRecomposeScope

  Column(
    modifier = Modifier
      .wrapContentSize()
      .background(color = Color.Gray),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(
      space = 10.dp,
      alignment = Alignment.CenterVertically,
    ),
  ) {
    Text(text = System.currentTimeMillis().toString())
    Button(onClick = { recomposeScope.invalidate() }) {
      Text(text = "Recompose")
    }
  }
}
