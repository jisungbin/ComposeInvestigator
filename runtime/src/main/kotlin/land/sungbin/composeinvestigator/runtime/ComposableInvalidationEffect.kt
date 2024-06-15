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
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

private val DEFAULT_KEY = arrayOf(Unit)

/**
 * Register [ComposableInvalidationListener]s and remove them to
 * match the lifecycle of the Composable. The lifecycle algorithm
 * is the same as [LaunchedEffect].
 *
 * @param table The [ComposableInvalidationTraceTable] to register the
 * listener. You can get it as [currentComposableInvalidationTracer].
 * @param composableKey A unique key for the current composable. You can
 * get it as [ComposableInvalidationTraceTable.currentComposableKeyName].
 * @param keys The keys to be used to determine whether the listener should
 * be re-registered. Same as [LaunchedEffect]'s `key` parameter.
 * @param block The [listener][ComposableInvalidationListener] to be
 * registered and managed.
 */
@Composable
@NonRestartableComposable
@ExperimentalComposeInvestigatorApi
public fun ComposableInvalidationEffect(
  table: ComposableInvalidationTraceTable,
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
      invalidationTraceTable = table,
      task = block,
    )
  }
}

private class InvalidationEffectScope(
  parentCoroutineContext: CoroutineContext,
  private val composableKey: String,
  private val invalidationTraceTable: ComposableInvalidationTraceTable,
  private val task: suspend CoroutineScope.() -> ComposableInvalidationListener,
) : RememberObserver {
  private val scope = CoroutineScope(parentCoroutineContext)
  private var job: Job? = null

  override fun onRemembered() {
    job?.cancel("Old job was still running!")
    job = scope.launch {
      invalidationTraceTable.registerListener(composableKey, task.invoke(this))
    }
  }

  override fun onForgotten() {
    job?.cancel(LeftCompositionCancellationException())
    job = null
    invalidationTraceTable.unregisterListener(composableKey)
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
