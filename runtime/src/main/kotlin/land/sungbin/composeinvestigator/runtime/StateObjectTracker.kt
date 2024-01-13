/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

import androidx.compose.runtime.State
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
public fun <S : State<*>> S.registerStateObjectTracking(
  composable: AffectedComposable,
  composableKeyName: String,
  stateName: String,
  stateValueGetter: StateValueGetter = ComposeStateObjectValueGetter,
): S = also {
  require(this is StateObject) { "State must be implemented as a StateObject. (${this::class.java.name})" }

  StateObjectTrackManager.ensureStarted()

  trackedStateObjects.getOrPut(composableKeyName, ::mutableSetOf).add(this)
  stateFieldNameMap.putIfAbsent(this, stateName)
  stateValueGetterMap.putIfAbsent(this, stateValueGetter)
  stateLocationMap.putIfAbsent(this, composable)
  ComposeStateObjectValueGetter.initialize(this)

  // TODO: Execute the logic below when the composable is destroyed. We can delegate the
  //  'forgetten' call to the Compose runtime by remembering RememberObserver, but remember
  //  is implemented by the Compose compiler. Since ComposeInvestigator runs after Compose
  //  has finished compiling, we can't delegate to the Compose compiler.
  // trackedStateObjects.getOrDefault(keyName, emptySet()).forEach { stateObject ->
  //   stateFieldNameMap.remove(stateObject)
  //   stateValueGetterMap.remove(stateObject)
  //   stateLocationMap.remove(stateObject)
  //   ComposeStateObjectValueGetter.clean(stateObject)
  // }
  // trackedStateObjects.remove(keyName)
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

  @Suppress("unused")
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
