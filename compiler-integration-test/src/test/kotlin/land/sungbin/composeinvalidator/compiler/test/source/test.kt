/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

@file:Suppress("unused")

package land.sungbin.composeinvalidator.compiler.test.source

import androidx.compose.runtime.Composable

@Composable
fun entry() {
  println("Hello, world!")
}

// fun entry() {
//   if (Random.nextBoolean()) {
//     println("true world!")
//     println("true world!")
//     println("true world!")
//   } else {
//     println("false world!")
//     println("false world!")
//     println("false world!")
//   }
// }
