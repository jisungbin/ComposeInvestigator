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
 * @param pkg Path to the package where the composable function is defined
 * @param filePath Path to the file where the composable function is defined
 * @param startLine Start line of the composable function
 * @param startColumn Start column of the composable function
 */
public data class AffectedComposable(
  public val name: String,
  public val pkg: String,
  public val filePath: String,
  public val startLine: Int,
  public val startColumn: Int,
) {
  /** Fully-qualified name of the composable function */
  public val fqName: String = pkg.takeUnless(String::isEmpty)?.plus(".").orEmpty() + name
}
