/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

package land.sungbin.composeinvalidator.compiler.test.source

import androidx.compose.runtime.Composable

@Composable
fun entry(one: Int, two: String) {
  println(one)
  println(two)
}
