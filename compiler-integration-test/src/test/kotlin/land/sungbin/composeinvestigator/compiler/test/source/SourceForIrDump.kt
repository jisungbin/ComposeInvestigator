@file:Suppress("TestFunctionName", "ComposableNaming", "unused")

package land.sungbin.composeinvestigator.compiler.test.source

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun One() {
  Button(onClick = {}) {
    Text(text = "One Text")
  }
  Two(value = 1)
}

@Composable
fun Two(value: Any) {
  Box {
    Spacer(Modifier.testTag(value.toString()))
  }
}
