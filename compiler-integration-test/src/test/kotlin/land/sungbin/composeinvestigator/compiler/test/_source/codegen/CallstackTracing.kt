/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("TestFunctionName")

package land.sungbin.composeinvestigator.compiler.test._source.codegen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
@Suppress("unused")
private fun Main() {
  Sub()
  Surface { DoubleSub() }
  DeepestSub()
  VarargContents(
    { Sub() },
    { Sub() },
    { Sub() },
  )
}

@Composable
private fun Sub() {
  Box(Modifier)
}

@Composable
private fun DoubleSub() {
  Sub()
  Sub()
}

@Composable
private fun DeepestSub() {
  Surface {
    Box {
      Column {
        BoxWithConstraints {
          BasicText(text = maxWidth.value.toString())
        }
      }
    }
  }
}

@Composable
private fun VarargContents(vararg contents: @Composable () -> Unit) {
  contents.forEach { content -> content() }
}
