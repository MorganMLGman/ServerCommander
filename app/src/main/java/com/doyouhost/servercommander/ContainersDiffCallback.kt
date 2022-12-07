package com.doyouhost.servercommander

import androidx.recyclerview.widget.DiffUtil

open class ContainersDiffCallback(
    private val oldList: List<Container>,
    private val newList: List<Container>
): DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].name == newList[newItemPosition].name
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldContainer = oldList[oldItemPosition]
        val newContainer = newList[newItemPosition]

        return (oldContainer.name == newContainer.name) and
                (oldContainer.runtime == newContainer.runtime) and
                (oldContainer.isRunning == newContainer.isRunning)
    }
}