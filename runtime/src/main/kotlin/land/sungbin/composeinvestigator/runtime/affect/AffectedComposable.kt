/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime.affect

/**
 * Indicates which composables were affected by the state change.
 *
 * @param name Composable function name
 * @param pkg Package name that the composable function is defined
 * @param filename File name that the composable function is defined
 */
public data class AffectedComposable(
  public val name: String,
  public val pkg: String,
  public val filename: String,
) {
  /** Fully-qualified name of the composable function. */
  public val fqName: String = pkg.takeUnless(String::isEmpty)?.plus(".").orEmpty() + name
}
