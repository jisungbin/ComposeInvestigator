/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.invalidation.internal.key

import org.jetbrains.kotlin.ir.expressions.IrConstructorCall

public data class ComposableKeyInfo(
  public val keyName: String,
  public val affectedComposable: IrConstructorCall,
)
