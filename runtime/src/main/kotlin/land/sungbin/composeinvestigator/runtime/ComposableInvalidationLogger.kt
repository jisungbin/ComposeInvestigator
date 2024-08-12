/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

import androidx.compose.runtime.Immutable

/** @see ComposeInvestigatorConfig.logger */
public fun interface ComposableInvalidationLogger {
  public fun log(composable: ComposableInformation, type: InvalidationType)
}

/** Indicates how the Composable was recomposed. */
@Immutable
public sealed class InvalidationType {
  /** The Composable has actually been recomposed. */
  public data class Processed(public val reason: InvalidationReason) : InvalidationType()

  /** Recomposition was skipped because there were no changes to the Composable. */
  public data object Skipped : InvalidationType()
}


/** Explains why the Composable was recomposed. */
@Immutable
public sealed class InvalidationReason {
  abstract override fun toString(): String

  /** The first composition (*not recomposition*). This happens by default. */
  public data object Initial : InvalidationReason() {
    override fun toString(): String = "[Initial] Initial composition."
  }

  /**
   * The current recompose scope has been requested to be recomposed. This can be caused
   * by a call to `currentRecomposeScope.invalidate()` or when a field within that Composable
   * has been changed.
   */
  public data object Invalidate : InvalidationReason() {
    override fun toString(): String =
      "[Invalidate] An recomposition has been requested for the current Composable scope. " +
        "The state value in the body of that Composable function has most likely changed."
  }

  /**
   * A argument in the Composable has been changed. The changed arguments are print sorted by
   * argument name.
   */
  public data class ArgumentChanged(public val changed: List<ChangedArgument>) : InvalidationReason() {
    override fun toString(): String = buildString {
      val sortedChanges = changed.sortedBy { field -> field.previous.name }

      appendLine("[ArgumentChanged]")
      sortedChanges.forEachIndexed { index, (old, new) ->
        check(old.name == new.name) { "Argument name must be same. old.name=${old.name}, new.name=${new.name}" }
        appendLine(
          """
          |${index + 1}. ${old.name} <${old.stability}>
          |  Old: ${with(old) { "$valueString ($valueHashCode)" }}
          |  New: ${with(new) { "$valueString ($valueHashCode)" }}
          """.trimMargin(),
        )
      }
    }
  }

  /**
   * @suppress According to the Compose compiler's comments this should be determinable via the `$changed` argument.
   *
   * "the lowest bit of the bitmask is a special bit which forces execution of the function."
   * (https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/compiler/compiler-hosted/src/main/java/androidx/compose/compiler/plugins/kotlin/lower/ComposableFunctionBodyTransformer.kt;l=381-382;drc=ea884612191a32933b697cc5062aa32505be4eaa)
   *
   * However, I haven't yet figured out how to determine this, so this type is not used. It's probably related to
   * `androidx.compose.runtime.changedLowBitMask`. (in RecomposeScopeImpl.kt)
   */
  @Deprecated("Force reason is not supported yet.", level = DeprecationLevel.ERROR)
  public data object Force /*: InvalidationReason()*/ {
    override fun toString(): String = "[Force] A forced recomposition has been requested for the current Composable scope."
  }

  /** The Composable was recomposed, but no changes were detected by ComposeInvestigator. */
  public data class Unknown(public val parameters: List<ValueParameter>) : InvalidationReason() {
    override fun toString(): String =
      "[Unknown] No parameters have changed. Perhaps the state value being referenced in the Composable function body has " +
        "changed. If no state has changed, then some function parameter may be unstable, or a forced recomposition may have " +
        "been requested."
          .plus(if (parameters.isNotEmpty()) "\n(parameters: ${parameters.joinToString()})" else "")
  }
}
