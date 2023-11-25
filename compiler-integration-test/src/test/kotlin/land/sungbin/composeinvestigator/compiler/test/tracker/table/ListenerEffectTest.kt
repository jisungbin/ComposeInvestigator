/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.tracker.table

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.matchers.collections.shouldBeSameSizeAs
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import land.sungbin.composeinvestigator.compiler.test.source.table.callback.SetListener_InvalidationSkippedRoot
import land.sungbin.composeinvestigator.compiler.test.source.table.callback.findInvalidationListens
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationType
import land.sungbin.composeinvestigator.runtime.InvalidationReason
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ListenerEffectTest {
  @get:Rule
  val compose = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun invalidationEffect() {
    compose.setContent { SetListener_InvalidationSkippedRoot() }
    compose.onNode(hasClickAction()).performClick()

    compose.runOnIdle {
      val rootLogs = findInvalidationListens("SetListener_InvalidationSkippedRoot")
      val childLogs = findInvalidationListens("SetListener_InvalidationSkippedChild")

      rootLogs shouldHaveSize 2
      rootLogs shouldBeSameSizeAs childLogs

      rootLogs[0] shouldBe ComposableInvalidationType.Processed(InvalidationReason.Initial)
      rootLogs[1] shouldBe ComposableInvalidationType.Processed(InvalidationReason.Unknown(params = emptyList()))

      childLogs[0] shouldBe ComposableInvalidationType.Processed(InvalidationReason.Initial)
      childLogs[1] shouldBe ComposableInvalidationType.Skipped
    }
  }
}