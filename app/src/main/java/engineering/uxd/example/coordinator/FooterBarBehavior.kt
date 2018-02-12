/*
 * Copyright 2018 Nazmul Idris. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package engineering.uxd.example.coordinator

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import java.time.LocalDateTime

/**
 * Simple layout behavior that will track the state of the AppBarLayout
 * and match its offset for a corresponding footer.
 */
class FooterBarBehavior : CoordinatorLayout.Behavior<FrameLayout>, AnkoLogger {
    lateinit var value: String

    //Required to instantiate as a default behavior
    constructor()

    //Required to attach behavior via XML
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        // Get this attribute from the XML layout
        value = attrs.getAttributeValue("http://example.com", "my_key")
    }

    //This is called to determine which views this behavior depends on
    override fun layoutDependsOn(parent: CoordinatorLayout,
                                 child: FrameLayout,
                                 dependency: View): Boolean {
        //We are watching changes in the AppBarLayout
        val dependencyMet = dependency is AppBarLayout
        debug {
            "CHECK: " +
                    "\nchild=${child.javaClass.simpleName}" +
                    ", dependency=${dependency.javaClass.simpleName}" +
                    if (dependencyMet) " <- YES!!!" else ""
        }
        setTag(child, "$value, ${LocalDateTime.now()}")
        return dependencyMet
    }

    //This is called for each change to a dependent view
    override fun onDependentViewChanged(parent: CoordinatorLayout,
                                        child: FrameLayout,
                                        dependency: View): Boolean {
        debug {
            "ACT: "+
                    "\nAppBarLayout changed!!!"+
                    "\nchild=${child.javaClass.simpleName}" +
                    ", dependency=${dependency.javaClass.simpleName}" +
                    ", tag=${getTag(child)}"
        }
        val offset = -dependency.top
        child.translationY = offset.toFloat()
        return true
    }

}