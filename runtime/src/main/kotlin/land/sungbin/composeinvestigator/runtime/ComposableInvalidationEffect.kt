/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.remember
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

private val DEFAULT_KEY = arrayOf(Unit)

@Composable
@NonRestartableComposable
public fun ComposableInvalidationEffect(
  table: ComposableInvalidationTrackTable,
  composableKey: String,
  vararg keys: Any?,
  block: suspend CoroutineScope.() -> ComposableInvalidationListener,
) {
  val applyContext = currentComposer.applyCoroutineContext
  val finalKeys = keys.ifEmpty { DEFAULT_KEY }

  remember(keys = finalKeys) {
    InvalidationEffectScope(
      parentCoroutineContext = applyContext,
      composableKey = composableKey,
      invalidationTrackTable = table,
      task = block,
    )
  }
}

private class InvalidationEffectScope(
  parentCoroutineContext: CoroutineContext,
  private val composableKey: String,
  private val invalidationTrackTable: ComposableInvalidationTrackTable,
  private val task: suspend CoroutineScope.() -> ComposableInvalidationListener,
) : RememberObserver {
  private val scope = CoroutineScope(parentCoroutineContext)
  private var job: Job? = null
  private var listener: ComposableInvalidationListener? = null

  override fun onRemembered() {
    job?.cancel("Old job was still running!")
    job = scope.launch {
      listener = task.invoke(this).also { listener ->
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
