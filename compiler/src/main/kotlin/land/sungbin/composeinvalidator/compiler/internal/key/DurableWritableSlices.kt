package land.sungbin.composeinvalidator.compiler.internal.key

import org.jetbrains.kotlin.ir.declarations.IrAttributeContainer
import org.jetbrains.kotlin.util.slicedMap.BasicWritableSlice
import org.jetbrains.kotlin.util.slicedMap.RewritePolicy
import org.jetbrains.kotlin.util.slicedMap.WritableSlice

internal object DurableWritableSlices {
  val DURABLE_FUNCTION_KEY: WritableSlice<IrAttributeContainer, KeyInfo> =
    BasicWritableSlice(RewritePolicy.DO_NOTHING)
}
