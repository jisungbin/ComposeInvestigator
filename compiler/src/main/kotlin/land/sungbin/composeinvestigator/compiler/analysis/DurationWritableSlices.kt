// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.analysis

import androidx.compose.compiler.plugins.kotlin.WeakBindingTrace
import org.jetbrains.kotlin.ir.declarations.IrAttributeContainer
import org.jetbrains.kotlin.util.slicedMap.BasicWritableSlice
import org.jetbrains.kotlin.util.slicedMap.RewritePolicy
import org.jetbrains.kotlin.util.slicedMap.WritableSlice
import androidx.compose.compiler.plugins.kotlin.analysis.ComposeWritableSlices

/** Same as [ComposeWritableSlices]. */
public object DurationWritableSlices {
  public val DURABLE_FUNCTION_KEY: WritableSlice<IrAttributeContainer, ComposableKeyInfo> =
    BasicWritableSlice(RewritePolicy.DO_NOTHING)
}

public operator fun <K : IrAttributeContainer, V> WeakBindingTrace.set(slice: WritableSlice<K, V>, key: K, value: V) {
  record(slice = slice, key = key, value = value)
}
