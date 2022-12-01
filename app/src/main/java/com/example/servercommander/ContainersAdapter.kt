package com.example.servercommander

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView

class ContainersAdapter(private val mContainers: List<Container>) : RecyclerView.Adapter<ContainersAdapter.ViewHolder>() {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Your holder should contain and initialize a member variable
        // for any view that will be set as you render a row

        val dockerAppName = itemView.findViewById<TextView>(R.id.dockerAppName)
        val dockerAppStatus = itemView.findViewById<TextView>(R.id.dockerAppStatus)
        val dockerAppRuntime = itemView.findViewById<TextView>(R.id.dockerAppRuntime)
        val buttonContainerUp = itemView.findViewById<AppCompatImageButton>(R.id.buttonContainerStart)
        val buttonContainerDown = itemView.findViewById<AppCompatImageButton>(R.id.buttonContainerDown)
        val buttonContainerRestart = itemView.findViewById<AppCompatImageButton>(R.id.buttonContainerRestart)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        // Inflate the custom layout
        val contactView = inflater.inflate(R.layout.docker_app_item, parent, false)
        // Return a new holder instance
        return ViewHolder(contactView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val container = mContainers[position]

        val dockerAppName = holder.dockerAppName
        dockerAppName.text = container.name
    }

    override fun getItemCount(): Int {
        return mContainers.size
    }
}