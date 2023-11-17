/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

package land.sungbin.composeinvalidator.compiler.internal.transformer.key

import org.jetbrains.kotlin.ir.declarations.IrAttributeContainer
import org.jetbrains.kotlin.util.slicedMap.BasicWritableSlice
import org.jetbrains.kotlin.util.slicedMap.RewritePolicy
import org.jetbrains.kotlin.util.slicedMap.WritableSlice

internal object DurableWritableSlices {
  val DURABLE_FUNCTION_KEY: WritableSlice<IrAttributeContainer, KeyInfo> =
    BasicWritableSlice(RewritePolicy.DO_NOTHING)
}
