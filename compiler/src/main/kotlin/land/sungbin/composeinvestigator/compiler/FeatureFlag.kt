/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler

/** @param phase 0: [ComposeInvestigatorFirstPhaseExtension], 1: [ComposeInvestigatorLastPhaseExtension] */
@Suppress("RedundantVisibilityModifier") // FIXME false negative
public enum class FeatureFlag(public val phase: Int) {
  InvalidationProcessTracing(0),
  InvalidationSkipTracing(1),
  InvalidationTraceTableIntrinsicCall(0),
  StateInitializerTracking(0),
}
