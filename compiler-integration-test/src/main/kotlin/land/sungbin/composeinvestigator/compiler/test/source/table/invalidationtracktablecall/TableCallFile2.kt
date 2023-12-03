/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.source.table.invalidationtracktablecall

import androidx.compose.runtime.Composable
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import land.sungbin.composeinvestigator.runtime.ComposableName
import land.sungbin.composeinvestigator.runtime.currentComposableInvalidationTracker
import land.sungbin.composeinvestigator.runtime.getValue

val table2 = currentComposableInvalidationTracker

fun table2() {
  table2 shouldBeSameInstanceAs currentComposableInvalidationTracker
}

@Composable
fun CurrentComposableName2() {
  val prevComposableName by table2.currentComposableName
  prevComposableName shouldBe "CurrentComposableName2"

  table2.currentComposableName = ComposableName("ChangedComposableName2")
  table2.currentComposableName.name shouldBe "ChangedComposableName2"
}

@Composable
fun CurrentComposableKeyName2() {
  table2.currentComposableKeyName shouldBe "fun-CurrentComposableKeyName2(Composer,Int)Unit/pkg-land.sungbin.composeinvestigator.compiler.test.source.table.invalidationtracktablecall/file-TableCallFile2.kt"
}
