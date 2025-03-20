// INVESTIGATOR_FEATURES: StateInitializerTracking

import androidx.compose.runtime.saveable.rememberSaveable

@Composable fun MyComposable() {
  val a: String by mutableStateOf("")
  val b: String by remember { mutableStateOf("") }
  val c: String by rememberSaveable { mutableStateOf("") }
}
