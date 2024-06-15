/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.analysis

import org.jetbrains.kotlin.ir.expressions.IrConstructorCall

public data class ComposableKeyInfo(
  public val keyName: String,
  public val affectedComposable: IrConstructorCall,
)
