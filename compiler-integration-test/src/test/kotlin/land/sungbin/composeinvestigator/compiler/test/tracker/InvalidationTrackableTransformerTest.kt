/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("TestFunctionName")

package land.sungbin.composeinvestigator.compiler.test.tracker

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith

private var currentComposableName: String? = null
private val composeInvestigateLogMap =
  mutableMapOf<String?, MutableList<Pair<AffectedComposable, ComposeInvestigateLogType>>>()

class ComposeInvestigateLogTestRule : TestWatcher() {
  override fun starting(description: Description?) {
    currentComposableName = description?.methodName
  }

  fun getCurrentLog(): List<Pair<AffectedComposable, ComposeInvestigateLogType>>? =
    composeInvestigateLogMap[currentComposableName]
}

// @Suppress("unused")
// @ComposeInvestigateLogger
// fun composeInvestigateLogger(composable: AffectedComposable, logType: ComposeInvestigateLogType) {
//   composeInvestigateLogMap.getOrPut(currentComposableName, ::mutableListOf).add(composable to logType)
// }

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

  @get:Rule
  val investigateLogRule = ComposeInvestigateLogTestRule()

  @Before
  fun cleanup() {
    composeInvestigateLogMap.clear()
  }

  @DisplayName("Most basic logging tests")
  @Test
  fun basic() {
    @Composable
    fun BasicTextContent() {
      var number by remember { mutableIntStateOf(1) }

      BasicText(
        modifier = Modifier.testTag("display"),
        text = "$number",
      )
      Box(
        Modifier
          .testTag("increaseNumber")
          .clickable { number++ },
      )
    }

    compose.setContent {
      BasicTextContent()
    }

    val button = compose.onNodeWithTag("increaseNumber")
    button.performClick()

    val text = compose.onNodeWithTag("display")

    compose.runOnIdle {
      text.assertTextEquals("2")

      // [
      // (AffectedComposable(
      //   name= <anonymous>,
      //   pkg=land.sungbin.composeinvestigator.compiler.test.tracker.ComposableSingletons$InvalidationTrackableTransformerTestKt.lambda-1.<anonymous>
      // ),
      // InvalidationProcessed(diffParams=null)
      // ),
      // (AffectedComposable(
      //   name= <anonymous>,
      //   pkg=land.sungbin.composeinvestigator.compiler.test.tracker.ComposableSingletons$InvalidationTrackableTransformerTestKt.lambda-1.<anonymous>
      // ),
      // InvalidationProcessed(
      //   diffParams= <<anonymous>> DiffParams(
      //     No diff params.
      //     Some argument may be unstable, or there may have been an invalidation request on the current RecomposeScope.
      // )
      // ))]
      println(investigateLogRule.getCurrentLog())
    }
  }
}
