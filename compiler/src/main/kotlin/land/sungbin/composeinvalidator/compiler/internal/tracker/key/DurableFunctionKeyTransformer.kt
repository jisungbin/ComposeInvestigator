/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

@file:Suppress("unused")

package land.sungbin.composeinvalidator.compiler.internal.tracker.key

import land.sungbin.composeinvalidator.compiler.internal.irTrace
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

internal class KeyInfo(
  val name: String,
  val startOffset: Int,
  val endOffset: Int,
  val hasDuplicates: Boolean,
) {
  var used: Boolean = false
  val key: Int get() = name.hashCode()
}

internal class DurableFunctionKeyTransformer(private val context: IrPluginContext) :
  DurableKeyTransformer(DurableKeyVisitor()) {

  private var currentKeys = mutableListOf<KeyInfo>()

  override fun visitFile(declaration: IrFile): IrFile {
    val stringKeys = mutableSetOf<String>()
    return root(stringKeys) {
      val prev = currentKeys
      val next = mutableListOf<KeyInfo>()
      try {
        currentKeys = next
        super.visitFile(declaration)
      } finally {
        currentKeys = prev
      }
    }
  }

  override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
    val signature = declaration.signatureString()
    val (fullName, success) = buildKey("fun-$signature")
    val info = KeyInfo(
      name = fullName,
      startOffset = declaration.startOffset,
      endOffset = declaration.endOffset,
      hasDuplicates = !success,
    )
    currentKeys.add(info)
    context.irTrace.record(DurableWritableSlices.DURABLE_FUNCTION_KEY, declaration, info)
    return super.visitSimpleFunction(declaration)
  }
}
