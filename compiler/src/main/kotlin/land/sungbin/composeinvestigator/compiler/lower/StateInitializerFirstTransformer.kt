// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.lower

import androidx.compose.compiler.plugins.kotlin.ComposeCallableIds
import land.sungbin.composeinvestigator.compiler.log
import land.sungbin.composeinvestigator.compiler.rememberSaveable
import land.sungbin.composeinvestigator.compiler.struct.IrComposeInvestigator
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.getCompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.util.isFunctionTypeOrSubtype
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addToStdlib.cast
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

/**
 * Transforms the code to add the use of `ComposableInvalidationTraceTable#registerStateObject`
 * in the definition of `State` or `StateObject`.
 *
 * ### Original
 *
 * ```
 * @Composable fun MyComposable() {
 *   val myState = remember { mutableStateOf(0) }
 *   val myStateObject by remember { mutableStateMapOf(0 to 0) }
 * }
 * ```
 *
 * ### Transformed
 *
 * ```
 * @Composable fun MyComposable() {
 *   val myState = remember { mutableStateOf(0) }.also { state ->
 *     currentComposableInvalidationTracer.registerStateObject(state, name = "myState")
 *   }
 *   val myStateObject by remember { mutableStateMapOf(0 to 0) }.also { stateObject ->
 *     currentComposableInvalidationTracer.registerStateObject(stateObject, name = "myStateObject")
 *   }
 * }
 */
public class StateInitializerFirstTransformer(
  context: IrPluginContext,
  messageCollector: MessageCollector,
) : ComposeInvestigatorBaseLower(context, messageCollector) {
  private val investigator = IrComposeInvestigator(context)

  override fun firstTransformStateInitializer(name: Name, initializer: IrExpression, file: IrFile): IrExpression {
    messageCollector.log(
      "Visit state initializer: ${name.asString()}",
      initializer.getCompilerMessageLocation(file),
    )

    if (
      initializer is IrCall &&
      (
        initializer.symbol.owner.callableIdOrNull == ComposeCallableIds.remember ||
          initializer.symbol.owner.callableIdOrNull == ComposeCallableIds.rememberSaveable
        )
    ) {
      val rememberBody =
        initializer.valueArguments
          .lastOrNull { it?.type?.isFunctionTypeOrSubtype() == true }
          ?.cast<IrFunctionExpression>()
          ?: return initializer
      val returnExpression = rememberBody.function.body?.statements?.last()?.safeAs<IrReturn>() ?: return initializer

      val newReturnStatement = investigator.irRegisterStateObject(returnExpression.value, irString(name.asString()))

      rememberBody.function
        .body!!.cast<IrBlockBody>()
        .statements
        .last().cast<IrReturn>()
        .value = newReturnStatement

      return initializer.also {
        messageCollector.log(
          "Transform state initializer succeed: ${name.asString()}",
          returnExpression.getCompilerMessageLocation(file),
        )
      }
    }

    return investigator
      .irRegisterStateObject(initializer, irString(name.asString()))
      .also {
        messageCollector.log(
          "Transform state initializer succeed: ${name.asString()}",
          initializer.getCompilerMessageLocation(file),
        )
      }
  }
}
