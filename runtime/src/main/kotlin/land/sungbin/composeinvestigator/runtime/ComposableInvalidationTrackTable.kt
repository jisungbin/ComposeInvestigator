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

/** Returns the [ComposableInvalidationTrackTable] assigned to the current file. */
@ExperimentalComposeInvestigatorApi
public val currentComposableInvalidationTracker: ComposableInvalidationTrackTable
  // TODO: Should we make this a Composable function?
  // @[Composable ExplicitGroupsComposable]
  get() {
    throw NotImplementedError("Implemented as an intrinsic")
  }

/** A callback that is called whenever a composable is invalidated (recomposed) */
public fun interface ComposableInvalidationListener {
  public fun onInvalidate(composable: AffectedComposable, type: ComposableInvalidationType)
}

// We use an annotation class to prevent LiveLiteral transform from the Compose compiler.
/**
 * Returns the name of the current composable.
 * See [ComposableInvalidationTrackTable.currentComposableName].
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
 * This class is automatically generated and managed at the compiler level in
 * ComposeInvestigator, and you should be very careful about controlling this
 * instance directly.
 *
 * To get the instance of [ComposableInvalidationTrackTable] created in the current file,
 * use [currentComposableInvalidationTracker].
 *
 * If a file is annotated with [NoInvestigation], this class will not be instantiated in
 * that file. If you use this class's APIs, including the [currentComposableInvalidationTracker]
 * API, without being instantiated, you will receive a runtime error.
 */
@ExperimentalComposeInvestigatorApi
@Immutable
public class ComposableInvalidationTrackTable @ComposeInvestigatorCompilerApi public constructor() {
  private val listeners: MutableMap<String, ComposableInvalidationListener> = mutableMapOf()
  private val affectFieldMap: MutableMap<String, List<AffectedField>> = mutableMapOf()

  /**
   * Returns all fields that were affected by the value change.
   * This is useful for debugging and testing purposes.
   *
   * Provide a [Map] consisting of a [unique key][currentComposableKeyName]
   * for the affected composables, and a list of [AffectedField]s.
   */
  public val affectFields: Map<String, List<AffectedField>> get() = affectFieldMap

  /**
   * Returns the name of the current composable, or you can define
   * your own composable name to use inside ComposeInvestigator.
   *
   * ```
   * @Composable fun MyComposable() {
   *   val table = currentComposableInvalidationTracker
   *
   *   val prevName by table.currentComposableName
   *   assertEquals(prevName, "MyComposable") // pass
   *
   *   table.currentComposableName = ComposableName("AwesomeComposable")
   *   val newName by table.currentComposableName
   *   assertEquals(newName, "AwesomeComposable") // pass
   * }
   * ```
   *
   * This is used as the value of [name][AffectedComposable.name] in
   * [AffectedComposable].
   *
   * If you call this from a composable that is configured as an
   * anonymous function, it will always default to 'anonymous',
   * so it is recommended that you specify your own composable name.
   */
  public var currentComposableName: ComposableName
    get() {
      throw NotImplementedError("Implemented as an intrinsic")
    }
    set(@Suppress("UNUSED_PARAMETER") name) {
      throw NotImplementedError("Implemented as an intrinsic")
    }

  /**
   * Returns a unique key for the current composable. This is
   * guaranteed to be the unique of the composable function in all cases.
   *
   * Unless the code changes, this key should be the same when recompiled.
   *
   * The unique key generation algorithm follows the
   * [Compose Compiler's implementation](https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/compiler/compiler-hosted/src/main/java/androidx/compose/compiler/plugins/kotlin/lower/DurableKeyTransformer.kt;l=70;drc=a963dd9408ae1cffa4adac6b8a194bc22d54d5d0)
   * of the AOSP standard.
   */
  public val currentComposableKeyName: String
    get() {
      throw NotImplementedError("Implemented as an intrinsic")
    }

  /**
   * Removes all records that were affected by the value change.
   * This means that it also resets what information was held in the past.
   *
   * This can be used in a test environment to clear the results of
   * previous test functions. (`@TestOnly`)
   */
  @TestOnly
  public fun resetAffectFields() {
    affectFieldMap.clear()
  }

  /**
   * This can be used when you need to manually add the
   * [ComposableInvalidationListener] callback. Later,
   * if the registered callback is no longer valid, you
   * should call [unregisterListener].
   *
   * It is not recommended to register directly this way;
   * consider the [ComposableInvalidationEffect] API instead.
   *
   * @param keyName The key of the composable to register the callback for.
   * See [currentComposableKeyName].
   */
  public fun registerListener(keyName: String, listener: ComposableInvalidationListener) {
    listeners.putIfNotPresent(keyName, listener)
  }

  /**
   * Removes callbacks registered with [registerListener].
   *
   * @param keyName The key of the composable to remove the callback for.
   * See [currentComposableKeyName].
   */
  public fun unregisterListener(keyName: String) {
    listeners.remove(keyName)
  }

  /** @suppress ComposeInvestigator compiler-only API */
  @ComposeInvestigatorCompilerApi
  public fun callListeners(keyName: String, composable: AffectedComposable, type: ComposableInvalidationType) {
    listeners[keyName]?.onInvalidate(composable, type)
  }

  /** @suppress ComposeInvestigator compiler-only API */
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
