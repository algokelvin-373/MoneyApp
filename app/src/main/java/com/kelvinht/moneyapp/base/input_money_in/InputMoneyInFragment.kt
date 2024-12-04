
import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.kelvinht.moneyapp.R
import com.kelvinht.moneyapp.databinding.FragmentAddTransactionBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InputMoneyInFragment : Fragment() {
    private lateinit var binding: FragmentAddTransactionBinding
    private lateinit var photoFile: File

    companion object {
        const val CAMERA_REQUEST_CODE = 1001
        const val PERMISSION_REQUEST_CODE = 1002
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.txtType.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.dialog_list_type)
            dialog.show()
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
        }

        binding.imgPhotoSend.setOnClickListener {
            openCamera()
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(requireActivity().packageManager) != null) {
            // Membuat file untuk menyimpan gambar
            val storageDir = File(requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "money_app")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            photoFile = File(storageDir, "IMG_${timeStamp}.jpg")

            val photoURI: Uri = FileProvider.getUriForFile(requireContext(), "${requireActivity().packageName}.fileprovider", photoFile)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Menampilkan gambar dari file
            binding.imgPhotoSend.setImageURI(Uri.fromFile(photoFile))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Izin diberikan", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Izin ditolak", Toast.LENGTH_SHORT).show()
            }
        }
    }
}