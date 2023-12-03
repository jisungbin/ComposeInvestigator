@file:Suppress("TestFunctionName", "ComposableNaming", "unused")

package land.sungbin.composeinvestigator.compiler.test.source

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun InvalidationProcessedRoot_StateDelegateReference() {
  val countDirect = remember { mutableIntStateOf(0) }
  var countDelegation by remember { mutableIntStateOf(0) }
}
