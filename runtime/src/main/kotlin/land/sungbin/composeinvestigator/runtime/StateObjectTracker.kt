/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

import androidx.compose.runtime.Composer
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.State
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
import org.jetbrains.annotations.TestOnly
import java.util.concurrent.atomic.AtomicBoolean

public data class StateValue(val previousValue: Any?, val newValue: Any?)

public fun interface StateValueGetter {
  public operator fun invoke(target: StateObject): StateValue
}

@Immutable
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

public fun <S : State<*>> ComposableKeyInfo.registerStateObjectTracking(
  composer: Composer,
  composable: AffectedComposable,
  stateValueGetter: StateValueGetter = ComposeStateObjectValueGetter,
  stateObjectField: Pair<String, S>, // first is field name
): S {
  require(stateObjectField.second is StateObject) {
    "The second value of the stateObjectField must be implemented as a StateObject."
  }

  StateObjectTrackManager.ensureStarted()

  val fieldName = stateObjectField.first
  val stateObject = stateObjectField.second as StateObject

  trackedStateObjects.getOrPut(keyName, ::mutableSetOf).add(stateObject)
  stateFieldNameMap.putIfAbsent(stateObject, fieldName)
  stateValueGetterMap.putIfAbsent(stateObject, stateValueGetter)
  stateLocationMap.putIfAbsent(stateObject, composable)
  ComposeStateObjectValueGetter.initialize(stateObject)

  val register by lazy {
    object : RememberObserver {
      override fun onRemembered() {}

      override fun onForgotten() {
        trackedStateObjects.getOrDefault(keyName, emptySet()).forEach { stateObject ->
          stateFieldNameMap.remove(stateObject)
          stateValueGetterMap.remove(stateObject)
          stateLocationMap.remove(stateObject)
          ComposeStateObjectValueGetter.clean(stateObject)
        }
        trackedStateObjects.remove(keyName)
      }

      override fun onAbandoned() {
        // Nothing to do as [onRemembered] was not called.
      }
    }
  }

  composer.startReplaceableGroup(keyName.hashCode())
  composer.cache(composer.changed(keyName)) { register }
  composer.endReplaceableGroup()

  return stateObjectField.second
}

public object ComposeStateObjectValueGetter : StateValueGetter {
  private val stateValueMap = mutableMapOf<StateObject, StateValue>()

  private fun StateObject.getCurrentValue() = (this as SnapshotMutableState<*>).value

  internal fun initialize(key: StateObject) {
    stateValueMap.putIfAbsent(
      key,
      StateValue(
        previousValue = object {
          override fun toString() = "STATE_NO_VALUE"
        },
        newValue = key.getCurrentValue(),
      ),
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
