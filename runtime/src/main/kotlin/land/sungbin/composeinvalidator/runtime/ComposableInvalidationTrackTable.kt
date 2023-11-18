/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package land.sungbin.composeinvalidator.runtime

@ComposeInvalidatorCompilerApi
@JvmInline
public value class DiffParams(public val params: List<Pair<ParameterInfo, ParameterInfo>>) {
  override fun toString(): String =
    buildString(capacity = params.size + 2) {
      appendLine("DiffParams(")
      if (params.isNotEmpty()) {
        for ((index, diffParam) in params.withIndex()) {
          val (prevParam, newParam) = diffParam
          val message =
            "${index + 1}. [${prevParam.name} <${prevParam.declarationStability}>] " +
              "${prevParam.value}$${prevParam.hashCode} -> ${newParam.value}$${newParam.hashCode}"
          appendLine("  $message")
        }
      } else {
        appendLine("  No diff params.")
        appendLine("  Some argument may be unstable, or there may have been an invalidation request on the current RecomposeScope.")
      }
      appendLine(")")
    }
}

@ComposeInvalidatorCompilerApi
public class ParameterInfo(
  public val name: String,
  public val declarationStability: DeclarationStability,
  public val value: String,
  public val hashCode: Int,
)

@ComposeInvalidatorCompilerApi
public class ComposableInvalidationTrackTable {
  private val parameterMap = mutableMapOf<String, Array<ParameterInfo>>()

  @ComposeInvalidatorCompilerApi
  public fun computeDiffParamsIfPresent(
    composableName: String,
    vararg newParameterInfo: ParameterInfo,
  ): DiffParams? {
    val prevParams = parameterMap[composableName]

    if (prevParams == null) {
      @Suppress("UNCHECKED_CAST")
      parameterMap[composableName] = newParameterInfo as Array<ParameterInfo>
      return null
    }

    val diffs = mutableListOf<Pair<ParameterInfo, ParameterInfo>>()
    for ((index, prevParam) in prevParams.withIndex()) {
      if (prevParam.hashCode != newParameterInfo[index].hashCode) {
        diffs.add(prevParam to newParameterInfo[index])
      }
    }

    @Suppress("UNCHECKED_CAST")
    parameterMap[composableName] = newParameterInfo as Array<ParameterInfo>

    return DiffParams(diffs)
  }
}
