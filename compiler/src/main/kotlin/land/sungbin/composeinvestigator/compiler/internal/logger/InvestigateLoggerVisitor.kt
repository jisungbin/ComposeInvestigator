/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.internal.logger

import land.sungbin.composeinvestigator.compiler.internal.AFFECTED_COMPOSABLE_FQN
import land.sungbin.composeinvestigator.compiler.internal.COMPOSABLE_FQN
import land.sungbin.composeinvestigator.compiler.internal.COMPOSABLE_INVALIDATE_TYPE_FQN
import land.sungbin.composeinvestigator.compiler.internal.COMPOSE_INVESTIGATE_LOGGER_FQN
import land.sungbin.composeinvestigator.compiler.util.VerboseLogger
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isSuspend
import org.jetbrains.kotlin.ir.util.isTopLevel
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

internal class InvestigateLoggerVisitor(
  private val context: IrPluginContext,
  private val logger: VerboseLogger,
) : IrElementTransformerVoid(), IrPluginContext by context {
  override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
    val currentLoggerSymbol = InvestigateLogger.getCurrentLoggerSymbolOrNull()
    if (declaration.isValidComposeInvestigateLoggerFunction()) {
      if (currentLoggerSymbol != null && currentLoggerSymbol.owner.kotlinFqName != declaration.kotlinFqName) {
        val message = """
          More than one @ComposeInvestigateLogger detected. To avoid confusion, the default logger is used.
          Detected loggers: [${currentLoggerSymbol.owner.kotlinFqName.asString()}, ${declaration.kotlinFqName.asString()}]
        """.trimIndent()
        // TODO: print message with warning level
        logger(message)
        InvestigateLogger.useDefaultLogger(context)
      } else if (currentLoggerSymbol == null) {
        InvestigateLogger.useCustomLogger(declaration.symbol)
        logger("Found ComposeInvestigateLogger function: ${declaration.dump()}")
      }
    }
    return super.visitSimpleFunction(declaration)
  }

  // 1. has @ComposeInvestigateLogger and no @Composable
  // 2. top-level declaration
  // 3. public
  // 4. no extension receiver and no context receiver
  // 5. not suspend
  // 6. unit type
  // 7. has only two parameters: <AffectedComposable, ComposableInvalidateType>
  private fun IrFunction.isValidComposeInvestigateLoggerFunction(): Boolean =
    hasAnnotation(COMPOSE_INVESTIGATE_LOGGER_FQN) &&
      !hasAnnotation(COMPOSABLE_FQN) &&
      isTopLevel &&
      visibility.isPublicAPI &&
      extensionReceiverParameter == null &&
      contextReceiverParametersCount == 0 &&
      !isSuspend &&
      returnType.isUnit() &&
      valueParameters.size == 2 &&
      valueParameters[0].type.classFqName == AFFECTED_COMPOSABLE_FQN &&
      valueParameters[1].type.classFqName == COMPOSABLE_INVALIDATE_TYPE_FQN
}
