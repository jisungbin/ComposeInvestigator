/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

import androidx.compose.runtime.Immutable
import land.sungbin.composeinvestigator.runtime.affect.AffectedComposable
import land.sungbin.composeinvestigator.runtime.affect.AffectedField
import land.sungbin.composeinvestigator.runtime.util.putIfNotPresent
import org.jetbrains.annotations.TestOnly

@ExperimentalComposeInvestigatorApi
public val currentComposableInvalidationTracker: ComposableInvalidationTrackTable
  // TODO: Should we make this a Composable function?
  // @Composable
  // @ExplicitGroupsComposable
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

@ExperimentalComposeInvestigatorApi
@Immutable
public class ComposableInvalidationTrackTable @ComposeInvestigatorCompilerApi public constructor() {
  private val listeners: MutableMap<String, ComposableInvalidationListener> = mutableMapOf()
  private val affectFieldMap: MutableMap<String, List<AffectedField>> = mutableMapOf()

  public val affectFields: Map<String, List<AffectedField>> get() = affectFieldMap

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

  @TestOnly
  public fun resetAffectFields() {
    affectFieldMap.clear()
  }

  public fun registerListener(keyName: String, listener: ComposableInvalidationListener) {
    listeners.putIfNotPresent(keyName, listener)
  }

  public fun unregisterListener(keyName: String) {
    listeners.remove(keyName)
  }

  @ComposeInvestigatorCompilerApi
  public fun callListeners(keyName: String, composable: AffectedComposable, type: ComposableInvalidationType) {
    listeners[keyName]?.onInvalidate(composable, type)
  }

  @ComposeInvestigatorCompilerApi
  public fun computeInvalidationReason(keyName: String, fields: List<AffectedField>): InvalidationReason {
    val oldFields = affectFieldMap[keyName]
    val changed = ArrayList<ChangedFieldPair>(fields.size)

    if (oldFields == null) {
      affectFieldMap[keyName] = fields
      return InvalidationReason.Initial
    }

    for (index in oldFields.indices) {
      val old = oldFields[index]
      val new = fields[index]
      check(old.name == new.name) { "Field name must be same. old.name=${old.name}, new.name=${new.name}" }
      if (old.valueHashCode != new.valueHashCode) changed.add(old changedTo new)
    }

    affectFieldMap[keyName] = fields

    return if (changed.isEmpty()) {
      val params = fields.filterIsInstance<AffectedField.ValueParameter>()
      InvalidationReason.Unknown(params = params.map(AffectedField.ValueParameter::toParameterInformation))
    } else {
      InvalidationReason.FieldChanged(changed = changed)
    }
  }
}

private fun AffectedField.ValueParameter.toParameterInformation(): ParameterInformation =
  ParameterInformation(name = name, typeFqName = typeFqName, stability = stability)
