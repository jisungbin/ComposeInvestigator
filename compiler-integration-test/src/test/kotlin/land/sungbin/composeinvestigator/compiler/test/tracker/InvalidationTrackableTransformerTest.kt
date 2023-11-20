/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.tracker

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import land.sungbin.composeinvestigator.runtime.AffectedComposable
import land.sungbin.composeinvestigator.runtime.ComposeInvestigateLogType
import land.sungbin.composeinvestigator.runtime.ComposeInvestigateLogger
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// [transformation scenarios]
// - invalidation processed
// - invalidation skipped
//
// [function location]
// - top-level
// - top-level inline
// - class
// - object
// - local
// - lambda
// - parameter
// - companion object
// - anonymous object
@RunWith(AndroidJUnit4::class)
class InvalidationTrackableTransformerTest {
  @get:Rule
  val compose = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun test() {
    compose.setContent {
      var a by remember { mutableIntStateOf(1) }
      BasicText(modifier = Modifier.testTag("a"), text = "$a")
      Box(
        Modifier
          .testTag("b")
          .clickable { a++ },
      )
    }

    val button = compose.onNodeWithTag("b")
    button.performClick()

    val text = compose.onNodeWithTag("a")

    compose.runOnIdle {
      text.assertTextEquals("2")
    }
  }
}

@Suppress("unused")
@ComposeInvestigateLogger
fun composeInvestigateLogger(composable: AffectedComposable, logType: ComposeInvestigateLogType) {
  println("composeInvestigateLogger: $composable, $logType")
}
