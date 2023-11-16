package land.sungbin.composeinvalidator.compiler.test.source

internal class ParameterInfo(val name: String, val value: String, val hashCode: Int)

internal class DiffParams(
  private val composableName: String,
  private val params: List<Pair<ParameterInfo, ParameterInfo>>,
) {
  override fun toString(): String =
    buildString(capacity = params.size + 2) {
      appendLine("[$composableName] DiffParams(")
      for ((index, diffParam) in params.withIndex()) {
        val (prevParam, newParam) = diffParam
        val message =
          "$index. [${prevParam.name}] ${prevParam.value}$${prevParam.hashCode} -> ${newParam.value}$${newParam.hashCode}"
        appendLine("  $message")
      }
      appendLine(")")
    }
}

internal class ComposableInvalidationTrackTable {
  private val parameterMap = mutableMapOf<String, Array<ParameterInfo>>()

  fun putParamsIfAbsent(name: String, vararg parameterInfo: ParameterInfo) {
    if (parameterMap[name] == null) {
      @Suppress("UNCHECKED_CAST")
      parameterMap[name] = parameterInfo as Array<ParameterInfo>
    }
  }

  fun getDiffParamsAndPutNewParams(
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