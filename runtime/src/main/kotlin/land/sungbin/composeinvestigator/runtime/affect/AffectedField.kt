/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime.affect

import land.sungbin.composeinvestigator.runtime.Stability

/** Define the fields that are affected by the state change. */
public sealed interface AffectedField {
  /** Field name */
  public val name: String

  /** String representation of the field value */
  public val valueString: String

  /** Hash code of the field value */
  public val valueHashCode: Int

  /**
   * The value parameter is changed.
   *
   * @param typeName fully-qualified name of the parameter type
   * @param stability Stability information for the parameter type
   */
  public data class ValueParameter(
    override val name: String,
    public val typeName: String,
    override val valueString: String,
    override val valueHashCode: Int,
    public val stability: Stability,
  ) : AffectedField
}
