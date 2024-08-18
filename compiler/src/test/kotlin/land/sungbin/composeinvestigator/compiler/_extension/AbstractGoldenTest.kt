/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler._extension

import androidx.compose.compiler.plugins.kotlin.lower.dumpSrc
import java.io.File
import java.util.EnumSet
import java.util.stream.Stream
import kotlin.streams.asStream
import kotlin.test.BeforeTest
import land.sungbin.composeinvestigator.compiler.FeatureFlag
import land.sungbin.composeinvestigator.compiler._compilation.AbstractCompilerTest
import land.sungbin.composeinvestigator.compiler._compilation.SourceFile
import land.sungbin.composeinvestigator.compiler._extension.GoldenTestExtension.Companion.DEFAULT_GOLDEN_DIRECTORY
import land.sungbin.composeinvestigator.compiler._source.sourcePath
import land.sungbin.composeinvestigator.compiler._source.toSourceFile
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.util.DumpIrTreeOptions
import org.jetbrains.kotlin.ir.util.dump
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith

@Suppress("unused")
@ExtendWith(GoldenTestExtension::class)
abstract class AbstractGoldenTest(
  features: EnumSet<FeatureFlag> = EnumSet.noneOf(FeatureFlag::class.java),
) : AbstractCompilerTest(features) {
  private lateinit var verifyGoldenImpl: VerifyGolden

  @BeforeTest fun prepare(verifyGolden: VerifyGolden) {
    verifyGoldenImpl = verifyGolden
  }

  fun verifyGolden(ir: String, source: String) = verifyGoldenImpl(ir, source)

  fun verifyIrGolden(file: SourceFile) {
    val module = compile(file).irModuleFragment.files.single()
    verifyGoldenImpl(
      module.dump(IR_DUMP_OPTIONS).trimIndent().trimTrailingWhitespaces(),
      module.dumpKotlinLikeForGolden(file.source),
    )
  }

  @TestFactory fun dynamicVerificationAllTestSourceGoldens(info: TestInfo): Stream<DynamicTest> {
    val clazz = info.testClass.get()
    val autoGolden = clazz.getAnnotation(GoldenVerification::class.java) ?: return Stream.empty()
    val sources = File(sourcePath("${autoGolden.category}/${autoGolden.group}"))

    return sources.walk()
      .filter { file -> file.name.endsWith(".kt") }
      .map { file ->
        dynamicTest(file.nameWithoutExtension) {
          val source = file.toSourceFile()
          val module = compile(source).irModuleFragment.files.single()

          GoldenTestExtension.verifyGolden(
            goldenPath = File(
              DEFAULT_GOLDEN_DIRECTORY,
              "${autoGolden.category}/${clazz.simpleName}/${file.nameWithoutExtension}.txt",
            ).path,
            ir = module.dump(IR_DUMP_OPTIONS).trimIndent().trimTrailingWhitespaces(),
            source = module.dumpKotlinLikeForGolden(source.source),
          )
        }
      }
      .asStream()
  }

  @Suppress("RegExpSimplifiable")
  private fun IrFile.dumpKotlinLikeForGolden(code: String): String {
    val keySet = mutableListOf<Int>()
    val actualTransformed = this
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
        "${match.groupValues[1]}\"${generateSourceInfo(match.groupValues[4], code)}\")"
      }
      .replace(Regex("(sourceInformation(MarkerStart)?\\(.*)\"(.*)\"\\)")) { match ->
        "${match.groupValues[1]}\"${generateSourceInfo(match.groupValues[3], code)}\")"
      }
      .replace(Regex("(composableLambda[N]?\\([^\"\\n]*)\"(.*)\"\\)")) { match ->
        "${match.groupValues[1]}\"${generateSourceInfo(match.groupValues[2], code)}\")"
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
      .trimTrailingWhitespaces()

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

  private companion object {
    val IR_DUMP_OPTIONS = DumpIrTreeOptions(
      printSignatures = true,
      printModuleName = false,
      printFilePath = false,
    )
  }
}

private fun String.trimTrailingWhitespaces(): String =
  split('\n').joinToString("\n", transform = String::trimEnd)
