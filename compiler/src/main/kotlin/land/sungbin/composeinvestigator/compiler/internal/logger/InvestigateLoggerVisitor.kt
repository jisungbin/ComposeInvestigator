/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.internal.logger

import land.sungbin.composeinvestigator.compiler.internal.COMPOSABLE_FQN
import land.sungbin.composeinvestigator.compiler.internal.COMPOSE_INVESTIGATE_AFFECTED_COMPOSABLE_FQN
import land.sungbin.composeinvestigator.compiler.internal.COMPOSE_INVESTIGATE_LOGGER_FQN
import land.sungbin.composeinvestigator.compiler.internal.COMPOSE_INVESTIGATE_LOG_TYPE_FQN
import land.sungbin.composeinvestigator.compiler.internal.origin.InvestigateLoggerUsedFuntionOrigin
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
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

internal class InvestigateLoggerVisitor(
  private val context: IrPluginContext,
  private val logger: VerboseLogger,
) : IrElementTransformerVoid(), IrPluginContext by context {
  override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
    if (declaration.isValidComposeInvestigateLoggerFunction()) {
      if (declaration.origin == InvestigateLoggerUsedFuntionOrigin) return super.visitFunction(declaration)
      if (InvestigateLogger.checkLoggerIsInstalled()) {
        error(
          "More than one ComposeInvestigateLogger function was found. " +
            "Only one ComposeInvestigateLogger function is supported.",
        )
      }
      logger("Found ComposeInvestigateLogger function: ${declaration.dump()}")
      declaration.origin = InvestigateLoggerUsedFuntionOrigin
      InvestigateLogger.useCustomLogger(declaration.symbol)
    }
    return super.visitFunction(declaration)
  }

  // 1. has @ComposeInvestigateLogger and no @Composable
  // 2. top-level declaration
  // 3. public
  // 4. no extension receiver and no context receiver
  // 5. not suspend
  // 6. unit type
  // 7. has only two parameters: <AffectedComposable, ComposeInvestigateLogType>
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
      valueParameters[0].type.classFqName == COMPOSE_INVESTIGATE_AFFECTED_COMPOSABLE_FQN &&
      valueParameters[1].type.classFqName == COMPOSE_INVESTIGATE_LOG_TYPE_FQN
}
