/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.test.tracker.logger

import land.sungbin.composeinvestigator.compiler.test.source.logger.clearInvalidationLog
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class InvalidationLoggerTestRule : TestWatcher() {
  override fun finished(description: Description?) {
    clearInvalidationLog()
    super.finished(description)
  }
}
