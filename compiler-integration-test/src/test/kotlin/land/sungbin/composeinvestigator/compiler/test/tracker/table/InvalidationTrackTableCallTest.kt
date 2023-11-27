/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.tracker.table

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeTypeOf
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import land.sungbin.composeinvestigator.compiler.test.source.table.invalidationtracktablecall.currentComposableKeyName1
import land.sungbin.composeinvestigator.compiler.test.source.table.invalidationtracktablecall.currentComposableKeyName2
import land.sungbin.composeinvestigator.compiler.test.source.table.invalidationtracktablecall.currentComposableName1
import land.sungbin.composeinvestigator.compiler.test.source.table.invalidationtracktablecall.currentComposableName2
import land.sungbin.composeinvestigator.compiler.test.source.table.invalidationtracktablecall.table1
import land.sungbin.composeinvestigator.compiler.test.source.table.invalidationtracktablecall.table2
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationTrackTable
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InvalidationTrackTableCallTest {
  @get:Rule
  val compose = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun table_instance() {
    table1.shouldNotBeNull().shouldBeTypeOf<ComposableInvalidationTrackTable>()
    table2.shouldNotBeNull().shouldBeTypeOf<ComposableInvalidationTrackTable>()

    table1 shouldNotBeSameInstanceAs table2

    table1()
    table2()
  }

  @Test
  fun composable_name_change() {
    currentComposableName1()
    currentComposableName2()
  }

  @Test
  fun composable_key_name() {
    currentComposableKeyName1()
    currentComposableKeyName2()
  }
}
