/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.table

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isNotNull
import assertk.assertions.isSameInstanceAs
import land.sungbin.composeinvestigator.compiler.test.source.table.invalidationtracetablecall.CurrentComposableKeyName1
import land.sungbin.composeinvestigator.compiler.test.source.table.invalidationtracetablecall.CurrentComposableKeyName2
import land.sungbin.composeinvestigator.compiler.test.source.table.invalidationtracetablecall.CurrentComposableName1
import land.sungbin.composeinvestigator.compiler.test.source.table.invalidationtracetablecall.CurrentComposableName2
import land.sungbin.composeinvestigator.compiler.test.source.table.invalidationtracetablecall.table1
import land.sungbin.composeinvestigator.compiler.test.source.table.invalidationtracetablecall.table2
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationTraceTable
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InvalidationTraceTableIntrinsicTest {
  @get:Rule
  val compose = createAndroidComposeRule<ComponentActivity>()

  @Test fun table_instance() {
    assertThat(table1).isNotNull().hasClass<ComposableInvalidationTraceTable>()
    assertThat(table2).isNotNull().hasClass<ComposableInvalidationTraceTable>()

    assertThat(table1).isSameInstanceAs(table2)

    table1()
    table2()
  }

  @Test fun composable_name_change() {
    compose.setContent {
      CurrentComposableName1()
      CurrentComposableName2()
    }
  }

  @Test fun composable_key_name() {
    compose.setContent {
      CurrentComposableKeyName1()
      CurrentComposableKeyName2()
    }
  }
}
