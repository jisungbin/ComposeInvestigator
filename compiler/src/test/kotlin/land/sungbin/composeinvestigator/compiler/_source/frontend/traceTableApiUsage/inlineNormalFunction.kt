/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("unused", "NOTHING_TO_INLINE")

package land.sungbin.composeinvestigator.compiler._source.frontend.traceTableApiUsage

import land.sungbin.composeinvestigator.runtime.ComposableName
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracer

private inline fun inlineNormalFunction() {
  val t = currentComposableInvalidationTracer
  t.currentComposableName
  t.currentComposableName = ComposableName("")
  currentComposableInvalidationTracer.currentComposableKeyName
}
