# Advanced usage

ComposeInvestigator has a nice set of runtime features.

---

## Customizing reporting formats

ComposeInvestigator has a default reporting format that you can see in`ComposeInvestigatorConfig`.
The two most important of these are organized as variables, so they can be easily changed.

For example, you can change the `invalidationLogger` to modify the reporting format when a
recomposition occurs, or the `stateChangedListener`to modify the reporting format when a state
changed.

``` kotlin
// in Manifest:
// android:name=".ComposeInvestigatorFormat"

class ComposeInvestigatorFormat : Application() {
  override fun onCreate() {
    ComposeInvestigatorConfig.invalidationLogger = ComposableInvalidationLogger { callstacks, composable, type ->
      println("The '${composable.name}' composable has been invalidated.")
    }
    
    ComposeInvestigatorConfig.stateChangedListener = StateChangedListener { composable, name, previousValue, newValue ->
      println("The state of '$name' inside '${composable.name}' composable has changed. ($previousValue -> $newValue)")
    }
  }
}
```

## Data management classes

ComposeInvestigator has a class, `ComposableInvalidationTrackTable`, for storing data and reporting
certain events at the right time. This class is initialized as a singleton in every file, so it is
recommended to use ComposeInvestigator only in debug mode.

To do this, you can disable ComposeInvestigator in Gradle.

``` kotlin
composeInvestigator {
  enabled.set(false)
}
```

The instance of `ComposableInvalidationTrackTable` created in the current file can be retrieved
using `currentComposableInvalidationTracker`.

The `ComposableInvalidationTrackTable` has a `currentComposableName` API that allows you to interact
with the name of the current composable. (You can get the name of the composable, or change it
temporarily).

``` kotlin
@Composable fun MyComposable() {
  val table = currentComposableInvalidationTracker

  val prevName by table.currentComposableName
  assertEquals(prevName, "MyComposable") // pass

  table.currentComposableName = ComposableName("AwesomeComposable")
  val newName by table.currentComposableName
  assertEquals(newName, "AwesomeComposable") // pass
}
```

The value of `currentComposableName` is used as the name of the affected composable when certain
events are reported. If an anonymous composable is reported that doesn't specify a temporary
composable name, the composable name will always be "anonymous". It's a good idea to use this API
when appropriate to prevent this from happening.

``` kotlin
Column(
  // content: @Composable () -> Unit
  // 'content' is just argument name. It's not composable name!
  content = {
    Text("This Composable name is always <anonymous>.")
  }
)
```  

To prevent composables from being named anonymously, we are developing an Android lint to help you
name temporary composables in appropriate situations. (#90)

## Add custom invalidation callbacks

By default, all composables are registered as recomposition-tracking targets, but you may want to
duplicate registing them for arbitrary reasons. (If you have a composable that isn't
recomposition-tracking by default, that's a bug! Please report it).

In that case, you can use the `registerListener` API inside the `ComposableInvalidationTrackTable`
to register additional recomposition event callbacks for a specific composable. These added
callbacks can be removed with the `unregisterListener` API.

``` kotlin
@Composable fun RegisterListenerSample() {
  val table = currentComposableInvalidationTracker

  table.registerListener(keyName = table.currentComposableKeyName) { composable, type ->
    println("${composable.name} recomposed! ($type)")
  }
}
```

!!! tip

    Listeners are only registered on first composition.
    (no duplicate registrations across multiple recompositions)

Adding and removing callbacks to match the lifecycle of a Composable can be cumbersome, which is why
we provide the `ComposableInvalidationEffect` API, which works similarly to `LaunchedEffect`.

``` kotlin
@Composable fun InvalidationEffectSample() {
  val table = currentComposableInvalidationTracker
  val currentKeyName = table.currentComposableKeyName

  ComposableInvalidationEffect(table = table, composableKey = currentKeyName) {
    ComposableInvalidationListener { composable, type ->
      println("${composable.name} recomposed! ($type)")
    }
  }
}
```

## Add custom status tracking

By default, any state variable that inherits from `State` or `Animatable` is targeted for state
tracking. However, if you want to enable state tracking for state variables other than `State` and
`Animatable`, you can use the `State.registerStateObjectTracking` API. For more information, see the
documentation for that API.

## Suppress ComposeInvestigator behavior 

If for some reason you need to suppress ComposeInvestigator behavior only to a certain range,
you can use the `@NoInvestigation` annotation. For more information, see the documentation for that API.
