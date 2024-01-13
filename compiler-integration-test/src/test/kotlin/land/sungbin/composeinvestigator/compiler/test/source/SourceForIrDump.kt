@file:Suppress("TestFunctionName", "ComposableNaming", "unused")

package land.sungbin.composeinvestigator.compiler.test.source

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember

@Composable
fun MsProducer() {
  val ms = remember { mutableLongStateOf(System.currentTimeMillis()) }
  Content(ms)
}

@Composable
private fun Content(ms: MutableLongState) {
  Button(onClick = { ms.longValue = System.currentTimeMillis() }) {}
  Display(text = ms.longValue.toString())
}

@Composable
private fun Display(text: String) {
  Text(text = text)
}
