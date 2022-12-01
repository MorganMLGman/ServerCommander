package com.example.servercommander

class Container(val name: String, val isRunning: Boolean, val runtime: Int) {

    companion object {

        private var containerID = 0
        fun createContainersList(numOfContainers: Int): ArrayList<Container> {
            val containers = ArrayList<Container>()
            for (i in 1..numOfContainers) {
                containers.add(Container("Container " + ++containerID, true, containerID))
            }
            return containers
        }
    }
}