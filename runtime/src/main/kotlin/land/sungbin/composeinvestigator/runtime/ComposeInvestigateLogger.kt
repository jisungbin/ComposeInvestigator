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
 * public fun composeInvestigateLogger(composable: AffectedComposable, logType: ComposeInvestigateLogType) {
 *   // Your logger code here
 * }
 * ```
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
public annotation class ComposeInvestigateLogger

public data class AffectedComposable(public val name: String, public val pkg: String)

public sealed class ComposeInvestigateLogType {
  public class InvalidationProcessed(public val diffParams: DiffParams?) : ComposeInvestigateLogType() {
    override fun toString(): String = "InvalidationProcessed(diffParams=$diffParams)"
  }

  public data object InvalidationSkipped : ComposeInvestigateLogType()
}
