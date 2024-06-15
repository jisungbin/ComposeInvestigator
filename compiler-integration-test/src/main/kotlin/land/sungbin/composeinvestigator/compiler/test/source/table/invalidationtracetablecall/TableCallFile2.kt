/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.source.table.invalidationtracetablecall

import androidx.compose.runtime.Composable
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSameInstanceAs
import land.sungbin.composeinvestigator.runtime.ComposableName
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracer
import land.sungbin.composeinvestigator.runtime.getValue

val table2 = currentComposableInvalidationTracer

fun table2() {
  assertThat(table2).isSameInstanceAs(currentComposableInvalidationTracer)
}

@Composable
fun CurrentComposableName2() {
  val prevComposableName by table2.currentComposableName
  assertThat(prevComposableName).isEqualTo("CurrentComposableName2")

  table2.currentComposableName = ComposableName("ChangedComposableName2")
  assertThat(table2.currentComposableName.name).isEqualTo("ChangedComposableName2")
}

@Composable
fun CurrentComposableKeyName2() {
  assertThat(table2.currentComposableKeyName).isEqualTo("fun-CurrentComposableKeyName2(Composer,Int)Unit/pkg-land.sungbin.composeinvestigator.compiler.test.source.table.invalidationtracetablecall/file-TableCallFile2.kt")
}
