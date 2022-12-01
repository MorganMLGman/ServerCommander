package com.example.servercommander

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView

class ContainersAdapter(private val mContainers: List<Container>) : RecyclerView.Adapter<ContainersAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val dockerAppName = itemView.findViewById<TextView>(R.id.dockerAppName)
        val dockerAppStatus = itemView.findViewById<TextView>(R.id.dockerAppStatus)
        val dockerAppRuntime = itemView.findViewById<TextView>(R.id.dockerAppRuntime)
        val buttonContainerUp = itemView.findViewById<AppCompatImageButton>(R.id.buttonContainerStart)
        val buttonContainerDown = itemView.findViewById<AppCompatImageButton>(R.id.buttonContainerDown)
        val buttonContainerRestart = itemView.findViewById<AppCompatImageButton>(R.id.buttonContainerRestart)

        init {
            buttonContainerUp.setOnClickListener {
                Toast.makeText(itemView.context, "${dockerAppName.text} button UP", Toast.LENGTH_SHORT).show()
            }

            buttonContainerDown.setOnClickListener {
                Toast.makeText(itemView.context, "${dockerAppName.text} button DOWN", Toast.LENGTH_SHORT).show()
            }

            buttonContainerRestart.setOnClickListener {
                Toast.makeText(itemView.context, "${dockerAppName.text} button RESTART", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val contactView = inflater.inflate(R.layout.docker_app_item, parent, false)
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