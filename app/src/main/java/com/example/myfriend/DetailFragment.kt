package com.example.myfriend

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.room.Room
import coil.load
import com.example.myfriend.databinding.FragmentDetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private val args: DetailFragmentArgs by navArgs()
    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java, "friends-db"
        ).build()

        CoroutineScope(Dispatchers.IO).launch {
            val friend = db.friendDao().getAll().find { it.id == args.friendId }
            withContext(Dispatchers.Main) {
                friend?.let {
                    binding.tvName.text = it.name
                    binding.tvSchool.text = it.school
                    binding.tvBio.text = it.bio ?: "Bio tidak tersedia"
                    binding.ivPhoto.load(it.photoUri ?: R.drawable.placeholder)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_detail, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                val action = DetailFragmentDirections.actionDetailFragmentToEditFriendFragment(args.friendId)
                findNavController().navigate(action)
                true
            }
            R.id.action_delete -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Konfirmasi Hapus")
                    .setMessage("Hapus teman ini?")
                    .setPositiveButton("Ya") { _, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            val friend = db.friendDao().getAll().find { it.id == args.friendId }
                            friend?.let { db.friendDao().delete(it) }
                            withContext(Dispatchers.Main) {
                                findNavController().popBackStack()
                            }
                        }
                    }
                    .setNegativeButton("Tidak", null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}