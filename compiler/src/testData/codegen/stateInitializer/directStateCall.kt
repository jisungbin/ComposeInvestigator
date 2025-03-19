import androidx.compose.runtime.saveable.rememberSaveable

@Composable fun MyComposable() {
  val a: MutableState<String> = mutableStateOf("")
  val b: MutableState<String> = remember { mutableStateOf("") }
  val c: MutableState<String> = rememberSaveable { mutableStateOf("") }
}
