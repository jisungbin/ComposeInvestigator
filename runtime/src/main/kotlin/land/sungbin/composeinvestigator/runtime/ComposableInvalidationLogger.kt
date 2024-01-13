/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

import land.sungbin.composeinvestigator.runtime.affect.AffectedComposable
import land.sungbin.composeinvestigator.runtime.affect.AffectedField

/** Alternative to `(AffectedComposable, ComposableInvalidationType) -> Unit` that's useful for avoiding boxing. */
public fun interface ComposableInvalidationLogger {
  public operator fun invoke(composable: AffectedComposable, type: ComposableInvalidationType)
}

public data class ParameterInformation(
  public val name: String,
  public val stability: DeclarationStability,
)

public data class ChangedFieldPair(public val old: AffectedField, public val new: AffectedField) {
  init {
    require(old.javaClass.name == new.javaClass.name) {
      "AffectedField must be same type. old=${old.javaClass.name}, new=${new.javaClass.name}"
    }
  }
}

public infix fun AffectedField.changedTo(new: AffectedField): ChangedFieldPair =
  ChangedFieldPair(old = this, new = new)

public sealed interface InvalidationReason {
  override fun toString(): String

  public data object Initial : InvalidationReason {
    override fun toString(): String = "Initial composition."
  }

  public data object Invalidate : InvalidationReason {
    override fun toString(): String =
      "An invalidation has been requested for the current RecomposeScope. " +
        "The state value in the body of that composable function has most likely changed."
  }

  public data class FieldChanged(public val changed: List<ChangedFieldPair>) : InvalidationReason {
    override fun toString(): String = buildString {
      val sortedChanges = changed.sortedBy { field -> field.old.name }

      appendLine("FieldChanged(")
      appendLine("  [Parameters]")
      sortedChanges.forEachIndexed { index, (old, new) ->
        check(old.name == new.name) { "Field name must be same. old.name=${old.name}, new.name=${new.name}" }
        appendLine(
          """
          |    ${index + 1}. ${old.name}${if (old is AffectedField.ValueParameter) " <${old.stability}>" else ""}
          |      Old: ${with(old) { "$valueString ($valueHashCode)" }}
          |      New: ${with(new) { "$valueString ($valueHashCode)" }}
          """.trimMargin(),
        )
      }
      appendLine(")")
    }
  }

  // According to the Compose compiler's comments, this should be determinable via the $changed argument.
  // https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/compiler/compiler-hosted/src/main/java/androidx/compose/compiler/plugins/kotlin/lower/ComposableFunctionBodyTransformer.kt;l=381-382;drc=ea884612191a32933b697cc5062aa32505be4eaa
  // However, I haven't yet figured out how to determine this, so this type is not used. (in TODO status)
  // It's probably related to androidx.compose.runtime.changedLowBitMask. (RecomposeScopeImpl.kt)
  @Deprecated("Force reason is not supported yet.")
  public data object Force : InvalidationReason {
    override fun toString(): String = "A forced recomposition has been requested for the current composable."
  }

  public data class Unknown(public val params: List<ParameterInformation> = emptyList()) : InvalidationReason {
    override fun toString(): String =
      "No parameters have changed. Perhaps the state value being referenced in the function body has changed. " +
        "If no state has changed, then some function parameter may be unstable, or a forced invalidation may have " +
        "been requested.${if (params.isNotEmpty()) "\nGiven parameters: ${params.joinToString()}" else ""}"
  }
}

public sealed class ComposableInvalidationType {
  public data class Processed(public val reason: InvalidationReason) : ComposableInvalidationType()
  public data object Skipped : ComposableInvalidationType()
}
