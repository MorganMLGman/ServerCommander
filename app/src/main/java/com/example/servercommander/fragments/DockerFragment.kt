package com.example.servercommander.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.servercommander.Container
import com.example.servercommander.ContainersAdapter
import com.example.servercommander.R
import com.example.servercommander.SshConnection
import com.example.servercommander.databinding.FragmentDockerBinding

class DockerFragment : Fragment() {

    private var _binding: FragmentDockerBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPref: SharedPreferences
    private lateinit var sshConnection: SshConnection
    private lateinit var containers: ArrayList<Container>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = requireActivity().getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDockerBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = binding.dockerRecyclerView
        containers = Container.createContainersList(20)
        val adapter = ContainersAdapter(containers)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        val swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            Toast.makeText(context, "Refresh", Toast.LENGTH_SHORT).show()
            containers.clear()
            containers = Container.createContainersList(20)
            recyclerView.adapter!!.notifyDataSetChanged()
            swipeRefreshLayout.isRefreshing = false
        }

        if( !(::sshConnection.isInitialized or sharedPref.getBoolean(getString(R.string.connectionTested), false)))
        {
            // TODO: DISABLE BUTTONS
        }
        else
        {
            // TODO: ENABLE BUTTONS
            if (sharedPref.contains(getString(R.string.server_url)) and
                sharedPref.contains(getString(R.string.username)) and
                sharedPref.contains(getString(R.string.pubkey)) and
                sharedPref.contains(getString(R.string.connectionTested))) {

                sshConnection = SshConnection(
                    sharedPref.getString(getString(R.string.server_url), "").toString(),
                    22,
                    sharedPref.getString(getString(R.string.username), "").toString(),
                    sharedPref.getString(getString(R.string.pubkey), "").toString()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()

    }
}