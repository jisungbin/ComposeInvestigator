import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.*

object MyState : State<Any> {
  override val value: Any get() = ""
}

object MyStateState : StateObject {
  override val firstStateRecord: StateRecord get() = TODO()
  override fun prependStateRecord(value: StateRecord) = TODO()
}

@Composable fun MyComposable() {
  var a: Int by mutableStateOf(1)
  var b: Int by remember { mutableStateOf(1) }
  var c: Int by rememberSaveable { mutableStateOf(1) }
  var d: State<Int> = mutableStateOf(1)
  var e: State<Int> = remember { mutableStateOf(1) }
  var f: State<Int> = rememberSaveable { mutableStateOf(1) }
  var g: MyState = MyState
  var h: MyState = remember { MyState }
  var i: MyState = rememberSaveable { MyState }
  var j: StateObject = MyStateState
  var k: StateObject = remember { MyStateState }
  var l: StateObject = rememberSaveable { MyStateState }
}
