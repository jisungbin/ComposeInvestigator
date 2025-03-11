// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.runtime

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.NoLiveLiterals
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshots.StateObject

/** Returns the [ComposeInvestigator] assigned to the current file. */
public val currentComposeInvestigator: ComposeInvestigator
  @[Stable JvmSynthetic] get() = intrinsicImplementedError()

/**
 * Classes that hold data from ComposeInvestigator.
 *
 * **This class is created as a singleton for *every* file**, so be careful in production
 * environments. (ComposeInvestigator is not recommended for production)
 *
 * This class is automatically generated and managed at the compiler level in ComposeInvestigator,
 * and you should be very careful about controlling this instance directly.
 *
 * To get the instance of [ComposeInvestigator] created in the current file,
 * use [currentComposeInvestigator].
 *
 * If a file is annotated with [NoInvestigation] or do not have any Composables, this class will
 * not be instantiated in that file. If you use this class's APIs (including the
 * [currentComposeInvestigator]) without being instantiated, you will receive a
 * unexpected behaviour.
 */
@Immutable public class ComposeInvestigator @ComposeInvestigatorCompilerApi public constructor() {
  private val stateObjectMap: MutableMap<StateObject, String> = mutableMapOf()
  private val argumentMap: MutableMap<Int, List<ValueArgument>> = mutableMapOf()
  private val nameMap: MutableMap<Int, String> = mutableMapOf()

  @NoLiveLiterals @ComposableScope
  @Stable public fun setComposableName(name: String, compoundKey: Int = intrinsicImplementedError()) {
    nameMap[compoundKey] = name
  }

  @NoLiveLiterals @ComposableScope
  @Stable public fun getComposableName(
    compoundKey: Int = intrinsicImplementedError(),
    default: String = intrinsicImplementedError(),
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

  public companion object {
    public const val LOGGER_DEFAULT_TAG: String = "ComposeInvestigator"

    /**
     * This logger is called whenever a recomposition is processed or skipped. This field
     * is variable, so you can easily change this.
     */
    public var logger: InvalidationLogger = InvalidationLogger { composable, result ->
      println("[$LOGGER_DEFAULT_TAG] The '${composable.simpleName}' composable has been recomposed.\n$result")
    }
  }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun intrinsicImplementedError(): Nothing =
  throw NotImplementedError("Implemented as an intrinsic. Did you apply ComposeInvestigator plugin?")
