package com.rain.arcast.ui.browse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.rain.arcast.R
import com.rain.arcast.adapter.RecyclerAdapter
import java.io.File

class BrowseFragment : Fragment() {

    private lateinit var browseViewModel: BrowseViewModel

    lateinit var recyclerList: RecyclerView
    lateinit var adapter: RecyclerAdapter

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        browseViewModel =
                ViewModelProvider(this).get(BrowseViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_browse, container, false)

        adapter = RecyclerAdapter(requireContext())
        recyclerList = root.findViewById(R.id.recyclerList)
        recyclerList.adapter = adapter

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        refreshList()
    }

    private fun refreshList() {
        val listRef: StorageReference = FirebaseStorage.getInstance().reference.child("images" + File.separator)

        listRef.listAll()
                .addOnSuccessListener {
                    it.items.forEach { task ->
                        task.getBytes(1048576).addOnSuccessListener {
                            adapter.addBitmap(it)
                        }
                                .addOnFailureListener {
                                    Toast.makeText(requireContext(), "Failed! Error: " + it.message, Toast.LENGTH_SHORT).show()
                                }
                    }
                    Toast.makeText(requireContext(),
                            "Success!",
                            Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { task ->
                    Toast.makeText(requireContext(),
                            "Failed to get images! Error: " + task.message,
                            Toast.LENGTH_SHORT).show()
                }
    }

}