
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.kelvinht.moneyapp.base.add_transaction.AddTransactionViewModel
import com.kelvinht.moneyapp.databinding.FragmentAddTransactionBinding
import java.security.AccessController.getContext

class AddTransactionFragment : Fragment() {
    private lateinit var binding: FragmentAddTransactionBinding

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

        /*etFrom = view.findViewById<EditText>(R.id.etFrom)
        etAmount = view.findViewById<EditText>(R.id.etAmount)
        etDescription = view.findViewById<EditText>(R.id.etDescription)
        spType = view.findViewById<Spinner>(R.id.spType)
        btnSave = view.findViewById<Button>(R.id.btnSave)
        btnUploadPhoto = view.findViewById<Button>(R.id.btnUploadPhoto)

        viewModel = ViewModelProvider(this).get(AddTransactionViewModel::class.java)

        btnSave.setOnClickListener(View.OnClickListener { v: View? -> saveTransaction() })*/
    }

    private fun saveTransaction() {
        /*val from = etFrom!!.text.toString()
        val amount = etAmount!!.text.toString().toDouble()
        val description = etDescription!!.text.toString()
        val type = spType!!.selectedItem.toString()

        val transaction: Transaction = Transaction(from, amount, description, type)
        viewModel.insertTransaction(transaction)

        // Navigate back or show success message
        Toast.makeText(getContext(), "Transaksi Disimpan", Toast.LENGTH_SHORT).show()
        getActivity().onBackPressed()*/
    }
}