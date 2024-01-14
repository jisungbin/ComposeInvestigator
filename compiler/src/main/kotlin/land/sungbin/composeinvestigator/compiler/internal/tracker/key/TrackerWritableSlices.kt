/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.internal.tracker.key

import androidx.compose.compiler.plugins.kotlin.WeakBindingTrace
import org.jetbrains.kotlin.ir.declarations.IrAttributeContainer
import org.jetbrains.kotlin.util.slicedMap.BasicWritableSlice
import org.jetbrains.kotlin.util.slicedMap.RewritePolicy
import org.jetbrains.kotlin.util.slicedMap.WritableSlice

public object TrackerWritableSlices {
  public val DURABLE_FUNCTION_KEY: WritableSlice<IrAttributeContainer, KeyInfo> = BasicWritableSlice(RewritePolicy.DO_NOTHING)
}

internal operator fun <K : IrAttributeContainer, V> WeakBindingTrace.set(slice: WritableSlice<K, V>, key: K, value: V) {
  record(slice = slice, key = key, value = value)
}
