/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.Transition
import androidx.compose.runtime.Composer
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.cache
import androidx.compose.runtime.snapshots.ObserverHandle
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotMutableState
import androidx.compose.runtime.snapshots.StateObject
import land.sungbin.composeinvestigator.runtime.StateObjectTrackManager.stateFieldNameMap
import land.sungbin.composeinvestigator.runtime.StateObjectTrackManager.stateLocationMap
import land.sungbin.composeinvestigator.runtime.StateObjectTrackManager.stateValueGetterMap
import land.sungbin.composeinvestigator.runtime.StateObjectTrackManager.trackedStateObjects
import land.sungbin.composeinvestigator.runtime.affect.AffectedComposable
import land.sungbin.composeinvestigator.runtime.util.putIfNotPresent
import org.jetbrains.annotations.TestOnly
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

public data class StateValue(val previousValue: Any?, val newValue: Any?)

public fun interface StateValueGetter {
  public operator fun invoke(target: StateObject): StateValue
}

public fun interface StateChangedListener {
  public fun onChanged(composable: AffectedComposable, stateName: String, previousValue: Any?, newValue: Any?)
}

internal object StateObjectTrackManager {
  private val started = AtomicBoolean(false)
  private var previousHandle: ObserverHandle? = null

  internal val trackedStateObjects = mutableMapOf<String, MutableSet<StateObject>>()

  internal val stateFieldNameMap = mutableMapOf<StateObject, String>()
  internal val stateValueGetterMap = mutableMapOf<StateObject, StateValueGetter>()
  internal val stateLocationMap = mutableMapOf<StateObject, AffectedComposable>()

  @TestOnly
  internal fun clear() {
    started.set(false)
    previousHandle?.dispose()

    trackedStateObjects.clear()
    stateFieldNameMap.clear()
    stateValueGetterMap.clear()
    stateLocationMap.clear()
  }

  internal fun ensureStarted() {
    if (started.compareAndSet(false, true)) {
      previousHandle = Snapshot.registerApplyObserver { stateObjects, _ ->
        stateObjects.forEach loop@{ stateObject ->
          if (stateObject !is StateObject) return@loop

          val name = stateFieldNameMap[stateObject] ?: return@loop
          val value = stateValueGetterMap[stateObject]?.invoke(stateObject) ?: return@loop
          val location = stateLocationMap[stateObject] ?: return@loop

          ComposeInvestigatorConfig.stateChangedListener.onChanged(
            stateName = name,
            composable = location,
            previousValue = value.previousValue,
            newValue = value.newValue,
          )
        }
      }
    }
  }
}

@ExperimentalComposeInvestigatorApi
public fun <State> State.registerStateObjectTracking(
  composer: Composer,
  composable: AffectedComposable,
  composableKeyName: String,
  stateName: String,
  stateValueGetter: StateValueGetter = ComposeStateObjectValueGetter,
): State = also {
  val state = this ?: return@also
  val register by lazy {
    val stateObject = when (state) {
      is StateObject -> state
      is Animatable<*, *> -> {
        val internalStateField = state::class.java.declaredFields.firstOrNull { field ->
          field.type == AnimationState::class.java
        }?.apply {
          isAccessible = true
        }
        val animationState = internalStateField?.get(this) as? AnimationState<*, *>
        animationState?.let { state -> state::value.obtainStateObjectOrNull() }
      }
      is AnimationState<*, *> -> state::value.obtainStateObjectOrNull()
      is Transition<*>.TransitionAnimationState<*, *> -> state::value.obtainStateObjectOrNull()
      is InfiniteTransition.TransitionAnimationState<*, *> -> state::value.obtainStateObjectOrNull()
      // Throwing here is reported a bug in the Compose runtime, so we replace it with null to avoid confusing developers.
      else -> null /* error("Unsupported state type: ${state::class.java}") */
    }

    object : RememberObserver {
      override fun onRemembered() {
        stateObject ?: return
        trackedStateObjects.getOrPut(composableKeyName, ::mutableSetOf).add(stateObject)
        stateFieldNameMap.putIfNotPresent(stateObject, stateName)
        stateValueGetterMap.putIfNotPresent(stateObject, stateValueGetter)
        stateLocationMap.putIfNotPresent(stateObject, composable)
        ComposeStateObjectValueGetter.initialize(stateObject)
      }

      override fun onForgotten() {
        // getOrDefault is available from API 24 (project minSdk is 21)
        trackedStateObjects[composableKeyName].orEmpty().forEach { state ->
          stateFieldNameMap.remove(state)
          stateValueGetterMap.remove(state)
          stateLocationMap.remove(state)
          ComposeStateObjectValueGetter.clean(state)
        }
        trackedStateObjects.remove(composableKeyName)
      }

      override fun onAbandoned() {}
    }
  }

  StateObjectTrackManager.ensureStarted()

  composer.startReplaceableGroup(composable.fqName.hashCode() + state.hashCode() + stateName.hashCode())
  composer.cache(false) { register }
  composer.endReplaceableGroup()
}

private fun KProperty0<*>.obtainStateObjectOrNull() = runCatching {
  val stateValue = apply { isAccessible = true }.getDelegate()
  stateValue as? StateObject
}.getOrNull()

public object ComposeStateObjectValueGetter : StateValueGetter {
  private val STATE_NO_VALUE = object {
    override fun toString() = "STATE_NO_VALUE"
  }
  private val stateValueMap = mutableMapOf<StateObject, StateValue>()

  // This may be a pointless defense, but we disable read observers for reliability.
  private fun StateObject.getCurrentValue() = Snapshot.withoutReadObservation {
    (this as SnapshotMutableState<*>).value
  }

  internal fun initialize(key: StateObject) {
    stateValueMap.putIfNotPresent(
      key,
      StateValue(previousValue = STATE_NO_VALUE, newValue = key.getCurrentValue()),
    )
  }

  internal fun clean(key: StateObject) {
    stateValueMap.remove(key)
  }

  internal fun clear() {
    stateValueMap.clear()
  }

  override fun invoke(target: StateObject): StateValue =
    StateValue(
      previousValue = stateValueMap[target]!!.newValue,
      newValue = target.getCurrentValue(),
    ).also { newValue ->
      stateValueMap[target] = newValue
    }
}
