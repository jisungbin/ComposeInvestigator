/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

public val currentComposableInvalidationTracker: ComposableInvalidationTrackTable
  get() {
    throw NotImplementedError("Implemented as an intrinsic")
  }

public fun interface ComposableInvalidationListener {
  public fun onInvalidate(composable: AffectedComposable, type: ComposableInvalidationType)
}

// We use an annotation class to prevent LiveLiteral from the Compose compiler.
@Target()
@Retention(AnnotationRetention.SOURCE)
public annotation class ComposableName(public val name: String)

public operator fun ComposableName.getValue(thisRef: Any?, property: Any?): String = name

public class ComposableInvalidationTrackTable @ComposeInvestigatorCompilerApi public constructor() {
  private val listeners: MutableMap<String, MutableList<ComposableInvalidationListener>> = mutableMapOf()

  public val parameterMap: MutableMap<String, Array<ParameterInfo>> = mutableMapOf()

  public var currentComposableName: ComposableName
    get() {
      throw NotImplementedError("Implemented as an intrinsic")
    }
    set(@Suppress("UNUSED_PARAMETER") name) {
      throw NotImplementedError("Implemented as an intrinsic")
    }

  public val currentComposableKeyName: String
    get() {
      throw NotImplementedError("Implemented as an intrinsic")
    }

  public fun registerListener(keyName: String, listener: ComposableInvalidationListener) {
    listeners.getOrPut(keyName, ::mutableListOf).add(listener)
  }

  public fun unregisterListener(keyName: String, listener: ComposableInvalidationListener) {
    listeners.getOrDefault(keyName, mutableListOf()).remove(listener)
  }

  @ComposeInvestigatorCompilerApi
  public fun callListeners(
    keyName: String,
    composable: AffectedComposable,
    type: ComposableInvalidationType,
  ) {
    for (listener in listeners.getOrDefault(keyName, emptyList())) {
      listener.onInvalidate(composable, type)
    }
  }

  @ComposeInvestigatorCompilerApi
  public fun computeInvalidationReason(
    keyName: String,
    vararg parameterInfos: ParameterInfo,
  ): InvalidationReason {
    val prevParams = parameterMap[keyName]

    if (prevParams == null) {
      @Suppress("UNCHECKED_CAST")
      parameterMap[keyName] = parameterInfos as Array<ParameterInfo>
      return InvalidationReason.Initial
    }

    val diffs = mutableListOf<Pair<ParameterInfo, ParameterInfo>>()
    for ((index, prevParam) in prevParams.withIndex()) {
      val newParam = parameterInfos[index]
      require(prevParam.name == newParam.name) {
        "Parameter name must be same. prevParam.name=${prevParam.name}, newParam.name=${newParam.name}"
      }
      if (prevParam.valueHashCode != newParam.valueHashCode) {
        diffs.add(prevParam to newParam)
      }
    }

    @Suppress("UNCHECKED_CAST")
    parameterMap[keyName] = parameterInfos as Array<ParameterInfo>

    return if (diffs.isEmpty()) {
      InvalidationReason.Unknown(params = parameterInfos.map(ParameterInfo::toSimpleParameter))
    } else {
      InvalidationReason.ParameterChanged(changedParams = diffs)
    }
  }
}

private fun ParameterInfo.toSimpleParameter(): Parameter =
  Parameter(name = name, stability = stability)
