/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

@file:Suppress("unused")

package land.sungbin.composeinvalidator.compiler.test.source

import androidx.compose.runtime.Composable

//@Composable
//fun fake_main() {
//  entry("hello world!")
//}

@Composable
fun entry(log: String?) {
  println(log)
  // log.hashCode()
}
