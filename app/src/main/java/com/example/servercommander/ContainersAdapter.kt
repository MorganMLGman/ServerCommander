package com.example.servercommander

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class ContainersAdapter(var mContainers: List<Container>, var mListener: OnViewClickListener) : RecyclerView.Adapter<ContainersAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View, mListener: OnViewClickListener) : RecyclerView.ViewHolder(itemView) {

        val dockerAppName: TextView = itemView.findViewById(R.id.dockerAppName)
        val dockerAppStatus: TextView = itemView.findViewById(R.id.dockerAppStatus)
        val dockerAppRuntime: TextView = itemView.findViewById(R.id.dockerAppRuntime)
        val buttonContainerUp: AppCompatImageButton = itemView.findViewById(R.id.buttonContainerStart)
        val buttonContainerDown: AppCompatImageButton = itemView.findViewById(R.id.buttonContainerDown)
        val buttonContainerRestart: AppCompatImageButton = itemView.findViewById(R.id.buttonContainerRestart)

        init {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val contactView = inflater.inflate(R.layout.docker_app_item, parent, false)
        return ViewHolder(contactView, mListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val container = mContainers[position]

        val dockerAppName = holder.dockerAppName
        val dockerAppStatus = holder.dockerAppStatus
        val dockerAppRuntime = holder.dockerAppRuntime

        dockerAppName.text = container.name

        if (container.isRunning) dockerAppStatus.text = "RUNNING"
        else dockerAppStatus.text = "STOPPED"

        dockerAppRuntime.text = container.runtime

        holder.itemView.setOnClickListener{
            mListener.onRowClickListener(holder.itemView, container)
        }

        holder.buttonContainerUp.setOnClickListener{
            mListener.onButtonStartClickListener(holder.buttonContainerUp, container)
        }

        holder.buttonContainerDown.setOnClickListener{
            mListener.onButtonStopClickListener(holder.buttonContainerDown, container)
        }

        holder.buttonContainerRestart.setOnClickListener {
            mListener.onButtonRestartClickListener(holder.buttonContainerRestart, container)
        }

    }

    override fun getItemCount(): Int {
        return mContainers.size
    }

    fun updateList(newContainers: List<Container>){
        val diffCallback = ContainersDiffCallback(this.mContainers, newContainers)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        diffResult.dispatchUpdatesTo(this)
        this.mContainers = newContainers
    }

    interface OnViewClickListener{
        fun onRowClickListener(view: View, container: Container)
        fun onButtonStartClickListener(button: AppCompatImageButton, container: Container)
        fun onButtonStopClickListener(button: AppCompatImageButton, container: Container)
        fun onButtonRestartClickListener(button: AppCompatImageButton, container: Container)
    }
}