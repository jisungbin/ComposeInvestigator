/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.source.table

import androidx.compose.runtime.Composable
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSameInstanceAs
import land.sungbin.composeinvestigator.runtime.ComposableName
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracer
import land.sungbin.composeinvestigator.runtime.getValue

val table1 = currentComposableInvalidationTracer

fun table1() {
  assertThat(table1).isSameInstanceAs(currentComposableInvalidationTracer)
}

@Composable
fun CurrentComposableName1() {
  val prevComposableName by table1.currentComposableName
  assertThat(prevComposableName).isEqualTo("CurrentComposableName1")

  table1.currentComposableName = ComposableName("ChangedComposableName1")
  assertThat(table1.currentComposableName.name).isEqualTo("ChangedComposableName1")
}

@Composable
fun CurrentComposableKeyName1() {
  assertThat(table1.currentComposableKeyName).isEqualTo("fun-CurrentComposableKeyName1(Composer,Int)Unit/pkg-land.sungbin.composeinvestigator.compiler.test.source.table.invalidationtracetablecall/file-TableCallFile1.kt")
}
