import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.StateObject

@Composable fun MyComposable() {
  val a: StateObject = mutableStateListOf("")
  val b: StateObject = remember { mutableStateListOf("") }
  val c: StateObject = rememberSaveable { mutableStateListOf("") }
}
