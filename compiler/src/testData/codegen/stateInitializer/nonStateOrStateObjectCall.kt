// INVESTIGATOR_FEATURES: StateInitializerTracking

import androidx.compose.runtime.saveable.rememberSaveable

@Composable fun MyComposable() {
  val a: Any = 1
  val b: Any = remember { Any() }
  val c: Any = rememberSaveable { Unit }
}
