import androidx.compose.runtime.saveable.rememberSaveable

object MyState : State<Any> {
  override val value: Any get() = ""
}

@Composable fun MyComposable() {
  val a: Any by MyState
  val b: Any by remember { MyState }
  val c: Any by rememberSaveable { MyState }
}
