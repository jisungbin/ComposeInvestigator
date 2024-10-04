/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

import androidx.compose.runtime.Composer
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationTraceTable.AffectedName
import org.jetbrains.annotations.TestOnly

/** Returns the [ComposableInvalidationTraceTable] assigned to the current file. */
public val currentComposableInvalidationTracer: ComposableInvalidationTraceTable
  @[Stable JvmSynthetic] get() = throw IntrinsicImplementedError()

/**
 * Returns the name of the current Composable.
 * See [ComposableInvalidationTraceTable.currentComposableName].
 *
 * You can get [name] directly from the property delegation.
 * See [ComposableName.getValue].
 */
@MustBeDocumented
@Target() // We use an annotation class to prevent LiveLiteral transform from the Compose compiler.
@Retention(AnnotationRetention.SOURCE)
public annotation class ComposableName(public val name: String)

/**
 * `val name: String by ComposableName("MyComposable") // result: "MyComposable"`
 *
 * @see ComposableName
 */
@Suppress("unused")
public operator fun ComposableName.getValue(thisRef: Any?, property: Any?): String = name

/**
 * Classes that hold data from ComposeInvestigator.
 *
 * **This class is created as a singleton for *every* file**, so be careful in production
 * environments. (ComposeInvestigator is not recommended for production)
 *
 * This class is automatically generated and managed at the compiler level in ComposeInvestigator,
 * and you should be very careful about controlling this instance directly.
 *
 * To get the instance of [ComposableInvalidationTraceTable] created in the current file,
 * use [currentComposableInvalidationTracer].
 *
 * If a file is annotated with [NoInvestigation] or do not have any Composables, this class will
 * not be instantiated in that file. If you use this class's APIs (including the
 * [currentComposableInvalidationTracer]) without being instantiated, you will receive a
 * unexpected behaviour.
 */
@Immutable
public class ComposableInvalidationTraceTable @ComposeInvestigatorCompilerApi public constructor() {
  /**
   * Represents the unique key of the affected Composable and the compound key.
   *
   * @property keyName Same as [currentComposableKeyName]
   * @property compoundKey Same as [Composer.compoundKeyHash]
   */
  @JvmInline public value class AffectedName private constructor(private val value: String) {
    public val keyName: String get() = value.substringBeforeLast('@')
    public val compoundKey: Int get() = value.substringAfterLast('@').toInt()

    @TestOnly public constructor(keyName: String, compoundKey: Int) : this("$keyName@$compoundKey")
  }

  private val stateObjectMap: MutableMap<Any, String> = mutableMapOf()
  private val affectedArgumentMap: MutableMap<AffectedName, List<ValueArgument>> = mutableMapOf()

  /**
   * Returns the name of the current Composable, or you can define your own Composable name to use for
   * ComposeInvestigator.
   *
   * ```
   * @Composable fun MyComposable() {
   *   val table = currentComposableInvalidationTracer
   *
   *   val originalName = table.currentComposableName.name
   *   assertEquals(originalName, "MyComposable") // Ok
   *
   *   table.currentComposableName = ComposableName("AwesomeComposable")
   *   val newName = table.currentComposableName.name
   *   assertEquals(newName, "AwesomeComposable") // Ok
   * }
   * ```
   *
   * This is used as the value of [name][ComposableInformation.name] in [ComposableInformation].
   *
   * If you call this getter from a Composable configured as an anonymous function, it will always
   * be named 'anonymous.' Therefore, in this case, we recommend you specify your Composable name.
   */
  public var currentComposableName: ComposableName
    @[Stable ComposableScope JvmSynthetic] get() {
      throw IntrinsicImplementedError()
    }
    @[ComposableScope JvmSynthetic] set(@Suppress("unused") name) {
      throw IntrinsicImplementedError()
    }

  /**
   * Returns a unique key for the current Composable. This is guaranteed to be the unique of the
   * Composable function in all cases.
   *
   * Unless the code changes, this key should be the same when recompiled.
   *
   * The unique key generation algorithm uses the
   * [Compose compiler's implementation](https://github.com/JetBrains/kotlin/blob/ede0373c4e5c0506b1491c6eb4c8bc0660ef7d21/plugins/compose/compiler-hosted/src/main/java/androidx/compose/compiler/plugins/kotlin/lower/DurableKeyTransformer.kt#L74)
   * of the AOSP.
   */
  public val currentComposableKeyName: String
    @[Stable ComposableScope JvmSynthetic] get() = throw IntrinsicImplementedError()

  /**
   * Returns all arguments that were affected by the value change. This is useful for debugging and
   * testing purposes.
   *
   * Provide a [Map] consisting of a [unique affected name][AffectedName] for the affected Composables,
   * and a list of [ChangedArgument]s.
   */
  public val affectedArguments: Map<AffectedName, List<ValueArgument>>
    @Stable get() = affectedArgumentMap

  /**
   * Removes all arguments that were affected by the value change. This means that it also resets
   * what information was held in the past.
   *
   * This can be used in a test environment to clear the results of previous test functions. (`@TestOnly`)
   */
  @TestOnly public fun reset() {
    affectedArgumentMap.clear()
  }

  /**
   * Considers the given [value] to be a `StateObject` and returns the field name of the [value]
   * found by ComposeInvestigator. If not found, returns `null`.
   */
  // TODO is this operation really O(1)?
  //  When become clear, add this note into KDoc: *Note: This operation takes `O(1)`*.
  public fun findStateObjectName(value: Any): String? = stateObjectMap[value]

  /** @suppress ComposeInvestigator compiler-only API */
  @ComposeInvestigatorCompilerApi
  public fun <T : Any> registerStateObject(value: T, name: String): T =
    value.apply { stateObjectMap[this] = name }

  /** @suppress ComposeInvestigator compiler-only API */
  @ComposeInvestigatorCompilerApi
  public fun computeInvalidationReason(keyName: String, compoundKey: Int, arguments: List<ValueArgument>): InvalidationResult {
    val affectedName = AffectedName(keyName, compoundKey)

    val previousArguments = affectedArgumentMap[affectedName]
    val changed = ArrayList<ChangedArgument>(arguments.size)

    if (previousArguments == null) {
      affectedArgumentMap[affectedName] = arguments
      return InvalidationResult.InitialComposition
    }

    for (index in previousArguments.indices) {
      val previous = previousArguments[index]
      val new = arguments[index]

      check(previous.name == new.name) { "Argument name must be same. previous=${previous.name}, new=${new.name}" }
      if (previous.valueHashCode != new.valueHashCode) changed.add(previous changedTo new)
    }

    affectedArgumentMap[affectedName] = arguments

    return if (@Suppress("UsePropertyAccessSyntax") changed.isEmpty()) {
      InvalidationResult.Recomposition
    } else {
      InvalidationResult.ArgumentChanged(changed = changed)
    }
  }
}

@Suppress("FunctionName", "NOTHING_TO_INLINE")
private inline fun IntrinsicImplementedError() =
  NotImplementedError("Implemented as an intrinsic. Did you apply ComposeInvestigator plugin?")
