package com.example.servercommander

class Container(val name: String, val isRunning: Boolean, val runtime: String) {

    companion object {

        fun createContainersList(numOfContainers: Int): ArrayList<Container> {
            val containers = ArrayList<Container>()
            for (i in 1..numOfContainers) {
                containers.add(Container("Container ", false, "Pull to refresh"))
            }
            return containers
        }
    }
}