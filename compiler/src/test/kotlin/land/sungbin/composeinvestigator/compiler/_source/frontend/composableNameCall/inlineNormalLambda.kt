/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("unused", "NOTHING_TO_INLINE")

package land.sungbin.composeinvestigator.compiler._source.frontend.composableNameCall

import land.sungbin.composeinvestigator.runtime.ComposableName

private fun inlineNormalLambda() {
  l { ComposableName("") }
}

private inline fun l(b: () -> Unit) = Unit
