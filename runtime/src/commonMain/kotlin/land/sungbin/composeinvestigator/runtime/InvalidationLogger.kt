// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.runtime

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.RecomposeScope

/** @see ComposeInvestigator.Logger */
public fun interface InvalidationLogger {
  public fun log(composable: ComposableInformation, result: InvalidationResult)
}

/** Defines the result of the invalidation requested for the Composable. */
@Immutable public sealed class InvalidationResult {
  /** The first composition (*not recomposition*). This happens by default. */
  public data object InitialComposition : InvalidationResult() {
    override fun toString(): String = "[Initial] Initial composition."
  }

  /**
   * The current recompose scope has been requested to be recomposed. This can be caused
   * by a call to [`currentRecomposeScope.invalidate()`][RecomposeScope.invalidate] or
   * when a field within that Composable has been changed.
   */
  public data object Recomposition : InvalidationResult() {
    override fun toString(): String =
      "[Invalidate] An recomposition has been requested for the current Composable scope. " +
        "The state value in the body of that Composable function has most likely changed."
  }

  /**
   * An argument in the Composable has been changed. The changed arguments are print sorted by
   * argument name.
   */
  public data class ArgumentChanged(public val changed: List<ChangedArgument>) : InvalidationResult() {
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

  /** Recomposition was skipped because there were no changes to the Composable. */
  public data object Skipped : InvalidationResult()

  /**
   * @suppress According to the Compose compiler's comments this should be determinable via the `$changed` argument.
   *
   * "the lowest bit of the bitmask is a special bit which forces execution of the function."
   * (https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/compiler/compiler-hosted/src/main/java/androidx/compose/compiler/plugins/kotlin/lower/ComposableFunctionBodyTransformer.kt;l=381-382;drc=ea884612191a32933b697cc5062aa32505be4eaa)
   *
   * However, I haven't yet figured out how to determine this, so this type is not used. It's probably related to
   * [androidx.compose.runtime.changedLowBitMask]. (in RecomposeScopeImpl.kt)
   */
  @Deprecated("Force reason is not supported yet.", level = DeprecationLevel.ERROR)
  public data object Force /*: InvalidationResult()*/ {
    override fun toString(): String = "[Force] A forced recomposition has been requested for the current Composable scope."
  }
}
