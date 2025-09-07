package com.example.myfriend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myfriend.databinding.FragmentHomeBinding
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var friendAdapter: FriendAdapter
    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java, "friends-db"
        ).build()

        friendAdapter = FriendAdapter { friend ->
            val action = HomeFragmentDirections.actionHomeFragmentToDetailFragment(friend.id)
            findNavController().navigate(action)
        }
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2) // Mengubah ke GridLayoutManager dengan 2 kolom
            adapter = friendAdapter
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                loadFriends(newText ?: "")
                return true
            }
        })

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addFriendFragment)
        }

        loadFriends("")
    }

    private fun loadFriends(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val friends = if (query.isEmpty()) {
                db.friendDao().getAll()
            } else {
                db.friendDao().search("%$query%")
            }
            withContext(Dispatchers.Main) {
                friendAdapter.submitList(friends)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}