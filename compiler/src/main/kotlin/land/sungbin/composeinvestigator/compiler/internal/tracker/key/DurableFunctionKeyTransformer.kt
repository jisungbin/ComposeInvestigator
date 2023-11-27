/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.internal.tracker.key

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

public class KeyInfo(public val keyName: String, public val userProvideName: String?) {
  public fun copy(
    keyName: String = this.keyName,
    userProvideName: String? = this.userProvideName,
  ): KeyInfo = KeyInfo(keyName = keyName, userProvideName = userProvideName)
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
    val (keyName) = buildKey("fun-$signature")
    val keyInfo = KeyInfo(keyName = keyName, userProvideName = null)
    currentKeys += keyInfo
    context.irTracee[DurableWritableSlices.DURABLE_FUNCTION_KEY, declaration] = keyInfo
    return super.visitSimpleFunction(declaration)
  }
}
