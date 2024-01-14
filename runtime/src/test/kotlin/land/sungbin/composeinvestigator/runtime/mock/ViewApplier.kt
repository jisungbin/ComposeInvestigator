// COPIED FROM https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/runtime/runtime/src/nonEmulatorCommonTest/kotlin/androidx/compose/runtime/mock/ViewApplier.kt;drc=6ab6b818afbb774083b126c73e7737e5af5b720a.

/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime.mock

import androidx.compose.runtime.AbstractApplier

class ViewApplier(root: View) : AbstractApplier<View>(root) {
  var onBeginChangesCalled = 0
    private set

  var onEndChangesCalled = 0
    private set

  override fun insertTopDown(index: Int, instance: View) {
    // Ignored as the tree is built bottom-up.
  }

  override fun insertBottomUp(index: Int, instance: View) {
    current.addAt(index, instance)
  }

  override fun remove(index: Int, count: Int) {
    current.removeAt(index, count)
  }

  override fun move(from: Int, to: Int, count: Int) {
    current.moveAt(from, to, count)
  }

  override fun onClear() {
    root.removeAllChildren()
  }

  override fun onBeginChanges() {
    onBeginChangesCalled++
  }

  override fun onEndChanges() {
    onEndChangesCalled++
  }
}
