/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime.affect

import land.sungbin.composeinvestigator.runtime.DeclarationStability

public sealed interface AffectedField {
  public val name: String
  public val valueString: String
  public val valueHashCode: Int

  public data class ValueParameter(
    override val name: String,
    override val valueString: String,
    override val valueHashCode: Int,
    public val stability: DeclarationStability,
  ) : AffectedField

  public data class StateProperty(
    override val name: String,
    override val valueString: String,
    override val valueHashCode: Int,
  ) : AffectedField
}
