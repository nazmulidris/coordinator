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

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class AndroidVersionNamesAdapter(recyclerView: RecyclerView) :
        RecyclerView.Adapter<AndroidVersionNamesAdapter.SimpleHolder>() {

    private val items =
            arrayOf("Alpha", "Beta", "Cupcake", "Donut", "Eclair", "FroYo",
                    "Gingerbread", "Honeycomb", "Ice Cream Sandwich",
                    "Jelly Bean", "KitKat", "Lollipop", "Marshmallow",
                    "Nougat", "Oreo", "P", "Q")

    private val inflater: LayoutInflater = LayoutInflater.from(recyclerView.context)

    class SimpleHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text: TextView

        init {
            this.text = itemView.findViewById(R.id.text) as TextView
            itemView.isClickable = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleHolder {
        val itemView = inflater.inflate(R.layout.list_item, parent, false)
        return SimpleHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: SimpleHolder, position: Int) {
        viewHolder.text.text = items[position]
    }

    override fun getItemCount(): Int {
        return items.size
    }
}