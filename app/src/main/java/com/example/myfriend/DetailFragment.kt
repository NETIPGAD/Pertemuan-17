package com.example.myfriend

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
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
                    binding.ivPhoto.load(it.photoUri ?: R.drawable.placeholder)
                }
            }
        }

        binding.btnEdit.setOnClickListener {
            val action = DetailFragmentDirections.actionDetailFragmentToEditFriendFragment(args.friendId)
            findNavController().navigate(action)
        }

        binding.btnDelete.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi Hapus")
                .setMessage("Hapus teman ini?")
                .setPositiveButton("Ya") { _, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val friend = db.friendDao().getAll().find { it.id == args.friendId }
                        friend?.let { db.friendDao().delete(it) }
                        requireActivity().runOnUiThread {
                            findNavController().popBackStack()
                        }
                    }
                }
                .setNegativeButton("Tidak", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}