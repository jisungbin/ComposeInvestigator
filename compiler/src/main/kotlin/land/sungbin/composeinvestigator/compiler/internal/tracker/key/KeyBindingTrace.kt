/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

// Since this is code copied from the Compose Compiler (AOSP), we use an extra e in the copy to distinguish it.

package land.sungbin.composeinvestigator.compiler.internal.tracker.key

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.com.intellij.util.keyFMap.KeyFMap
import org.jetbrains.kotlin.ir.declarations.IrAttributeContainer
import org.jetbrains.kotlin.util.slicedMap.ReadOnlySlice
import org.jetbrains.kotlin.util.slicedMap.WritableSlice
import java.util.WeakHashMap

public class KeyBindingTrace {
  private val map = WeakHashMap<Any, KeyFMap>()

  internal operator fun <K : IrAttributeContainer, V> set(
    slice: WritableSlice<K, V>,
    key: K,
    value: V,
  ) {
    var holder = map[key.attributeOwnerId] ?: KeyFMap.EMPTY_MAP
    val prev = holder.get(slice.key)
    if (prev != null) {
      holder = holder.minus(slice.key)
    }
    holder = holder.plus(slice.key, value!!)
    map[key.attributeOwnerId] = holder
  }

  public operator fun <K : IrAttributeContainer, V> get(slice: ReadOnlySlice<K, V>, key: K): V? {
    return map[key.attributeOwnerId]?.get(slice.key)
  }
}

private val ComposeTemporaryGlobalBindingTracee = KeyBindingTrace()

@Suppress("UnusedReceiverParameter")
public val IrPluginContext.irTracee: KeyBindingTrace get() = ComposeTemporaryGlobalBindingTracee
