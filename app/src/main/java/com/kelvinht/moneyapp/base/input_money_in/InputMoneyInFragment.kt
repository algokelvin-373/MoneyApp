package com.kelvinht.moneyapp.base.input_money_in

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
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
import com.kelvinht.moneyapp.R
import com.kelvinht.moneyapp.data.MoneyIn
import com.kelvinht.moneyapp.databinding.DialogListTypeBinding
import com.kelvinht.moneyapp.databinding.FragmentAddMoneyInBinding
import com.kelvinht.moneyapp.utils.GlobalVariable
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
    private lateinit var context: Context

    companion object {
        const val CAMERA_REQUEST_CODE = 1001
        const val PERMISSION_REQUEST_CODE = 1002
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        context = requireContext()
        binding = FragmentAddMoneyInBinding.inflate(inflater, container, false)
        viewModelFactory = InputMoneyInViewModelFactory(requireActivity(), "")
        viewModel = ViewModelProvider(this, viewModelFactory)[InputMoneyInViewModel::class.java]
        return binding.root
    }

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        permissionCamera()
        val moneyIn = arguments?.getParcelable<MoneyIn>(GlobalVariable.MONEY_IN_BUNDLE)
        if (moneyIn != null)
            setDataMoneyInUpdate(moneyIn)
        else {
            addDataMoneyIn()
            binding.btnChangePhoto.visibility = View.GONE
            binding.btnDeletePhoto.visibility = View.GONE
        }
        binding.btnChangePhoto.setOnClickListener {
            openCamera()
        }
    }

    private fun setDataMoneyInUpdate(moneyIn: MoneyIn) {
        typeData = moneyIn.type
        strNamePhoto = moneyIn.photoPath
        binding.apply {
            edtDataTo.setText(moneyIn.dataInto)
            edtDataFrom.setText(moneyIn.dataFrom)
            edtAmount.setText(moneyIn.amount.toString())
            edtDescription.setText(moneyIn.description)
            txtType.text = typeData
            btnSave.text = "Update"
        }

        val storageDir = File(requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "money_app")
        val filePhoto = File(storageDir, strNamePhoto)
        binding.imgPhotoSend.setImageURI(Uri.fromFile(filePhoto))

        binding.btnSave.setOnClickListener {
            val currentDateTime = LocalDateTime.now()
            val dateFormatted = DateTimeFormatter.ofPattern("dd MMM yyyy")
            val timeFormatted = DateTimeFormatter.ofPattern("HH:mm:ss")

            val moneyInUpdate = MoneyIn(
                id = moneyIn.id,
                dataInto = binding.edtDataTo.text.toString(),
                dataFrom = binding.edtDataFrom.text.toString(),
                amount = binding.edtAmount.text.toString().toInt(),
                description = binding.edtDescription.text.toString(),
                type = typeData,
                photoPath = strNamePhoto,
                time = currentDateTime.format(timeFormatted),
                date = currentDateTime.format(dateFormatted)
            )

            viewModel.update(moneyInUpdate).observe(viewLifecycleOwner) {
                if (it != null) {
                    Toast.makeText(context, getString(R.string.message_update_money_in_success), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, getString(R.string.message_update_money_in_failed), Toast.LENGTH_SHORT).show()
                }
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun addDataMoneyIn() {
        binding.txtType.setOnClickListener {
            showDialogChooseType()
        }
        binding.imgPhotoSend.setOnClickListener {
            openCamera()
        }
        binding.btnDeletePhoto.setOnClickListener {
            //binding.imgPhotoSend.setImageResource()
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
                    Toast.makeText(context, getString(R.string.message_save_money_in_success), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, R.string.message_save_money_in_failed, Toast.LENGTH_SHORT).show()
                }
                parentFragmentManager.popBackStack()
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

    private fun showDialogChooseType() {
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
            binding.btnChangePhoto.visibility = View.VISIBLE
            binding.btnDeletePhoto.visibility = View.VISIBLE
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Permission Rejected", Toast.LENGTH_SHORT).show()
            }
        }
    }
}