# ComposeInvestigator Internals

ComposeInvestigator is built by utilizing the Kotlin compiler plugin and the open
source of the Compose compiler.

``` kotlin
// before
@Composable fun Main() {
  val count = mutableStateOf(0)
  Text(text = "Count: $count")
}

// after compile (with compose compiler)
val composeInvestigatorTable: ComposableInvalidationTrackTable = ComposableInvalidationTrackTable()
val composableCallstack: Stack<String> = Stack()

@Composable fun Main(args: Any, $composer: Composer?, $changed: Int) {
  $composer = $composer.startRestartGroup()
  if (!$composer.skipping) {
    val affectFields = mutableListOf()
    val argsValueParam = ValueParameter("args", "kotlin.Any", args.toString(), args.hashCode(), Certain(false))
    affectFields.add(argsValueParam)
    
    val invalidationReason = composeInvestigatorTable.computeInvalidationReason("fun-Main(Any,Composer,Int)Unit", affectFields)
    composeInvestigatorTable.callListeners("fun-Main(Any,Composer,Int)Unit", AffectedComposable("Main", "my.package.name", "MyFileName.kt", line, column), Processed(invalidationReason))
    ComposeInvestigatorConfig.invalidationLogger(composableCallstack.toList(), AffectedComposable("Main", "my.package.name", "MyFileName.kt", line, column), Processed(invalidationReason))
    
    val count = mutableStateOf(0).registerStateObjectTracking(
      composer = $composer, 
      composable = AffectedComposable("Main", "my.package.name", "MyFileName.kt", line, column), 
      composableKeyName = "fun-Main(Any,Composer,Int)Unit", 
      stateName = "count",
    )
    
    try {
      composableCallstack.push("my.package.name.Main")
      Text("$count")
    } finally {
      composableCallstack.pop()
    }
  } else {
    composeInvestigatorTable.callListeners("fun-Main(Any,Composer,Int)Unit", AffectedComposable("Main", "my.package.name", "MyFileName.kt", line, column), Skipped)
    ComposeInvestigatorConfig.invalidationLogger(composableCallstack.toList(), AffectedComposable("Main", "my.package.name", "MyFileName.kt", line, column), Skipped)
    $composer.skipToGroupEnd()
  }
  $composer.endRestartGroup()?.updateScope { $composer: Composer?, $force: Int ->
    composeInvestigatorTable.callListeners("fun-Main(Any,Composer,Int)Unit", AffectedComposable("Main", "my.package.name", "MyFileName.kt", line, column), Processed(Invalidate))
    ComposeInvestigatorConfig.invalidationLogger(composableCallstack.toList(), AffectedComposable("Main", "my.package.name", "MyFileName.kt", line, column), Processed(Invalidate))
    Main(args, $composer, $changed)
  }
}
```

---

### Composable call stacks tracking

Composable callstack tracing has been implemented since issue [#77](https://github.com/jisungbin/ComposeInvestigator/issues/77)
and is still an experimental feature.

The concept is simple: Wrap all calls to composable functions in `try-finally`, and push the
parent function name onto the stack before calling the composable. Then pop it from `finally`.

``` kotlin
// before
package land.sungbin.composeinvestigator

@Composable fun Main() {
  Call()
}

// after compile
package land.sungbin.composeinvestigator

val composableCallstack: Stack<String> = Stack()

@Composable fun Main() {
  try {
    composableCallstack.push("land.sungbin.composeinvestigator.Main")
    Call()
  } finally {
    composableCallstack.pop()
  }
}
```

If you have an idea for a better way to track callstacks, please open an issue.

### Recomposition tracking

Recomposition tracing involves three different kinds of code generation.

1. compute composable argument changes
2. detect composable invalidation requests
3. detect composable invalidation skips

Composable argument change detection starts by sending all the arguments of the composable to the
`ComposableInvalidationTrackTable`. It then calculates which arguments have changed and determines
the reason for the recomposition.

``` kotlin
// before
@Composable fun Main(args: Any) {
  Text(args.toString())
}

// after compile
val composeInvestigatorTable: ComposableInvalidationTrackTable = ComposableInvalidationTrackTable()

@Composable fun Main(args: Any) {
  val affectFields = mutableListOf()
  val argsValueParam = ValueParameter("args", "kotlin.Any", args.toString(), args.hashCode(), Certain(false))
  affectFields.add(argsValueParam)
  
  val invalidationReason = composeInvestigatorTable.computeInvalidationReason("fun-Main(Any)Unit", affectFields)
  
  Text(args.toString())
}
```

If the composable body has been executed, it means that the composable has been recomposed,
so we generate recomposition logging and event sending code in the first line of the composable body.

``` diff
// after compile
val composeInvestigatorTable: ComposableInvalidationTrackTable = ComposableInvalidationTrackTable()

@Composable fun Main(args: Any) {
  val affectFields = mutableListOf()
  val argsValueParam = ValueParameter("args", "kotlin.Any", args.toString(), args.hashCode(), Certain(false))
  affectFields.add(argsValueParam)
  
  val invalidationReason = composeInvestigatorTable.computeInvalidationReason("fun-Main(Any)Unit", affectFields)
  
  + composeInvestigatorTable.callListeners("fun-Main(Any)Unit", AffectedComposable("Main", "my.package.name", "MyFileName.kt", line, column), Processed(invalidationReason))
  + ComposeInvestigatorConfig.invalidationLogger(composableCallstack.toList(), AffectedComposable("Main", "my.package.name", "MyFileName.kt", line, column), Processed(invalidationReason))
  
  Text(args.toString())
}
```


### State change tracking

(TODO)
