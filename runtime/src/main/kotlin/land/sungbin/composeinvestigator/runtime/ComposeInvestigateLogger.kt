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
 * public fun composeInvestigateLogger(composableName: String, logType: ComposeInvestigateLogType) {
 *   // Your logger code here
 * }
 * ```
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
public annotation class ComposeInvestigateLogger

public sealed class ComposeInvestigateLogType {
  public class InvalidationProcessed(public val diffParams: DiffParams?) : ComposeInvestigateLogType()
  public data object InvalidationSkipped : ComposeInvestigateLogType()
}
