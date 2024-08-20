/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("unused", "NonAsciiCharacters")

package land.sungbin.composeinvestigator.compiler.test

import androidx.compose.runtime.Composable
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracer

fun main() {
  println(currentComposableInvalidationTracer)
  println(currentComposableInvalidationTracer.currentComposableName.name)
}

@Composable fun `제발되라!`() = Unit
