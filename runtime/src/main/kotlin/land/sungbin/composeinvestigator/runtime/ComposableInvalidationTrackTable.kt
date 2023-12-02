/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

import androidx.compose.runtime.Immutable
import land.sungbin.composeinvestigator.runtime.affect.AffectedComposable
import land.sungbin.composeinvestigator.runtime.affect.AffectedField
import land.sungbin.composeinvestigator.runtime.affect.AffectedField.ValueParameter

public val currentComposableInvalidationTracker: ComposableInvalidationTrackTable
  get() {
    throw NotImplementedError("Implemented as an intrinsic")
  }

public fun interface ComposableInvalidationListener {
  public fun onInvalidate(composable: AffectedComposable, type: ComposableInvalidationType)
}

// We use an annotation class to prevent LiveLiteral transform from the Compose compiler.
@Target()
@Retention(AnnotationRetention.SOURCE)
public annotation class ComposableName(public val name: String)

public operator fun ComposableName.getValue(thisRef: Any?, property: Any?): String = name

@Immutable
public class ComposableInvalidationTrackTable @ComposeInvestigatorCompilerApi public constructor() {
  private val listeners: MutableMap<String, MutableSet<ComposableInvalidationListener>> = mutableMapOf()
  private val affectFieldMap: MutableMap<String, Array<AffectedField>> = mutableMapOf()

  public val affectFields: Map<String, List<AffectedField>>
    get() = affectFieldMap.mapValues { (_, value) -> value.asList() }

  public var currentComposableName: ComposableName
    get() {
      throw NotImplementedError("Implemented as an intrinsic")
    }
    set(@Suppress("UNUSED_PARAMETER") name) {
      throw NotImplementedError("Implemented as an intrinsic")
    }

  public val currentComposableKeyName: String
    get() {
      throw NotImplementedError("Implemented as an intrinsic")
    }

  public fun registerListener(keyName: String, listener: ComposableInvalidationListener) {
    listeners.getOrPut(keyName, ::mutableSetOf).add(listener)
  }

  public fun unregisterListener(keyName: String, listener: ComposableInvalidationListener) {
    if (listeners.containsKey(keyName)) {
      listeners[keyName]!!.remove(listener)
    }
  }

  @ComposeInvestigatorCompilerApi
  public fun callListeners(keyName: String, composable: AffectedComposable, type: ComposableInvalidationType) {
    for (listener in listeners[keyName].orEmpty()) {
      listener.onInvalidate(composable, type)
    }
  }

  @ComposeInvestigatorCompilerApi
  public fun computeInvalidationReason(keyName: String, vararg fields: AffectedField): InvalidationReason {
    val oldFields = affectFieldMap[keyName]
    val changed = ArrayList<ChangedFieldPair>(fields.size)

    if (oldFields == null) {
      @Suppress("UNCHECKED_CAST")
      affectFieldMap[keyName] = fields as Array<AffectedField>
      return InvalidationReason.Initial
    }

    for ((index, old) in oldFields.withIndex()) {
      val new = fields[index]
      check(old.name == new.name) { "Field name must be same. old.name=${old.name}, new.name=${new.name}" }
      if (old.valueHashCode != new.valueHashCode) changed.add(old changedTo new)
    }

    @Suppress("UNCHECKED_CAST")
    affectFieldMap[keyName] = fields as Array<AffectedField>

    return if (changed.isEmpty()) {
      val params = fields.filterIsInstance<ValueParameter>()
      InvalidationReason.Unknown(params = params.map(ValueParameter::toSimpleParameter))
    } else {
      InvalidationReason.FieldChanged(changed = changed)
    }
  }
}

private fun ValueParameter.toSimpleParameter(): SimpleParameter =
  SimpleParameter(name = name, stability = stability)
