/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

import land.sungbin.composeinvestigator.runtime.affect.AffectedComposable
import land.sungbin.composeinvestigator.runtime.affect.AffectedField

@JvmInline
public value class ComposableInvalidationLogger(
  private val logger: (composable: AffectedComposable, type: ComposableInvalidationType) -> Unit,
) : Function2<AffectedComposable, ComposableInvalidationType, Unit> by logger

public data class SimpleParameter(
  public val name: String,
  public val stability: DeclarationStability,
)

public data class ChangedFieldPair(public val old: AffectedField, public val new: AffectedField) {
  init {
    require(old.javaClass.canonicalName == new.javaClass.canonicalName) {
      "AffectedField must be same type. old=${old.javaClass.canonicalName}, new=${new.javaClass.canonicalName}"
    }
  }
}

public infix fun AffectedField.changedTo(new: AffectedField): ChangedFieldPair =
  ChangedFieldPair(old = this, new = new)

public sealed interface InvalidationReason {
  override fun toString(): String

  public data object Initial : InvalidationReason {
    override fun toString(): String = "Initial composition for building data inside Compose."
  }

  public data class FieldChanged(public val changed: List<ChangedFieldPair>) : InvalidationReason {
    override fun toString(): String = buildString {
      // We print the value parameter first. This order should always be guaranteed by the compiler logic,
      // but we sort it one more time just in case there are any bugs.
      val typeSortedFields = changed.sortedByDescending { field -> field.old is AffectedField.ValueParameter }
      var stateTypePrinted = false
      var index = 0

      appendLine("FieldChanged(")

      appendLine("  [Parameters]")
      if (typeSortedFields[0].old !is AffectedField.ValueParameter) {
        appendLine("    (no changed parameter)")
      }

      typeSortedFields.forEach { (old, new) ->
        check(old.name == new.name) { "Field name must be same. old.name=${old.name}, new.name=${new.name}" }

        if (old is AffectedField.StateProperty && !stateTypePrinted) {
          appendLine("  [States]")
          stateTypePrinted = true
          index = 0
        }

        appendLine(
          """
          |    ${++index}. ${old.name}${if (old is AffectedField.ValueParameter) " <${old.stability}>" else ""}
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
  @Deprecated("Force is not supported yet.")
  public data object Force : InvalidationReason {
    override fun toString(): String = "A forced recomposition has been requested for the current composable."
  }

  public data class Unknown(public val params: List<SimpleParameter> = emptyList()) : InvalidationReason {
    override fun toString(): String = "Didn't find any fields that are changed from before. " +
      "Please refer to the project README for more information on why this happens." +
      if (params.isNotEmpty()) "\n\ngiven parameters: ${params.joinToString()}" else ""
  }
}

public sealed class ComposableInvalidationType {
  public data class Processed(public val reason: InvalidationReason) : ComposableInvalidationType()
  public data object Skipped : ComposableInvalidationType()
}
