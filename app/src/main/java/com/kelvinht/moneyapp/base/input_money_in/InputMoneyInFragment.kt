
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
import androidx.lifecycle.ViewModelProvider
import com.kelvinht.moneyapp.base.input_money_in.InputMoneyInViewModel
import com.kelvinht.moneyapp.base.input_money_in.InputMoneyInViewModelFactory
import com.kelvinht.moneyapp.data.MoneyIn
import com.kelvinht.moneyapp.databinding.DialogListTypeBinding
import com.kelvinht.moneyapp.databinding.FragmentAddMoneyInBinding
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class InputMoneyInFragment : Fragment() {
    private lateinit var binding: FragmentAddMoneyInBinding
    private lateinit var viewModel: InputMoneyInViewModel
    private lateinit var viewModelFactory: InputMoneyInViewModelFactory
    private lateinit var photoFile: File
    private lateinit var strNamePhoto: String
    private lateinit var typeData: String

    companion object {
        const val CAMERA_REQUEST_CODE = 1001
        const val PERMISSION_REQUEST_CODE = 1002
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddMoneyInBinding.inflate(inflater, container, false)
        viewModelFactory = InputMoneyInViewModelFactory(requireActivity(), "")
        viewModel = ViewModelProvider(this, viewModelFactory)[InputMoneyInViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.txtType.setOnClickListener {
            val dialog = Dialog(requireContext())
            val bindingDialog = DialogListTypeBinding.inflate(LayoutInflater.from(requireContext()))
            dialog.setContentView(bindingDialog.root)
            bindingDialog.txtPendapatanLain.setOnClickListener {
                typeData = "Pendapatan Lain"
                binding.txtType.text = typeData
                dialog.dismiss()
            }
            bindingDialog.txtNonPendapatan.setOnClickListener {
                typeData = "Non Pendapatan"
                binding.txtType.text = typeData
                dialog.dismiss()
            }
            dialog.show()
        }

        permissionCamera()

        binding.imgPhotoSend.setOnClickListener {
            openCamera()
        }
        binding.btnSave.setOnClickListener {
            val currentDateTime = LocalDateTime.now()
            val dateFormatted = DateTimeFormatter.ofPattern("dd MMM yyyy")
            val timeFormatted = DateTimeFormatter.ofPattern("HH:mm:ss")

            val moneyIn = MoneyIn(
                dataInto = binding.edtDataTo.text.toString(),
                dataFrom = binding.edtDataFrom.text.toString(),
                amount = binding.edtAmount.text.toString().toInt(),
                description = binding.edtDescription.text.toString(),
                type = typeData,
                photoPath = strNamePhoto,
                time = currentDateTime.format(timeFormatted),
                date = currentDateTime.format(dateFormatted)
            )
            viewModel.insert(moneyIn).observe(viewLifecycleOwner) {
                if (it != null) {
                    Toast.makeText(requireContext(), "Success Save Transaction", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed Save Transaction", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkPermissionCamera(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
    }

    private fun permissionCamera() {
        if (checkPermissionCamera()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(requireActivity().packageManager) != null) {
            val storageDir = File(requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "money_app")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            strNamePhoto = "IMG_${timeStamp}.jpg"
            photoFile = File(storageDir, strNamePhoto)

            val photoURI: Uri = FileProvider.getUriForFile(requireContext(), "${requireActivity().packageName}.fileprovider", photoFile)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            binding.imgPhotoSend.setImageURI(Uri.fromFile(photoFile))
        }
    }

    @Deprecated("Deprecated in Java")
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