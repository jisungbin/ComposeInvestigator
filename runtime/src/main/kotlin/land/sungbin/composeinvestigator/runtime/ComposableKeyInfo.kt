/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

@ExperimentalComposeInvestigatorApi
public data class ComposableKeyInfo @ComposeInvestigatorCompilerApi constructor(
  public val composableName: String,
  public val keyName: String,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ComposableKeyInfo) return false

    if (keyName != other.keyName) return false

    return true
  }

  override fun hashCode(): Int = keyName.hashCode()
}
