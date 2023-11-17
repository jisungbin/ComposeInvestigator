/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

package land.sungbin.composeinvalidator.compiler.internal

import java.util.WeakHashMap
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.com.intellij.util.keyFMap.KeyFMap
import org.jetbrains.kotlin.ir.declarations.IrAttributeContainer
import org.jetbrains.kotlin.util.slicedMap.ReadOnlySlice
import org.jetbrains.kotlin.util.slicedMap.WritableSlice

/**
 * This class is meant to have the shape of a BindingTrace object that could exist and flow
 * through the Psi2Ir -> Ir phase, but doesn't currently exist. Ideally, this gets replaced in
 * the future by a trace that handles this use case in upstream. For now, we are okay with this
 * because the combination of IrAttributeContainer and WeakHashMap makes this relatively safe.
 */
internal class WeakBindingTrace {
  private val map = WeakHashMap<Any, KeyFMap>()

  fun <K : IrAttributeContainer, V> record(slice: WritableSlice<K, V>, key: K, value: V) {
    var holder = map[key.attributeOwnerId] ?: KeyFMap.EMPTY_MAP
    val prev = holder.get(slice.key)
    if (prev != null) {
      holder = holder.minus(slice.key)
    }
    holder = holder.plus(slice.key, value!!)
    map[key.attributeOwnerId] = holder
  }

  operator fun <K : IrAttributeContainer, V> get(slice: ReadOnlySlice<K, V>, key: K): V? {
    return map[key.attributeOwnerId]?.get(slice.key)
  }
}

private val ComposeTemporaryGlobalBindingTrace = WeakBindingTrace()

@Suppress("UnusedReceiverParameter")
internal val IrPluginContext.irTrace: WeakBindingTrace get() = ComposeTemporaryGlobalBindingTrace
