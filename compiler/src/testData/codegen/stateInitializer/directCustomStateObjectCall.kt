import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.*

object MyStateState : StateObject {
  override val firstStateRecord: StateRecord get() = TODO()
  override fun prependStateRecord(value: StateRecord) = TODO()
}

@Composable fun MyComposable() {
  val a: StateObject = MyStateState
  val b: StateObject = remember { MyStateState }
  val c: StateObject = rememberSaveable { MyStateState }
}
