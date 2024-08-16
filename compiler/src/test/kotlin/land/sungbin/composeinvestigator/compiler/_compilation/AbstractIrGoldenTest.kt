/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler._compilation

import androidx.compose.compiler.plugins.kotlin.lower.dumpSrc
import kotlin.test.BeforeTest
import land.sungbin.composeinvestigator.compiler._extension.GoldenTestExtension
import land.sungbin.composeinvestigator.compiler._extension.VerifyGolden
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(GoldenTestExtension::class)
abstract class AbstractIrGoldenTest : AbstractCompilerTest() {
  private lateinit var verifyGolden: VerifyGolden

  @BeforeTest fun prepare(verifyGolden: VerifyGolden) {
    this.verifyGolden = verifyGolden
  }

  fun verifyIrGolden(file: SourceFile) {
    verifyGolden(file.source, transform(file).trimIndent())
  }

  @Suppress("RegExpSimplifiable")
  private fun transform(file: SourceFile): String {
    val irModule = compile(file)
    val keySet = mutableListOf<Int>()
    val actualTransformed = irModule
      .files.first()
      .dumpSrc(useFir = true)
      .replace('$', '%')
      // replace source keys for start group calls
      .replace(Regex("(%composer\\.start(Restart|Movable|Replaceable|Replace)Group\\()-?((0b)?[-\\d]+)")) { match ->
        val stringKey = match.groupValues[3]
        val key = if (stringKey.startsWith("0b")) Integer.parseInt(stringKey.drop(2), 2) else stringKey.toInt()
        if (key in keySet) {
          "${match.groupValues[1]}<!DUPLICATE KEY: $key!>"
        } else {
          keySet.add(key)
          "${match.groupValues[1]}<>"
        }
      }
      .replace(Regex("(sourceInformationMarkerStart\\(%composer, )([-\\d]+)")) { match ->
        "${match.groupValues[1]}<>"
      }
      // replace traceEventStart values with a token
      .replace(Regex("traceEventStart\\(-?\\d+, (%dirty|%changed|-1), (%dirty1|%changed1|-1), (.*)")) { match ->
        "traceEventStart(<>, ${match.groupValues[1]}, ${match.groupValues[2]}, <>)"
      }
      // replace source information with source it references
      .replace(Regex("(%composer\\.start(Restart|Movable|Replaceable|Replace)Group\\([^\"\\n]*)\"(.*)\"\\)")) { match ->
        "${match.groupValues[1]}\"${generateSourceInfo(match.groupValues[4], file.source)}\")"
      }
      .replace(Regex("(sourceInformation(MarkerStart)?\\(.*)\"(.*)\"\\)")) { match ->
        "${match.groupValues[1]}\"${generateSourceInfo(match.groupValues[3], file.source)}\")"
      }
      .replace(Regex("(composableLambda[N]?\\([^\"\\n]*)\"(.*)\"\\)")) { match ->
        "${match.groupValues[1]}\"${generateSourceInfo(match.groupValues[2], file.source)}\")"
      }
      .replace(Regex("(rememberComposableLambda[N]?)\\((-?\\d+)")) { match ->
        "${match.groupValues[1]}(<>"
      }
      // replace source keys for joinKey calls
      .replace(Regex("(%composer\\.joinKey\\()([-\\d]+)")) { match ->
        "${match.groupValues[1]}<>"
      }
      // composableLambdaInstance(<>, true)
      .replace(Regex("(composableLambdaInstance\\()([-\\d]+, (true|false))")) { match ->
        "${match.groupValues[1]}<>, ${match.groupValues[3]}"
      }
      // composableLambda(%composer, <>, true)
      .replace(Regex("(composableLambda\\(%composer,\\s)([-\\d]+)")) { match ->
        "${match.groupValues[1]}<>"
      }
      .trimIndent()
      .trimTrailingWhitespacesAndAddNewlineAtEOF()

    return actualTransformed
  }

  private val MatchResult.text get() = groupValues[0]
  private fun MatchResult.number() = groupValues[1].toInt()
  private fun MatchResult.isChar(c: String) = text == c
  private fun MatchResult.isNumber() = groupValues[1].isNotEmpty()
  private fun MatchResult.isFileName() = groups[4] != null

  @Suppress("RegExpSimplifiable")
  private fun generateSourceInfo(sourceInfo: String, source: String): String {
    val regex = Regex("(\\d+)|([,])|([*])|([:])|C(\\(.*\\))?|L|(P\\(*\\))|@")

    var current = 0
    var currentResult = regex.find(sourceInfo, current)
    var result = ""

    fun next(): MatchResult? {
      currentResult?.let { match ->
        current = match.range.last + 1
        currentResult = match.next()
      }
      return currentResult
    }

    // A location has the format: [<line-number>]['@' <offset> ['L' <length>]]
    // where the named productions are numbers
    fun parseLocation(): String? {
      var mr = currentResult
      if (mr != null && mr.isNumber()) {
        // line number, we ignore the value in during testing.
        mr = next()
      }
      if (mr != null && mr.isChar("@")) {
        // Offset
        mr = next()
        if (mr == null || !mr.isNumber()) {
          return null
        }
        val offset = mr.number()
        mr = next()
        var ellipsis = ""
        val maxFragment = 6
        val rawLength = if (mr != null && mr.isChar("L")) {
          mr = next()
          if (mr == null || !mr.isNumber()) {
            return null
          }
          mr.number().also { next() }
        } else {
          maxFragment
        }
        val eol = source.indexOf('\n', offset).let {
          if (it < 0) source.length else it
        }
        val space = source.indexOf(' ', offset).let {
          if (it < 0) source.length else it
        }
        val maxEnd = offset + maxFragment
        if (eol > maxEnd && space > maxEnd) ellipsis = "..."
        val length = minOf(maxEnd, minOf(offset + rawLength, space, eol)) - offset
        return "<${source.substring(offset, offset + length)}$ellipsis>"
      }
      return null
    }

    while (currentResult != null) {
      val mr = currentResult!!
      if (mr.range.first != current) {
        return "invalid source info at $current: '$sourceInfo'"
      }
      when {
        mr.isNumber() || mr.isChar("@") -> {
          val fragment = parseLocation() ?: return "invalid source info at $current: '$sourceInfo'"
          result += fragment
        }
        mr.isFileName() -> {
          return result + ":" + sourceInfo.substring(mr.range.last + 1)
        }
        else -> {
          result += mr.text
          next()
        }
      }
      require(mr != currentResult) { "regex didn't advance" }
    }

    if (current != sourceInfo.length) return "invalid source info at $current: '$sourceInfo'"

    return result
  }
}

private fun String.trimTrailingWhitespacesAndAddNewlineAtEOF(): String =
  trimTrailingWhitespaces().let { result -> if (result.endsWith("\n")) result else result + "\n" }

private fun String.trimTrailingWhitespaces(): String =
  split('\n').joinToString("\n", transform = String::trimEnd)
