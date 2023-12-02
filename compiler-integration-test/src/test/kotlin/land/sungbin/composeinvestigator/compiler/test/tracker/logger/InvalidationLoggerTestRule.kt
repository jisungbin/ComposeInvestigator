/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.tracker.logger

import land.sungbin.composeinvestigator.compiler.test.source.logger.clearInvalidationLog
import land.sungbin.composeinvestigator.compiler.test.source.logger.invalidationLogger
import land.sungbin.composeinvestigator.runtime.ComposeInvestigatorConfig
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class InvalidationLoggerTestRule : TestWatcher() {
  override fun starting(description: Description?) {
    ComposeInvestigatorConfig.invalidationLogger = invalidationLogger
    super.starting(description)
  }

  override fun finished(description: Description?) {
    super.finished(description)
    clearInvalidationLog()
  }
}
