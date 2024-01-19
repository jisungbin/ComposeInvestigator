/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.table

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeTypeOf
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import land.sungbin.composeinvestigator.compiler.test.source.table.invalidationtracktablecall.CurrentComposableKeyName1
import land.sungbin.composeinvestigator.compiler.test.source.table.invalidationtracktablecall.CurrentComposableKeyName2
import land.sungbin.composeinvestigator.compiler.test.source.table.invalidationtracktablecall.CurrentComposableName1
import land.sungbin.composeinvestigator.compiler.test.source.table.invalidationtracktablecall.CurrentComposableName2
import land.sungbin.composeinvestigator.compiler.test.source.table.invalidationtracktablecall.table1
import land.sungbin.composeinvestigator.compiler.test.source.table.invalidationtracktablecall.table2
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationTrackTable
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InvalidationTrackTableIntrinsicTest {
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
    compose.setContent {
      CurrentComposableName1()
      CurrentComposableName2()
    }
  }

  @Test
  fun composable_key_name() {
    compose.setContent {
      CurrentComposableKeyName1()
      CurrentComposableKeyName2()
    }
  }
}
