/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

private val DEFAULT_KEY = arrayOf(Unit)

/**
 * Register [ComposableInvalidationListener]s and remove them to
 * match the lifecycle of the Composable. The lifecycle algorithm
 * is the same as [LaunchedEffect].
 *
 * @param table The [ComposableInvalidationTrackTable] to register the
 * listener. You can get it as [currentComposableInvalidationTracker].
 * @param composableKey A unique key for the current composable. You can
 * get it as [ComposableInvalidationTrackTable.currentComposableKeyName].
 * @param keys The keys to be used to determine whether the listener should
 * be re-registered. Same as [LaunchedEffect]'s `key` parameter.
 * @param block The [listener][ComposableInvalidationListener] to be
 * registered and managed.
 */
@Composable
@NonRestartableComposable
@ExperimentalComposeInvestigatorApi
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

  override fun onRemembered() {
    job?.cancel("Old job was still running!")
    job = scope.launch {
      invalidationTrackTable.registerListener(composableKey, task.invoke(this))
    }
  }

  override fun onForgotten() {
    job?.cancel(LeftCompositionCancellationException())
    job = null
    invalidationTrackTable.unregisterListener(composableKey)
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
