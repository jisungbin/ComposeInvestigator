/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("unused", "TestFunctionName")

package land.sungbin.composeinvestigator.compiler.test._source

import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
private fun Tv(title: String, value: Any) {
  Text(text = "[$title] $value")
}
