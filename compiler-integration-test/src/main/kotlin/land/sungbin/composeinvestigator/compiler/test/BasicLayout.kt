package land.sungbin.composeinvestigator.compiler.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mock.Linear
import androidx.compose.runtime.mock.Text
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracer

@Composable fun BasicLayout() {
  println(currentComposableInvalidationTracer)
  Text("Root")
  Linear {
    repeat(3) {
      Text("Child $it")
    }
  }
  Text("Tail")
}