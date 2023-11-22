/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.runtime

/**
 * ```
 * @ComposeInvestigateLogger
 * public fun composeInvestigateLogger(composable: AffectedComposable, type: ComposableInvalidateType) {
 *   // Your logger code here
 * }
 * ```
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
public annotation class ComposeInvestigateLogger

public data class AffectedComposable(
  public val name: String,
  public val pkg: String,
  public val filePath: String,
  public val startLine: Int,
  public val startColumn: Int,
)

public sealed class ComposableInvalidateType {
  public class Processed(public val diffParams: DiffParams?) : ComposableInvalidateType() {
    override fun toString(): String = "InvalidationProcessed(diffParams=$diffParams)"
  }

  public data object Skipped : ComposableInvalidateType()
}
