/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("unused")

package land.sungbin.composeinvestigator.compiler._source.frontend.traceTableApiUsage

import land.sungbin.composeinvestigator.runtime.ComposableName
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracer

private fun normalLambda() {
  l {
    with(currentComposableInvalidationTracer) {
      currentComposableName
      currentComposableName = ComposableName("")
      currentComposableKeyName
    }
  }
}

private fun l(b: () -> Unit) = Unit
