/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

import land.sungbin.composeinvestigator.runtime.affect.AffectedComposable
import land.sungbin.composeinvestigator.runtime.affect.AffectedField

/** @see ComposeInvestigatorConfig.invalidationLogger */
public fun interface ComposableInvalidationLogger {
  public fun log(composable: AffectedComposable, type: ComposableInvalidationType)
}

/**
 * Represents information about the composable function parameters.
 *
 * @param name Parameter name
 * @param typeName fully-qualified name of the parameter type
 * @param stability Stability information for the parameter type
 */
public data class ParameterInformation(
  public val name: String,
  public val typeName: String,
  public val stability: Stability,
)

/**
 * Indicates which fields have changed as a result of the recomposition.
 *
 * @param old Field before recomposition
 * @param new Field after recomposition
 */
public data class FieldChanged(public val old: AffectedField, public val new: AffectedField) {
  init {
    require(old.javaClass.name == new.javaClass.name) {
      "AffectedField must be same type. old=${old.javaClass.name}, new=${new.javaClass.name}"
    }
  }
}

/** Create a [FieldChanged] with [this] as `old` and [new] as `new`. */
public infix fun AffectedField.changedTo(new: AffectedField): FieldChanged =
  FieldChanged(old = this, new = new)

/** Explains why the composable was recomposed. */
public sealed class InvalidationReason {
  abstract override fun toString(): String

  /** The first composition (not recomposition). This happens by default. */
  public data object Initial : InvalidationReason() {
    override fun toString(): String = "[Initial] Initial composition."
  }

  /**
   * The current recompose scope has been requested to be recomposed. This can be
   * caused by a call to `currentRecomposeScope.invalidate()` or when a field within
   * that composable has been changed.
   */
  public data object Invalidate : InvalidationReason() {
    override fun toString(): String =
      "[Invalidate] An recomposition has been requested for the current composable scope. " +
        "The state value in the body of that composable function has most likely changed."
  }

  /**
   * A field in the composable has been changed. The changed fields are print sorted by
   * field name.
   */
  public data class FieldChanged(public val changed: List<land.sungbin.composeinvestigator.runtime.FieldChanged>) : InvalidationReason() {
    override fun toString(): String = buildString {
      val sortedChanges = changed.sortedBy { field -> field.old.name }

      appendLine("[FieldChanged]")
      sortedChanges.forEachIndexed { index, (old, new) ->
        check(old.name == new.name) { "Field name must be same. old.name=${old.name}, new.name=${new.name}" }
        appendLine(
          """
          |  ${index + 1}. ${old.name}${if (old is AffectedField.ValueParameter) " <${old.stability}>" else ""}
          |    Old: ${with(old) { "$valueString ($valueHashCode)" }}
          |    New: ${with(new) { "$valueString ($valueHashCode)" }}
          """.trimMargin(),
        )
      }
    }
  }

  /**
   * @suppress According to the Compose compiler's comments this should be determinable via
   * the `$changed` argument.
   *
   * "the lowest bit of the bitmask is a special bit which forces execution of the function."
   * (https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/compiler/compiler-hosted/src/main/java/androidx/compose/compiler/plugins/kotlin/lower/ComposableFunctionBodyTransformer.kt;l=381-382;drc=ea884612191a32933b697cc5062aa32505be4eaa)
   *
   * However, I haven't yet figured out how to determine this, so this type is not used.
   * It's probably related to `androidx.compose.runtime.changedLowBitMask`. (in RecomposeScopeImpl.kt)
   */
  @Deprecated("Force reason is not supported yet.", level = DeprecationLevel.ERROR)
  public data object Force /*: InvalidationReason()*/ {
    override fun toString(): String = "[Force] A forced recomposition has been requested for the current composable."
  }

  /** The composable was recomposed, but no changes were detected by ComposeInvestigator. */
  public data class Unknown(public val params: List<ParameterInformation>) : InvalidationReason() {
    override fun toString(): String =
      "[Unknown] No parameters have changed. Perhaps the state value being referenced in the function body has changed. " +
        "If no state has changed, then some function parameter may be unstable, or a forced invalidation may have " +
        "been requested.${if (params.isNotEmpty()) "\nGiven parameters: ${params.joinToString()}" else ""}"
  }
}

/** Indicates how the composable was recomposed. */
public sealed class ComposableInvalidationType {
  /** The composable has actually been recomposed. */
  public data class Processed(public val reason: InvalidationReason) : ComposableInvalidationType()

  /** Recomposition was skipped because there were no changes to the composable. */
  public data object Skipped : ComposableInvalidationType()
}
