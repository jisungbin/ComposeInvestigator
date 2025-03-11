// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.sample

import android.os.Bundle
import android.util.Log
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
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationLogger
import land.sungbin.composeinvestigator.runtime.ComposableName
import land.sungbin.composeinvestigator.runtime.ComposeInvestigator
import land.sungbin.composeinvestigator.runtime.currentComposeInvestigator

private val table by lazy { currentComposeInvestigator }

class SampleActivity : ComponentActivity() {
  init {
    ComposeInvestigator.Logger = ComposableInvalidationLogger { composable, result ->
      Log.d(ComposeInvestigator.LOGGER_DEFAULT_TAG, "The '${composable.simpleName}' composable has been recomposed.\n$result")
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      Entry()
    }
  }
}

@Composable private fun Entry() {
  var currentMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

  Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(
      space = 10.dp,
      alignment = Alignment.CenterVertically,
    ),
  ) {
    table.currentComposableName = ComposableName("EntryContent")

    Text(System.currentTimeMillis().toString())
    Button(onClick = { currentMillis = System.currentTimeMillis() }) {
      table.currentComposableName = ComposableName("EntryUpdateButton")

      Text("Update current time")
    }
    TimeDisplay(currentMillis)
  }
}

@Composable private fun TimeDisplay(milliseconds: Long) {
  Text("Current time: $milliseconds")
}
