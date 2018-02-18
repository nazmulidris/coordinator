# coordinator

This project is all about `CoordinatorLayout` and nested scrolling.

# Resources

- [CoordinatorLayout and Behaviors (medium article)](https://goo.gl/oLH8pm)

# Info about the code

## Attaching the custom behavior to a child of the `CoordinatorLayout`

The main layout XML file has a `FrameLayout` view group that has a custom behavior called 
`FooterBarBehavior` attached to it. It also has a key-value pair that is defined, which 
can be retrieved in the code.

```xml
<CoordinatorLayout ...>
    <RecyclerView ...>...</RecyclerView>
    <AppBarLayout ...>
        <Toolbar ...>...</Toolbar>
    </AppBarLayout>
    <FrameLayout xmlns:my_app="http://example.com"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom"
        my_app:my_key="my_value"
        app:layout_behavior="engineering.uxd.example.coordinator.FooterBarBehavior">
</CoordinatorLayout>
```
## Meaning of the arguments

Note that the custom behavior is bound to the `FrameLayout` child of `CoordinatorLayout`. Here are
the arguments that are going to be passed to the methods of this custom behavior.
1. `parent` - this is the `CoordinatorLayout` object itself.
2. `child` - this is the `FrameLayout` object that the behavior is bound to (see the XML above).
3. `dependency` - this will contain object references to all the children of the `parent`.

## `child` reacting to changes in the `dependency` views

The `FooterBarBehavior` class extends `CoordinatorLayout.Behavior<FrameLayout>` and it implements 
two methods:

1. `layoutDependsOn(parent: CoordinatorLayout, child: FrameLayout, dependency: View): Boolean`. 
This method is called multiple times to check if any of the children contained in 
the `CoordinatorLayout` (`parent`) will affect this behavior. In this case, we declare that the 
behavior of the `child` (`FrameLayout`, which this behavior is bound to in XML) is affected by the 
any changes to the `AppBarLayout` (`dependency`). 

2. `onDependentViewChanged(parent: CoordinatorLayout, child: FrameLayout, dependency: View): Boolean`. 
If the first method found that there were views that this behavior has a dependency on, then this 
second method will be called when those views change in any way. This provides this behavior the 
opportunity to take some action on the `child` view, in reaction to some change in the `dependency`
view.

## Key Value pairs
The following code in the behavior class constructor shows how to retrieve the key-value pair 
that is declared in the XML layout above. 
```kotlin
init {
    value = attrs.getAttributeValue("http://example.com", "my_key")
}
```

Note that the namespace `http://example.com` must match whatever is defined in the XML. 
```xml
<FrameLayout 
        xmlns:my_app="http://example.com"
        my_app:my_key="my_value"
        ...
```