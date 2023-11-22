/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

public data class DiffParams(
  public val name: String,
  public val params: List<Pair<ParameterInfo, ParameterInfo>>,
) {
  override fun toString(): String =
    buildString(capacity = params.size + 2) {
      appendLine("<$name> DiffParams(")
      if (params.isNotEmpty()) {
        for ((index, diffParam) in params.withIndex()) {
          val (prevParam, newParam) = diffParam
          val message =
            "${index + 1}. [${prevParam.name} <${prevParam.stability}>] " +
              "${prevParam.value} (${prevParam.hashCode}) -> ${newParam.value} (${newParam.hashCode})"
          appendLine("  $message")
        }
      } else {
        appendLine("  No diff params.")
        appendLine("  Some argument may be unstable, or there may have been an invalidation request on the current RecomposeScope.")
      }
      appendLine(")")
    }
}

public data class ParameterInfo(
  public val name: String,
  public val stability: DeclarationStability,
  public val value: String,
  public val hashCode: Int,
)

public fun interface ComposableInvalidationListener {
  public fun onInvalidate(composable: AffectedComposable, type: ComposableInvalidateType)
}

public val currentComposableInvalidationTracker: ComposableInvalidationTrackTable
  get() {
    throw NotImplementedError("Implemented as an intrinsic")
  }

public class ComposableInvalidationTrackTable @ComposeInvestigatorCompilerApi constructor() {
  private val listeners = mutableListOf<ComposableInvalidationListener>()

  public val parameterMap: MutableMap<String, Array<ParameterInfo>> = mutableMapOf()

  public var currentComposableName: String
    get() = throw NotImplementedError("Implemented as an intrinsic")
    set(_) { /* Implemented as an intrinsic */ }

  public val currentComposableKeyName: String
    get() {
      throw NotImplementedError("Implemented as an intrinsic")
    }

  public fun registerListener(listener: ComposableInvalidationListener) {
    listeners.add(listener)
  }

  public fun unregisterListener(listener: ComposableInvalidationListener) {
    listeners.remove(listener)
  }

  @ComposeInvestigatorCompilerApi
  public fun callListeners(composable: AffectedComposable, type: ComposableInvalidateType) {
    for (listener in listeners) {
      listener.onInvalidate(composable, type)
    }
  }

  @ComposeInvestigatorCompilerApi
  public fun computeDiffParamsIfPresent(
    composableKeyName: String,
    composableOriginalName: String,
    vararg parameterInfos: ParameterInfo,
  ): DiffParams? {
    val prevParams = parameterMap[composableKeyName]

    if (prevParams == null) {
      @Suppress("UNCHECKED_CAST")
      parameterMap[composableKeyName] = parameterInfos as Array<ParameterInfo>
      return null
    }

    val diffs = mutableListOf<Pair<ParameterInfo, ParameterInfo>>()
    for ((index, prevParam) in prevParams.withIndex()) {
      if (prevParam.hashCode != parameterInfos[index].hashCode) {
        diffs.add(prevParam to parameterInfos[index])
      }
    }

    @Suppress("UNCHECKED_CAST")
    parameterMap[composableKeyName] = parameterInfos as Array<ParameterInfo>

    return DiffParams(name = composableOriginalName, params = diffs)
  }
}
