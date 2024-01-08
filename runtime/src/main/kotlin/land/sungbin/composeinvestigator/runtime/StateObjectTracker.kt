/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

import android.util.Log
import androidx.compose.runtime.Composer
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.cache
import androidx.compose.runtime.snapshots.ObserverHandle
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotMutableState
import androidx.compose.runtime.snapshots.StateObject
import land.sungbin.composeinvestigator.runtime.StateObjectTrackManager.stateFieldNameMap
import land.sungbin.composeinvestigator.runtime.StateObjectTrackManager.stateValueChangedListener
import land.sungbin.composeinvestigator.runtime.StateObjectTrackManager.stateValueGetterMap
import org.jetbrains.annotations.TestOnly
import java.util.concurrent.atomic.AtomicBoolean

public data class StateValue(val previousValue: Any?, val newValue: Any?)

public typealias StateValueGetter = (target: StateObject) -> StateValue

@Immutable
public fun interface StateChangedListener {
  public fun onChanged(stateName: String, previousValue: Any?, newValue: Any?)

  public companion object {
    public val DefaultLogger: StateChangedListener = StateChangedListener { name, previousValue, newValue ->
      Log.d(ComposeInvestigatorConfig.LOGGER_DEFAULT_TAG, "The '$name' state has been changed. previousValue=$previousValue, newValue=$newValue")
    }
  }
}

internal object StateObjectTrackManager {
  private val started = AtomicBoolean(false)
  private var previousHandle: ObserverHandle? = null

  internal val stateFieldNameMap = mutableMapOf<StateObject, String>()
  internal val stateValueGetterMap = mutableMapOf<StateObject, StateValueGetter>()
  internal val stateValueChangedListener = mutableMapOf<StateObject, StateChangedListener>()

  @TestOnly
  internal fun clear() {
    started.set(false)
    previousHandle?.dispose()

    stateFieldNameMap.clear()
    stateValueGetterMap.clear()
    stateValueChangedListener.clear()
  }

  internal fun ensureStarted() {
    if (started.compareAndSet(false, true)) {
      previousHandle = Snapshot.registerApplyObserver { stateObjects, _ ->
        stateObjects.forEach loop@{ stateObject ->
          if (stateObject !is StateObject) return@loop

          val name = stateFieldNameMap[stateObject] ?: return@loop
          val value = stateValueGetterMap[stateObject]?.invoke(stateObject) ?: return@loop
          val listener = stateValueChangedListener[stateObject] ?: return@loop

          listener.onChanged(stateName = name, previousValue = value.previousValue, newValue = value.newValue)
        }
      }
    }
  }
}

public fun ComposableInvalidationTrackTable.registerStateObjectTracking(
  composer: Composer,
  stateValueGetter: StateValueGetter = ComposeStateObjectValueGetter,
  listener: StateChangedListener = StateChangedListener.DefaultLogger,
  vararg stateObjectFields: Pair<String, StateObject>, // first is field name
) {
  StateObjectTrackManager.ensureStarted()

  val register by lazy {
    object : RememberObserver {
      override fun onRemembered() {
        stateObjectFields.forEach { (fieldName, stateObject) ->
          stateFieldNameMap[stateObject] = fieldName
          stateValueGetterMap[stateObject] = stateValueGetter
          stateValueChangedListener[stateObject] = listener
          ComposeStateObjectValueGetter.initialize(stateObject)
        }
      }

      override fun onForgotten() {
        stateObjectFields.forEach { (_, stateObject) ->
          stateFieldNameMap.remove(stateObject)
          stateValueGetterMap.remove(stateObject)
          stateValueChangedListener.remove(stateObject)
          ComposeStateObjectValueGetter.clean(stateObject)
        }
      }

      override fun onAbandoned() {
        // Nothing to do as [onRemembered] was not called.
      }
    }
  }

  composer.startReplaceableGroup(currentComposableKeyName.hashCode())
  composer.cache(composer.changed(currentComposableKeyName)) { register }
  composer.endReplaceableGroup()
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
