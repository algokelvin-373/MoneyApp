package com.kelvinht.moneyapp.base.list_money_in

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kelvinht.moneyapp.R
import com.kelvinht.moneyapp.adapter.MoneyInLandsAdapter
import com.kelvinht.moneyapp.adapter.onclick.OnClickMoneyInItem
import com.kelvinht.moneyapp.base.input_money_in.InputMoneyInFragment
import com.kelvinht.moneyapp.data.MoneyIn
import com.kelvinht.moneyapp.databinding.DialogConfirmDeleteBinding
import com.kelvinht.moneyapp.databinding.FragmentListMoneyInBinding
import com.kelvinht.moneyapp.utils.GlobalVariable
import com.kelvinht.moneyapp.utils.data.ListMoneyInUtils

class ListMoneyInFragment : Fragment(), OnClickMoneyInItem {
    private lateinit var binding: FragmentListMoneyInBinding
    private lateinit var viewModel: ListMoneyInViewModel
    private lateinit var viewModelFactory: ListMoneyInViewModelFactory
    private lateinit var adapterLandscape: MoneyInLandsAdapter
    private lateinit var bounds: Rect
    private lateinit var context: Context

    @SuppressLint("NewApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        context = requireContext()
        binding = FragmentListMoneyInBinding.inflate(inflater, container, false)
        viewModelFactory = ListMoneyInViewModelFactory(requireContext())
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory)[ListMoneyInViewModel::class.java]

        (activity as AppCompatActivity).supportActionBar?.title = context.getString(R.string.title_header)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        val windowManager = requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = windowManager.currentWindowMetrics
        bounds = metrics.bounds

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showListMoneyIn()
        binding.btnAddTransaction.setOnClickListener {
            goToInputMoneyInFragment(InputMoneyInFragment())
        }
    }

    private fun showListMoneyIn() {
        viewModel.getAllTransaction().observe(viewLifecycleOwner) { data ->
            val listMoneyIn = ListMoneyInUtils.modifierListMoneyIn(context, bounds, data as ArrayList<MoneyIn>)
            adapterLandscape = MoneyInLandsAdapter(bounds, listMoneyIn, this)
            binding.rvMoneyIn.layoutManager = LinearLayoutManager(requireContext())
            binding.rvMoneyIn.adapter = adapterLandscape
        }
    }

    override fun editMoneyIn(moneyIn: MoneyIn) {
        val fragment = InputMoneyInFragment()
        val bundle = Bundle()
        bundle.putParcelable(GlobalVariable.MONEY_IN_BUNDLE, moneyIn)
        fragment.arguments = bundle
        goToInputMoneyInFragment(fragment)
    }

    override fun deleteMoneyIn(moneyIn: MoneyIn) {
        val dialog = Dialog(requireContext())
        val bindingDialog = DialogConfirmDeleteBinding.inflate(LayoutInflater.from(requireContext()))
        dialog.setContentView(bindingDialog.root)
        bindingDialog.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        bindingDialog.btnDelete.setOnClickListener {
            viewModel.deleteMoneyIn(moneyIn).observe(viewLifecycleOwner) {
                if (it != null) {
                    Toast.makeText(requireContext(), getString(R.string.message_delete_money_in_success), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.message_delete_money_in_failed), Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
            showListMoneyIn()
        }
        dialog.show()
    }

    private fun goToInputMoneyInFragment(fragment: InputMoneyInFragment) {
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
}