package com.example.myfriend

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.room.Room
import coil.load
import com.example.myfriend.databinding.FragmentEditFriendBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class EditFriendFragment : Fragment() {
    private var _binding: FragmentEditFriendBinding? = null
    private val binding get() = _binding!!
    private val args: EditFriendFragmentArgs by navArgs()
    private lateinit var db: AppDatabase
    private var photoUri: Uri? = null

    private val requestPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.all { it.value }) {
            showPhotoSourceDialog()
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle("Izin Ditolak")
                .setMessage("Aplikasi memerlukan izin kamera dan penyimpanan untuk mengambil atau memilih foto.")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            photoUri = it
            binding.ivPhoto.load(it) {
                placeholder(R.drawable.placeholder)
                error(R.drawable.placeholder)
            }
        }
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoUri != null) {
            binding.ivPhoto.load(photoUri) {
                placeholder(R.drawable.placeholder)
                error(R.drawable.placeholder)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditFriendBinding.inflate(inflater, container, false)
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
                    binding.etName.setText(it.name)
                    binding.etSchool.setText(it.school)
                    binding.etBio.setText(it.bio)
                    binding.ivPhoto.load(it.photoUri ?: R.drawable.placeholder) {
                        placeholder(R.drawable.placeholder)
                        error(R.drawable.placeholder)
                    }
                    photoUri = it.photoUri?.let { uri -> Uri.parse(uri) }
                }
            }
        }

        binding.btnPhoto.setOnClickListener {
            checkPermissionsAndShowDialog()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_add_friiend, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                val name = binding.etName.text.toString()
                val school = binding.etSchool.text.toString()
                val bio = binding.etBio.text.toString()
                if (name.isNotBlank() && school.isNotBlank()) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Konfirmasi")
                        .setMessage("Simpan perubahan?")
                        .setPositiveButton("Ya") { _, _ ->
                            CoroutineScope(Dispatchers.IO).launch {
                                val friend = db.friendDao().getAll().find { it.id == args.friendId }
                                friend?.let {
                                    db.friendDao().update(it.copy(name = name, school = school, bio = bio, photoUri = photoUri?.toString()))
                                    withContext(Dispatchers.Main) {
                                        findNavController().popBackStack()
                                    }
                                }
                            }
                        }
                        .setNegativeButton("Tidak", null)
                        .show()
                } else {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Input Tidak Valid")
                        .setMessage("Nama dan sekolah harus diisi.")
                        .setPositiveButton("OK", null)
                        .show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkPermissionsAndShowDialog() {
        val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            showPhotoSourceDialog()
        } else {
            requestPermissions.launch(permissions)
        }
    }

    private fun showPhotoSourceDialog() {
        val items = mutableListOf("Galeri")
        val cameraIntent = android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(requireContext().packageManager) != null) {
            items.add(0, "Kamera")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Sumber Foto")
            .setItems(items.toTypedArray()) { _, which ->
                when (items[which]) {
                    "Kamera" -> {
                        photoUri = createImageUri()
                        photoUri?.let { takePicture.launch(it) } ?: run {
                            AlertDialog.Builder(requireContext())
                                .setTitle("Kesalahan")
                                .setMessage("Gagal membuat URI untuk foto.")
                                .setPositiveButton("OK", null)
                                .show()
                        }
                    }
                    "Galeri" -> {
                        pickImage.launch("image/*")
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun createImageUri(): Uri? {
        return try {
            val image = File(requireContext().getExternalFilesDir("Pictures"), "friend_${System.currentTimeMillis()}.jpg")
            FileProvider.getUriForFile(requireContext(), "com.example.myfriend.fileprovider", image)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}