/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package land.sungbin.composeinvalidator.compiler.internal.key

import land.sungbin.composeinvalidator.compiler.util.irTrace
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

internal class KeyInfo(
  val name: String,
  val startOffset: Int,
  val endOffset: Int,
  val hasDuplicates: Boolean,
) {
  var used: Boolean = false
  val key: Int get() = name.hashCode()
}

/**
 * This transform will generate a "durable" and mostly unique key for every function in the module.
 * In this case "durable" means that when the code is edited over time, a function with the same
 * semantic identity will usually have the same key each time it is compiled. This is important so
 * that new code can be recompiled and the key that the function gets after that recompile ought to
 * be the same as before, so one could inject this new code and signal to the runtime that
 * composable functions with that key should be considered invalid.
 *
 * This transform runs early on in the lowering pipeline, and stores the keys for every function in
 * the file in the BindingTrace for each function. These keys are then retrieved later on by other
 * lowerings and marked as used. After all lowerings have completed, one can use the
 * [realizeKeyMetaAnnotations] method to generate additional empty classes that include annotations
 * with the keys of each function and their source locations for tooling to utilize.
 *
 * For example, this transform will run on code like the following:
 *
 *     @Composable fun Example() {
 *       Box {
 *          Text("Hello World")
 *       }
 *     }
 *
 * And produce code like the following:
 *
 *     @Composable fun Example() {
 *       startGroup(123)
 *       Box {
 *         startGroup(345)
 *         Text("Hello World")
 *         endGroup()
 *       }
 *       endGroup()
 *     }
 *
 *     @FunctionKeyMetaClass
 *     @FunctionKeyMeta(key=123, startOffset=24, endOffset=56)
 *     @FunctionKeyMeta(key=345, startOffset=32, endOffset=43)
 *     class Example-KeyMeta
 *
 * @see DurableKeyVisitor
 */
internal class DurableFunctionKeyTransformer(private val context: IrPluginContext) :
  DurableKeyTransformer(DurableKeyVisitor()) {

  private var currentKeys = mutableListOf<KeyInfo>()

  override fun visitFile(declaration: IrFile): IrFile {
    val stringKeys = mutableSetOf<String>()
    return root(stringKeys) {
      val prev = currentKeys
      val next = mutableListOf<KeyInfo>()
      try {
        currentKeys = next
        super.visitFile(declaration)
      } finally {
        currentKeys = prev
      }
    }
  }

  override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
    val signature = declaration.signatureString()
    val (fullName, success) = buildKey("fun-$signature")
    val info = KeyInfo(
      fullName,
      declaration.startOffset,
      declaration.endOffset,
      !success,
    )
    currentKeys.add(info)
    context.irTrace.record(DurableWritableSlices.DURABLE_FUNCTION_KEY, declaration, info)
    return super.visitSimpleFunction(declaration)
  }
}
