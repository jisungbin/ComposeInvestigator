/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mock.Linear
import androidx.compose.runtime.mock.Text

@Composable fun BasicLayout() {
  Text("Root")
  Linear {
    repeat(3) {
      Text("Child $it")
    }
  }
  Text("Tail")
}
