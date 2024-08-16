/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("unused")

package land.sungbin.composeinvestigator.compiler._source.frontend.composableNameCall

import androidx.compose.runtime.Composable
import kotlin.random.Random
import land.sungbin.composeinvestigator.runtime.ComposableName

@Composable fun expressionComposableName() {
  ComposableName(Random.nextBoolean().toString())
}
