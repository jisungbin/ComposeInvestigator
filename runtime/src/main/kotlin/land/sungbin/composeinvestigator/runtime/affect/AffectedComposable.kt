/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime.affect

private const val ROOT = "<ROOT>"

public data class AffectedComposable(
  public val name: String,
  public val pkg: String,
  public val filePath: String,
  public val startLine: Int,
  public val startColumn: Int,
  public val parent: AffectedComposable? = null,
) {
  public val fqName: String = "${pkg.ifEmpty { ROOT }}.$name"

  public val parentTree: String = buildList {
    var current = parent
    while (current != null) {
      add(current!!.fqName)
      current = current!!.parent
    }
  }.reversed().joinToString(separator = " -> ")

  public fun render(): String =
    "AffectedComposable(" +
      "fqName:'$fqName', " +
      "locate='$filePath' ($startLine:$startColumn), " +
      "parentTree=${parentTree.ifEmpty { ROOT }}" +
      ")"
}
