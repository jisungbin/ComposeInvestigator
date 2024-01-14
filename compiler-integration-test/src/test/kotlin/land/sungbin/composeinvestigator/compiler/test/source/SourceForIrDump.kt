@file:Suppress("TestFunctionName", "ComposableNaming", "unused")

package land.sungbin.composeinvestigator.compiler.test.source

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun InvalidationProcessedRoot_StateDelegateReference() {
  Column {
    var delegateState by remember { mutableIntStateOf(0) }
    Button(
      modifier = Modifier.testTag("InvalidationProcessedRoot_StateDelegateReference"),
      onClick = { delegateState++ },
      content = {},
    )
    InvalidationProcessedChild_StateDelegateReference(delegateState)
  }
}

@Composable
private fun InvalidationProcessedChild_StateDelegateReference(delegateCount: Int) {
  Column {
    Text(text = "$delegateCount")
    InvalidationProcessedChild_NestedStateDelegateReferenceProvider()
  }
}

@Composable
private fun InvalidationProcessedChild_NestedStateDelegateReferenceProvider() {
  Column {
    var nestedDelegateState by remember { mutableStateOf("state") }
    Button(
      modifier = Modifier.testTag("InvalidationProcessedChild_NestedStateDelegateReferenceProvider"),
      onClick = { nestedDelegateState += " state" },
      content = {},
    )
    InvalidationProcessedChild_NestedStateDelegateReferenceConsumer(nestedDelegateState)
  }
}

@Composable
private fun InvalidationProcessedChild_NestedStateDelegateReferenceConsumer(nestedDelegateString: String) {
  Text(text = nestedDelegateString)
}
