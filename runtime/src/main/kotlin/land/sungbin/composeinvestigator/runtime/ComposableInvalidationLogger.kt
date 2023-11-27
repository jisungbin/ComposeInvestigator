/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

/**
 * ```
 * @ComposableInvalidationLogger
 * public fun invalidationLogger(composable: AffectedComposable, type: ComposableInvalidationType) {
 *   // Your logger code here
 * }
 * ```
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
public annotation class ComposableInvalidationLogger

public data class AffectedComposable(
  public val name: String,
  public val pkg: String,
  public val filePath: String,
  @Deprecated("This value is not yet provided correctly.") public val startLine: Int,
  @Deprecated("This value is not yet provided correctly.") public val startColumn: Int,
)

public data class Parameter(
  public val name: String,
  public val stability: DeclarationStability,
)

public data class ParameterInfo(
  public val name: String,
  public val valueString: String,
  public val valueHashCode: Int,
  public val stability: DeclarationStability,
)

public sealed class InvalidationReason {
  public data object Initial : InvalidationReason() {
    override fun toString(): String = "Initial composition for building data inside Compose."
  }

  public data class ParameterChanged(public val changedParams: List<Pair<ParameterInfo, ParameterInfo>>) : InvalidationReason() {
    override fun toString(): String = buildString {
      appendLine("ChangedParams(")
      for ((index, changedParam) in changedParams.withIndex()) {
        val (prevParam, newParam) = changedParam
        require(prevParam.name == newParam.name) {
          "Parameter name must be same. prevParam.name=${prevParam.name}, newParam.name=${newParam.name}"
        }
        val message =
          "${index + 1}. [${prevParam.name} <${prevParam.stability.toCertainString()}>] " +
            "${prevParam.valueString} (${prevParam.valueHashCode}) -> ${newParam.valueString} (${newParam.valueHashCode})"
        appendLine("  $message")
      }
      appendLine(")")
    }
  }

  // According to the Compose compiler's comments, this should be determinable via the $changed argument.
  // https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/compiler/compiler-hosted/src/main/java/androidx/compose/compiler/plugins/kotlin/lower/ComposableFunctionBodyTransformer.kt;l=381-382;drc=ea884612191a32933b697cc5062aa32505be4eaa
  // However, I haven't yet figured out how to determine this, so this type is not used. (in TODO status)
  @Deprecated("Force is not supported yet.")
  public data object Force : InvalidationReason() {
    override fun toString(): String = "A forced recomposition has been requested for the current composable."
  }

  public data class Unknown(public val params: List<Parameter>) : InvalidationReason() {
    override fun toString(): String = """
      No diff params. Some parameter may be unstable.
      ParameterInfos: ${params.joinToString().ifEmpty { "(no parameter)" }}
    """.trimIndent()
  }
}

public sealed class ComposableInvalidationType {
  public data class Processed(public val reason: InvalidationReason) : ComposableInvalidationType()
  public data object Skipped : ComposableInvalidationType()
}
