package com.kelvinht.moneyapp.base.list_money_in

import InputMoneyInFragment
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
import com.kelvinht.moneyapp.data.MoneyIn
import com.kelvinht.moneyapp.data.MoneyInTitle
import com.kelvinht.moneyapp.data.MoneyInTotal
import com.kelvinht.moneyapp.databinding.DialogConfirmDeleteBinding
import com.kelvinht.moneyapp.databinding.FragmentListMoneyInBinding

class ListMoneyInFragment : Fragment(), OnClickMoneyInItem {
    private lateinit var binding: FragmentListMoneyInBinding
    private lateinit var viewModel: ListMoneyInViewModel
    private lateinit var viewModelFactory: ListMoneyInViewModelFactory
    private lateinit var adapterLandscape: MoneyInLandsAdapter
    private lateinit var bounds: Rect

    private fun modifierListMoneyIn(list: ArrayList<MoneyIn>): ArrayList<Any> {
        val result = ArrayList<Any>()
        val groupedTransactions = list.groupBy { it.date } // Make Group Based On Date
        groupedTransactions.entries.sortedByDescending {
            it.key // Sorted By Date Descending
        }.forEach { data ->
            val date = data.key
            val moneyIn = data.value
            val total = moneyIn.sumOf { it.amount }

            if (bounds.width() > bounds.height()) { // Mode Landscape
                result.add(MoneyInTitle(date = date)) // for data title date money in
                result.addAll(moneyIn) // for data money in based on date
                result.add(MoneyInTotal(date = date, totalAmount = total)) // for data total money in based on date
            } else { // Mode Portrait
                result.add(MoneyInTotal(date = date, totalAmount = total)) // for data total money in based on date
                result.addAll(moneyIn) // for data money in based on date
            }
        }
        return result
    }

    @SuppressLint("NewApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListMoneyInBinding.inflate(inflater, container, false)
        viewModelFactory = ListMoneyInViewModelFactory(requireContext())
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory)[ListMoneyInViewModel::class.java]
        (activity as AppCompatActivity).supportActionBar?.title = "Uang Masuk"
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
        val windowManager = requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = windowManager.currentWindowMetrics
        bounds = metrics.bounds
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getAllTransaction().observe(viewLifecycleOwner) { data ->
            val listMoneyIn = modifierListMoneyIn(data as ArrayList<MoneyIn>)
            adapterLandscape = MoneyInLandsAdapter(bounds, this)
            adapterLandscape.setList(listMoneyIn)
            binding.rvMoneyIn.layoutManager = LinearLayoutManager(requireContext())
            binding.rvMoneyIn.adapter = adapterLandscape
        }
        binding.btnAddTransaction.setOnClickListener {
            val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragment_container, InputMoneyInFragment())
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }
    }

    private fun showListMoneyIn() {

    }

    override fun editMoneyIn(moneyIn: MoneyIn) {
        val fragment = InputMoneyInFragment()
        val bundle = Bundle()
        bundle.putParcelable("data_money_in", moneyIn)
        fragment.arguments = bundle
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
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
                    Toast.makeText(requireContext(), "Success Delete Transaction", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed Delete Transaction", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
            viewModel.getAllTransaction().observe(viewLifecycleOwner) { data ->
                val listMoneyIn = modifierListMoneyIn(data as ArrayList<MoneyIn>)
                adapterLandscape = MoneyInLandsAdapter(bounds, this)
                adapterLandscape.setList(listMoneyIn)
                binding.rvMoneyIn.layoutManager = LinearLayoutManager(requireContext())
                binding.rvMoneyIn.adapter = adapterLandscape
            }
        }
        dialog.show()
    }
}