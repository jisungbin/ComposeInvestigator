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
import org.jetbrains.annotations.TestOnly

/** Returns the [ComposableInvalidationTraceTable] assigned to the current file. */
public val currentComposableInvalidationTracer: ComposableInvalidationTraceTable
  get() {
    throw NotImplementedError("Implemented as an intrinsic")
  }

/** A callback that is called whenever a composable is recomposed. */
public fun interface ComposableInvalidationListener {
  public fun onInvalidate(composable: AffectedComposable, type: ComposableInvalidationType)
}

// We use an annotation class to prevent LiveLiteral transform from the Compose compiler.
/**
 * Returns the name of the current composable.
 * See [ComposableInvalidationTraceTable.currentComposableName].
 *
 * You can get [name] directly from the property delegation.
 * See [ComposableName.getValue].
 */
@Target()
@Retention(AnnotationRetention.SOURCE)
public annotation class ComposableName(public val name: String)

/** `val name: String by ComposableName("MyComposable")` */
public operator fun ComposableName.getValue(thisRef: Any?, property: Any?): String = name

/**
 * Classes that hold data from ComposeInvestigator.
 *
 * **This class is created as a singleton for *every* file**, so be careful in release
 * environments. (ComposeInvestigator is not recommended for production)
 *
 * This class is automatically generated and managed at the compiler level in ComposeInvestigator,
 * and you should be very careful about controlling this instance directly.
 *
 * To get the instance of [ComposableInvalidationTraceTable] created in the current file,
 * use [currentComposableInvalidationTracer].
 *
 * If a file is annotated with [NoInvestigation], this class will not be instantiated in
 * that file. If you use this class's APIs, including the [currentComposableInvalidationTracer]
 * API, without being instantiated, you will receive a runtime error.
 */
@Immutable
public class ComposableInvalidationTraceTable @ComposeInvestigatorCompilerApi public constructor() {
  private val affectFieldMap: MutableMap<String, List<AffectedField>> = mutableMapOf()

  /**
   * Returns all fields that were affected by the value change. This is useful
   * for debugging and testing purposes.
   *
   * Provide a [Map] consisting of a [unique key][currentComposableKeyName] for
   * the affected composables, and a list of [AffectedField]s.
   */
  public val affectFields: Map<String, List<AffectedField>> get() = affectFieldMap

  /**
   * Returns the name of the current composable, or you can define your own
   * composable name to use inside ComposeInvestigator.
   *
   * ```
   * @Composable fun MyComposable() {
   *   val table = currentComposableInvalidationTracer
   *
   *   val prevName = table.currentComposableName.name
   *   assertEquals(prevName, "MyComposable") // ok
   *
   *   table.currentComposableName = ComposableName("AwesomeComposable")
   *   val newName = table.currentComposableName.name
   *   assertEquals(newName, "AwesomeComposable") // ok
   * }
   * ```
   *
   * This is used as the value of [name][AffectedComposable.name] in [AffectedComposable].
   *
   * If you call this from a composable that is configured as an anonymous function,
   * it will always default to 'anonymous', so it is recommended that you specify your
   * own composable name.
   */
  public var currentComposableName: ComposableName
    get() {
      throw NotImplementedError("Implemented as an intrinsic")
    }
    set(@Suppress("UNUSED_PARAMETER") name) {
      throw NotImplementedError("Implemented as an intrinsic")
    }

  /**
   * Returns a unique key for the current composable. This is guaranteed to be
   * the unique of the composable function in all cases.
   *
   * Unless the code changes, this key should be the same when recompiled.
   *
   * The unique key generation algorithm uses the
   * [Compose compiler's implementation](https://github.com/JetBrains/kotlin/blob/ede0373c4e5c0506b1491c6eb4c8bc0660ef7d21/plugins/compose/compiler-hosted/src/main/java/androidx/compose/compiler/plugins/kotlin/lower/DurableKeyTransformer.kt#L74)
   * of the AOSP.
   */
  public val currentComposableKeyName: String
    get() {
      throw NotImplementedError("Implemented as an intrinsic")
    }

  /**
   * Removes all records that were affected by the value change. This means
   * that it also resets what information was held in the past.
   *
   * This can be used in a test environment to clear the results of previous
   * test functions. (`@TestOnly`)
   */
  @TestOnly
  public fun resetAffectFields() {
    affectFieldMap.clear()
  }

  /** @suppress ComposeInvestigator compiler-only API */
  @ComposeInvestigatorCompilerApi
  public fun computeInvalidationReason(keyName: String, fields: List<AffectedField>): InvalidationReason {
    val oldFields = affectFieldMap[keyName]
    val changed = ArrayList<FieldChanged>(fields.size)

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
  ParameterInformation(name = name, typeName = typeName, stability = stability)
