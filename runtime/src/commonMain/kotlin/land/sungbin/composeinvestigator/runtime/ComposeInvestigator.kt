// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.runtime

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExplicitGroupsComposable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.NoLiveLiterals
import androidx.compose.runtime.Stable
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.snapshots.StateObject

@Immutable public object ComposeInvestigator {
  public const val LOGGER_DEFAULT_TAG: String = "ComposeInvestigator"

  /**
   * This logger is called whenever a recomposition is processed or skipped. This field
   * is variable, so you can easily change this.
   */
  public var Logger: InvalidationLogger = InvalidationLogger { composable, result ->
    println("[$LOGGER_DEFAULT_TAG] The '${composable.simpleName}' composable has been recomposed.\n$result")
  }

  private val stateObjectMap = mutableMapOf<StateObject, String>()
  private val argumentMap = mutableMapOf<Int, List<ValueArgument>>()
  private val nameMap = mutableMapOf<Int, String>()

  @[Composable ExplicitGroupsComposable NoLiveLiterals]
  @Stable public fun setCurrentComposableName(name: String) {
    nameMap[currentCompositeKeyHash] = name
  }

  @[Composable ExplicitGroupsComposable NoLiveLiterals]
  @Stable public fun getCurrentComposableName(
    default: String = throw NotImplementedError("Implemented as an intrinsic. Did you apply ComposeInvestigator plugin?"),
    compoundKey: Int = currentCompositeKeyHash,
  ): String = nameMap[compoundKey] ?: default

  @VisibleForTesting public fun reset() {
    argumentMap.clear()
    nameMap.clear()
  }

  /**
   * If the given [value] is a [StateObject] (corresponding to a [State]), returns the name of the
   * variable to which the [value] is assigned.
   *
   * ```
   * val myState = mutableStateOf(Any())
   *
   * table.findStateObjectName(myState) // result: "myState"
   * ```
   *
   * If [value] is not a [StateObject], or if the assigned variable is not found, null is returned.
   *
   * @suppress This is not yet ready for public use.
   */
  public fun findStateObjectName(value: Any): String? {
    if (value !is StateObject) return null
    return stateObjectMap[value]
  }

  // FIXME Storing the StateObject itself can potentially create a memory leak.
  //  Consider storing immutable primitives like hashCode instead of the object.
  /** @suppress ComposeInvestigator compiler-only API */
  @ComposeInvestigatorCompilerApi
  public fun <T : Any> registerStateObject(value: T, name: String): T {
    if (value !is StateObject) return value
    return value.apply { stateObjectMap[this] = name }
  }

  /** @suppress ComposeInvestigator compiler-only API */
  @ComposeInvestigatorCompilerApi
  public fun computeInvalidationReason(compoundKey: Int, arguments: List<ValueArgument>): InvalidationResult {
    val previousArguments = argumentMap[compoundKey]
    val changed = ArrayList<ChangedArgument>(arguments.size)

    if (previousArguments == null) {
      argumentMap[compoundKey] = arguments
      return InvalidationResult.InitialComposition
    }

    for (index in previousArguments.indices) {
      val previous = previousArguments[index]
      val new = arguments[index]

      check(previous.name == new.name) { "Argument name must be same. previous=${previous.name}, new=${new.name}" }
      if (previous.valueHashCode != new.valueHashCode) changed.add(previous changedTo new)
    }

    argumentMap[compoundKey] = arguments

    return if (changed.isEmpty()) {
      InvalidationResult.Recomposition
    } else {
      InvalidationResult.ArgumentChanged(changed = changed)
    }
  }
}
