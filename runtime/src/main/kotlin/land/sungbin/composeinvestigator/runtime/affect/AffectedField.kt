/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime.affect

import land.sungbin.composeinvestigator.runtime.DeclarationStability

// Design it as a sealed class in case new field types come in the future.
public sealed interface AffectedField {
  public val name: String
  public val valueString: String
  public val valueHashCode: Int

  public data class ValueParameter(
    override val name: String,
    public val typeFqName: String,
    override val valueString: String,
    override val valueHashCode: Int,
    public val stability: DeclarationStability,
  ) : AffectedField
}
