/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime.affect

public data class AffectedComposable(
  public val name: String,
  public val pkg: String,
  public val filePath: String,
  @Deprecated("This value is not yet provided correctly.") public val startLine: Int,
  @Deprecated("This value is not yet provided correctly.") public val startColumn: Int,
)
