/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.tracker.logger

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.matchers.collections.shouldBeSameSizeAs
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSingleElement
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import land.sungbin.composeinvestigator.compiler.test.source.logger.InvalidationProcessedRoot_StateDelegateReference
import land.sungbin.composeinvestigator.compiler.test.source.logger.InvalidationProcessedRoot_StateDirectReference
import land.sungbin.composeinvestigator.compiler.test.source.logger.InvalidationSkippedRoot
import land.sungbin.composeinvestigator.compiler.test.source.logger.InvalidationSkippedRoot_CustomName
import land.sungbin.composeinvestigator.compiler.test.source.logger.StateNameValue
import land.sungbin.composeinvestigator.compiler.test.source.logger.findInvalidationLog
import land.sungbin.composeinvestigator.compiler.test.source.logger.findStateChangeLog
import land.sungbin.composeinvestigator.compiler.test.source.logger.invalidationLog
import land.sungbin.composeinvestigator.compiler.test.source.logger.stateChangeLog
import land.sungbin.composeinvestigator.runtime.ChangedFieldPair
import land.sungbin.composeinvestigator.runtime.ComposableInvalidationType
import land.sungbin.composeinvestigator.runtime.DeclarationStability
import land.sungbin.composeinvestigator.runtime.InvalidationReason
import land.sungbin.composeinvestigator.runtime.affect.AffectedField
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InvalidationLoggerTest {
  @get:Rule
  val compose = createAndroidComposeRule<ComponentActivity>()

  @get:Rule
  val loggerTest = InvalidationLoggerTestRule()

  @Test
  fun invalidation_skipped() {
    compose.setContent { InvalidationSkippedRoot() }
    compose.onNode(hasClickAction()).performClick()

    compose.runOnIdle {
      val rootLogs = findInvalidationLog("InvalidationSkippedRoot")
      val childLogs = findInvalidationLog("InvalidationSkippedChild")

      rootLogs shouldHaveSingleElement ComposableInvalidationType.Processed(InvalidationReason.Invalidate)
      childLogs shouldHaveSingleElement ComposableInvalidationType.Skipped
    }
  }

  @Test
  fun invalidation_skipped_custom_name() {
    compose.setContent { InvalidationSkippedRoot_CustomName() }
    compose.onNode(hasClickAction()).performClick()

    compose.runOnIdle {
      val rootLogs = findInvalidationLog("InvalidationSkippedRoot_custom_name")
      val childLogs = findInvalidationLog("InvalidationSkippedChild_custom_name")

      rootLogs shouldHaveSingleElement ComposableInvalidationType.Processed(InvalidationReason.Invalidate)
      childLogs shouldHaveSingleElement ComposableInvalidationType.Skipped
    }
  }

  @Test
  fun invalidation_processed_state_delegate() {
    compose.setContent { InvalidationProcessedRoot_StateDelegateReference() }
    repeat(2) { compose.onNode(hasClickAction()).performClick() }

    compose.runOnIdle {
      val rootLogs = findInvalidationLog("InvalidationProcessedRoot_StateDelegateReference")
      val childLogs = findInvalidationLog("InvalidationProcessedChild_StateDelegateReference")
      val stateLogs = findStateChangeLog("InvalidationProcessedRoot_StateDelegateReference")

      println(invalidationLog)
      println(stateChangeLog)

      rootLogs shouldHaveSingleElement ComposableInvalidationType.Processed(InvalidationReason.Invalidate)

      stateLogs shouldContainExactly listOf(
        StateNameValue(name = "count", previousValue = 0, newValue = 1),
        StateNameValue(name = "count", previousValue = 1, newValue = 2),
      )

      childLogs[0] shouldBe ComposableInvalidationType.Processed(InvalidationReason.Invalidate)
      childLogs[1] shouldBe ComposableInvalidationType.Processed(
        InvalidationReason.FieldChanged(
          changed = listOf(
            ChangedFieldPair(
              old = AffectedField.ValueParameter(
                name = "count",
                valueString = "0",
                valueHashCode = 0,
                stability = DeclarationStability.Stable,
              ),
              new = AffectedField.ValueParameter(
                name = "count",
                valueString = "1",
                valueHashCode = 1,
                stability = DeclarationStability.Stable,
              ),
            ),
          ),
        ),
      )

      stateLogs shouldHaveSingleElement StateNameValue(name = "count", previousValue = 0, newValue = 1)
    }
  }

  @Test
  fun invalidation_processed_state_direct() {
    compose.setContent { InvalidationProcessedRoot_StateDirectReference() }
    compose.onNode(hasClickAction()).performClick()

    compose.runOnIdle {
      val rootLogs = findInvalidationLog("InvalidationProcessedRoot_StateDirectReference")
      val childLogs = findInvalidationLog("InvalidationProcessedChild_StateDirectReference")
      val stateLogs = findStateChangeLog("InvalidationProcessedRoot_StateDirectReference")

      rootLogs shouldHaveSize 2
      rootLogs shouldBeSameSizeAs childLogs

      rootLogs[0] shouldBe ComposableInvalidationType.Processed(InvalidationReason.Invalidate)
      rootLogs[1] shouldBe ComposableInvalidationType.Processed(InvalidationReason.Unknown(params = emptyList()))

      childLogs[0] shouldBe ComposableInvalidationType.Processed(InvalidationReason.Invalidate)
      childLogs[1] shouldBe ComposableInvalidationType.Processed(
        InvalidationReason.FieldChanged(
          changed = listOf(
            ChangedFieldPair(
              old = AffectedField.ValueParameter(
                name = "count",
                valueString = "0",
                valueHashCode = 0,
                stability = DeclarationStability.Stable,
              ),
              new = AffectedField.ValueParameter(
                name = "count",
                valueString = "1",
                valueHashCode = 1,
                stability = DeclarationStability.Stable,
              ),
            ),
          ),
        ),
      )

      stateLogs shouldHaveSingleElement StateNameValue(name = "count", previousValue = 0, newValue = 1)
    }
  }
}
