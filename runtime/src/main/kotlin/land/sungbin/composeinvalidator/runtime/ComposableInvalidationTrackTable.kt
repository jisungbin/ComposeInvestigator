/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

package land.sungbin.composeinvalidator.runtime

public class ParameterInfo(
  public val name: String,
  public val stability: Stability,
  public val value: String,
  public val hashCode: Int,
)

public class DiffParams(
  public val composableName: String,
  public val params: List<Pair<ParameterInfo, ParameterInfo>>,
) {
  override fun toString(): String =
    buildString(capacity = params.size + 2) {
      appendLine("[$composableName] DiffParams(")
      for ((index, diffParam) in params.withIndex()) {
        val (prevParam, newParam) = diffParam
        val message =
          "$index. [${prevParam.name} " +
            "<${prevParam.stability}>] ${prevParam.value}$${prevParam.hashCode} " +
            "-> ${newParam.value}$${newParam.hashCode}"
        appendLine("  $message")
      }
      appendLine(")")
    }
}

public class ComposableInvalidationTrackTable {
  private val parameterMap = mutableMapOf<String, Array<ParameterInfo>>()

  public fun putParamsIfAbsent(name: String, vararg parameterInfo: ParameterInfo) {
    if (parameterMap[name] == null) {
      @Suppress("UNCHECKED_CAST")
      parameterMap[name] = parameterInfo as Array<ParameterInfo>
    }
  }

  public fun getDiffParamsAndPutNewParams(
    composableName: String,
    vararg newParameterInfo: ParameterInfo,
  ): DiffParams {
    val diffs = mutableListOf<Pair<ParameterInfo, ParameterInfo>>()

    for ((index, prevParam) in parameterMap[composableName]!!.withIndex()) {
      if (prevParam.hashCode != newParameterInfo[index].hashCode) {
        diffs.add(prevParam to newParameterInfo[index])
      }
    }

    @Suppress("UNCHECKED_CAST")
    parameterMap[composableName] = newParameterInfo as Array<ParameterInfo>

    return DiffParams(
      composableName = composableName,
      params = diffs,
    )
  }
}
