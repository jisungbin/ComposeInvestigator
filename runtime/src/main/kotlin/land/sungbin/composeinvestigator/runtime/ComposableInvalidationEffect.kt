/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

// This code is based on https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/runtime/runtime/src/commonMain/kotlin/androidx/compose/runtime/Effects.kt;l=281;drc=8fa982f966a52c0b72cdf4756461354914a178be.

package land.sungbin.composeinvestigator.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.remember
import java.util.concurrent.CancellationException
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

// TODO: `invalidationTrackTable.currentComposableKeyName`의 값이 ComposableInvalidationEffect 함수 자체의 키로
//  적용되어서 예상하지 않은 결과로 이어짐. 이를 해결하기 위해선 ComposableInvalidationEffect 함수도 intrinsic으로
//  만들어야 할 거 같음.
@OptIn(InternalComposeApi::class)
@Composable
@NonRestartableComposable
public fun ComposableInvalidationEffect(
  vararg keys: Any?,
  block: suspend CoroutineScope.(invalidationTrackTable: ComposableInvalidationTrackTable) -> ComposableInvalidationListener,
) {
  val applyContext = currentComposer.applyCoroutineContext
  val invalidationTrackTable = currentComposableInvalidationTracker
  val composableKey = invalidationTrackTable.currentComposableKeyName
  remember(*keys) {
    InvalidationEffectScope(
      parentCoroutineContext = applyContext,
      composableKey = composableKey,
      invalidationTrackTable = invalidationTrackTable,
      task = block,
    )
  }
}

private class InvalidationEffectScope(
  parentCoroutineContext: CoroutineContext,
  private val composableKey: String,
  private val invalidationTrackTable: ComposableInvalidationTrackTable,
  private val task: suspend CoroutineScope.(invalidationTrackTable: ComposableInvalidationTrackTable) -> ComposableInvalidationListener,
) : RememberObserver {
  private val scope = CoroutineScope(parentCoroutineContext)
  private var job: Job? = null
  private var listener: ComposableInvalidationListener? = null

  override fun onRemembered() {
    job?.cancel("Old job was still running!")
    job = scope.launch {
      listener = task.invoke(this, invalidationTrackTable).also { listener ->
        invalidationTrackTable.registerListener(composableKey, listener)
      }
    }
  }

  override fun onForgotten() {
    job?.cancel(LeftCompositionCancellationException())
    job = null
    listener?.let { listener ->
      invalidationTrackTable.unregisterListener(composableKey, listener)
    }
    listener = null
  }

  override fun onAbandoned() {
    onForgotten()
  }
}

private class LeftCompositionCancellationException : CancellationException("The coroutine scope left the composition") {
  override fun fillInStackTrace(): Throwable {
    stackTrace = emptyArray()
    return this
  }
}
