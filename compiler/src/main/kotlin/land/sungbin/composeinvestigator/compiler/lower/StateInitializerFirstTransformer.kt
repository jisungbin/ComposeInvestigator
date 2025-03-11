// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.lower

import land.sungbin.composeinvestigator.compiler.log
import land.sungbin.composeinvestigator.compiler.struct.IrComposeInvestigator
import land.sungbin.composeinvestigator.compiler.struct.IrComposeInvestigatorHolder
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.getCompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.name.Name

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
  // TODO Special behaviour of 'remember' and 'rememberSaveable':
  //  Transforms should be performed inside the 'remember[Saveable]' lambda, not outside of it.
  override fun firstTransformStateInitializer(
    name: Name,
    initializer: IrExpression,
    table: IrComposeInvestigator,
  ): IrExpression {
    messageCollector.log(
      "Visit state initializer: ${name.asString()}",
      initializer.getCompilerMessageLocation(table.rawProp.file),
    )

    return table.callRegisterStateObject(initializer, irString(name.asString())).also {
      messageCollector.log(
        "Transform state initializer succeed: ${name.asString()}",
        initializer.getCompilerMessageLocation(table.rawProp.file),
      )
    }
  }
}
