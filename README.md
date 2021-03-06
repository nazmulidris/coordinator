Table of Contents
=================

   * [CoordinatorLayout and nested scrolling](#coordinatorlayout-and-nested-scrolling)
   * [Screenshot of app in action](#screenshot-of-app-in-action)
   * [Attaching the custom behavior to a child of the CoordinatorLayout](#attaching-the-custom-behavior-to-a-child-of-the-coordinatorlayout)
      * [Meaning of the arguments](#meaning-of-the-arguments)
      * [child reacting to changes in the <code>dependency</code> views](#child-reacting-to-changes-in-the-dependency-views)
         * [1. layoutDependsOn(parent: CoordinatorLayout, child: FrameLayout, dependency: View)](#1-layoutdependsonparent-coordinatorlayout-child-framelayout-dependency-view)
         * [2. onDependentViewChanged(parent: CoordinatorLayout, child: FrameLayout, dependency: View)](#2-ondependentviewchangedparent-coordinatorlayout-child-framelayout-dependency-view)
      * [Key Value pairs](#key-value-pairs)
   * [Nested scrolling](#nested-scrolling)
      * [1. onStartNestedScroll](#1-onstartnestedscroll)
      * [2. onNestedScroll](#2-onnestedscroll)
   * [Apply physics based animation to the RecyclerView](#apply-physics-based-animation-to-the-recyclerview)

# `CoordinatorLayout` and nested scrolling

This project is all about `CoordinatorLayout` and nested scrolling.

To learn more CoordinatorLayout, custom behaviors, and nested scrolling here are some great 
articles:
- [Intercepting everything with CoordinatorLayout Behaviors](https://goo.gl/oLH8pm)
- [Experimenting with Nested Scrolling](https://goo.gl/w8Jrq2)
- [Carry on Scrolling](https://goo.gl/1dwU8S)

# Screenshot of app in action
<img 
src="https://raw.githubusercontent.com/nazmulidris/coordinator/master/docs/screenshot.gif" 
width="400">

# Attaching the custom behavior to a child of the `CoordinatorLayout`

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

### 1. `layoutDependsOn(parent: CoordinatorLayout, child: FrameLayout, dependency: View)`
This method is called multiple times to check if any of the children contained in 
the `CoordinatorLayout` (`parent`) will affect this behavior. In this case, we declare that the 
behavior of the `child` (`FrameLayout`, which this behavior is bound to in XML) is affected by the 
any changes to the `AppBarLayout` (`dependency`). 

```kotlin
/**
 * This custom behavior depends on the [AppBarLayout] object. So make sure to return
 * `true` when the `dependency` matches [AppBarLayout].
 */
override fun layoutDependsOn(parent: CoordinatorLayout,
                             child: FrameLayout,
                             dependency: View): Boolean {
    //We are only interested in watching changes in the AppBarLayout
    val dependencyMet = dependency is AppBarLayout
    info {
        "DEPENDENCY CHECK: " +
                "\nchild=${child.javaClass.simpleName}" +
                ", dependency=${dependency.javaClass.simpleName}" +
                if (dependencyMet) " <- YES!!!" else ""
    }
    setTag(child, "$value, ${LocalDateTime.now()}")
    return dependencyMet
}
```

### 2. `onDependentViewChanged(parent: CoordinatorLayout, child: FrameLayout, dependency: View)`
If the first method found that there were views that this behavior has a dependency on, then this 
second method will be called when those views change in any way. This provides this behavior the 
opportunity to take some action on the `child` view, in reaction to some change in the `dependency`
view.
```kotlin
/**
 * This is called when the [AppBarLayout] `dependency` changes in any way. This provides
 * us an opportunity to make a change to the [FrameLayout] `child`.
 */
override fun onDependentViewChanged(parent: CoordinatorLayout,
                                    child: FrameLayout,
                                    dependency: View): Boolean {
    info {
        "REACT TO DEPENDENCY CHANGE: " +
                "\nAppBarLayout changed!!!" +
                "\nchild=${child.javaClass.simpleName}" +
                ", dependency=${dependency.javaClass.simpleName}" +
                ", tag=${getTag(child)}"
    }
    val offset = -dependency.top
    child.translationY = offset.toFloat()
    return true
}
```

## Key Value pairs
The following code in the behavior class constructor shows how to retrieve the key-value pair 
that is declared in the XML layout above. When the custom behavior is declared in the XML the 
following constructor is called (with `Context` and `AttributeSet`). The key-value pairs can then
be retrieved from the `AttributeSet` argument.

```kotlin
class FooterBarBehavior(context: Context, attrs: AttributeSet) : 
    CoordinatorLayout.Behavior<FrameLayout>(context, attrs) {
    init {
        value = attrs.getAttributeValue("http://example.com", "my_key")
    }
    ...
}
```

Note that the namespace `http://example.com` must match whatever is defined in the XML. 
```xml
<FrameLayout xmlns:my_app="http://example.com"
        my_app:my_key="my_value"
        ...>
    ...
</FrameLayout>
```

# Nested scrolling

In Android, the child view that intercepts the touch event gets to consume them. This poses a 
problem for nested scrolling, since the child view that is nested underneath the parent needs to 
be able to share some of the touch events w/ its parent and not consume all of them. This 
goes against the way that touch events are consumed by default. This is where 
[`NestedScrollingParent`](https://goo.gl/YpqYMf)and [`NestedScrollingChild`](https://goo.gl/PFxcpH) 
come into play. 

The main methods that come into play when attempting to detect when the user has flung the 
`RecyclerView` and it has reached either the top or bottom and can't scroll anymore. Scrolling 
occurs while the user is still touching the screen and moving the `RecyclerView` around. As soon as
they lift their finger, the fling starts. This causes the `RecyclerView` to move up or down. At 
some point it hits the top / bottom edge and can't scroll anymore. At this point, we want to 
respond to this by either shaking or doing some other animation on the `RecyclerView` to let the 
user know that they've hit the edges of the `RecyclerView`. 

## 1. `onStartNestedScroll`
As soon as the user scrolls the `RecyclerView`, this method is called. 
This method gives our custom behavior the ability to snoop on the `RecyclerView` scrolling. 
We return `true` if the scrolling is occurring on the Y axis. Also, the `type` parameter is 
important since it lets us know whether this scrolling is happening with a user touching 
the screen (scroll) or not (fling).
```kotlin
override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout,
                                 child: FrameLayout,
                                 directTargetChild: View,
                                 target: View,
                                 axes: Int,
                                 type: Int): Boolean {
    if (type == ViewCompat.TYPE_NON_TOUCH) {
        info {
            "START NESTED SCROLL - NON_TOUCH - fling"
        }
    }else {
        info {
            "START NESTED SCROLL - TOUCH - scroll"
        }
    }
    return axes == ViewCompat.SCROLL_AXIS_VERTICAL ||
        super.onStartNestedScroll(
                coordinatorLayout, child, directTargetChild, target, axes, type)
}
```

## 2. `onNestedScroll`
This method is called repeatedly while the `RecyclerView` scrolls (fling or scroll). 
In this method we can determine if the user has flung the `RecyclerView` and it
can't scroll anymore. The `type` parameter lets us know if whether the user has flung 
(`TYPE_NON_TOUCH`) or if the user is scrolling (`TYPE_TOUCH`). And if `dyUnconsumed` 
has an integer (that is > 0) this means that the `RecyclerView` has stopped consuming 
the Y axis movement, the remainder dY value is unconsumed. This
provides us a trigger to then do something w/ this left over velocity, after the 
`RecyclerView` has stopped scrolling. This method keeps getting called until the 
nested scroll has ended (when `onStopNestedScroll` is called).

While this method is getting called, the user is free to move the `RecyclerView`. 
They can launch another scroll / fling gesture while the previous one is settling. In this case 
the `onNestedFling` method will be called, and that allows us to know that the fling / scroll 
operation begins again. The `FlingData` object is reset when this occurs. The reset also occurs 
when the `onStopNestedScroll` is called (and the overscroll comes to an end after settling).

## Apply physics based animation to the `RecyclerView`

In `FooterBarBehavior.onNestedScroll()` take the `FlingData.ratio` as a signal for how much force 
the user applied to the fling (after the RV has bumped into its top / bottom edge), and perform
an animation on the RecyclerView.

Here's an example of physics based animation on an entire RecyclerView.

```kotlin
private fun applyAnimationToRV(vY: Float, ratio: Int, target: RecyclerView) {
    val forceConstant = 500f
    val forceApplied = when (ratio) {
        in 0..5 -> 500f
        in 6..10 -> 1500f
        in 11..15 -> 5000f
        in 15..30 -> 10000f
        else -> 20000f
    }
    info { "NESTED SCROLL forceApplied=$forceApplied, ratio=$ratio, vY=$vY" }
    val scaleProperty = object : FloatPropertyCompat<View>("scaleProperty") {
        var value = 0f
        override fun getValue(view: View): Float {
            return value
        }

        override fun setValue(view: View, value: Float) {
            this.value = value
            val scaleValue = (value / forceConstant) + 1f
            info { "value = $value, scaleValue = $scaleValue" }
            view.scaleX = scaleValue
            view.translationY = value
        }
    }
    val force = (SpringForce()).apply {
        finalPosition = 1f
        dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
        stiffness = SpringForce.STIFFNESS_LOW
    }
    with(SpringAnimation(target, scaleProperty)) {
        spring = force
        setStartVelocity(forceApplied)
        start()
    }
}
```

In the code above, the `startVelocity` is derived from the `ratio` integer. The `ratio` is derived
from how hard the user flung the nested scrolling view (RV), and it represents the energy that
still remains after the RV has stopped scrolling up / down (since it's hit the top / bottom). This
`ratio` number varies between `0` and `30`. Also, the `vY` float that is passed to the 
`applyAnimationToRV()` method holds the fling direction. 
```
Finger drag down 👇 -> vY is positive
Finger drag up   👆 -> vY is negative
```

The `SpringForce` object determines how bouncy and firm the spring motion is. And ultimately this 
force will go to `1f` at the end of the animation. However, depending on the size of the 
`startVelocity` that is applied, this will cause the animated value to be much greater than 1f
and have it swing between positive and negative float values. This `value` is applied to the
`translationY` property of the RV, which makes it move up and down in the Y axis. 

There's another value `scaleValue` that is derived from the `value` and the `forceConstant`. This
float `scaleValue` hovers around `1f`, it goes a little below and a little above, but not too much.
It doesn't go between positive and negative values (like `value` does). The `scaleValue` is then
applied to the `scaleX` property of the RV. 

Together, this makes the RV bouncy, like a marshmallow 😁.