package com.kelvinht.moneyapp.base.list_money_in

import InputMoneyInFragment
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kelvinht.moneyapp.R
import com.kelvinht.moneyapp.adapter.MoneyInAdapter
import com.kelvinht.moneyapp.adapter.MoneyInLandsAdapter
import com.kelvinht.moneyapp.data.MoneyInTitle
import com.kelvinht.moneyapp.data.MoneyInTotal
import com.kelvinht.moneyapp.data.MoneyIn
import com.kelvinht.moneyapp.databinding.FragmentListMoneyInBinding

class ListMoneyInFragment : Fragment() {
    private lateinit var binding: FragmentListMoneyInBinding
    private lateinit var viewModel: ListMoneyInViewModel
    private lateinit var viewModelFactory: ListMoneyInViewModelFactory
    private lateinit var adapter: MoneyInAdapter
    private lateinit var adapterLandscape: MoneyInLandsAdapter

    private fun modifierListMoneyIn(list: ArrayList<MoneyIn>): ArrayList<Any> {
        val result = ArrayList<Any>()
        val groupedTransactions = list.groupBy { it.date } // Make Group Based On Date
        groupedTransactions.entries.sortedByDescending {
            it.key // Sorted By Date Descending
        }.forEach { data ->
            val date = data.key
            val moneyIn = data.value
            val total = moneyIn.sumOf { it.amount }

            result.add(MoneyInTitle(date = date)) // for data title date money in
            result.addAll(moneyIn) // for data money in based on date
            result.add(MoneyInTotal(date = date, totalAmount = total)) // for data total money in based on date
        }
        return result
    }

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        if (displayMetrics.widthPixels > displayMetrics.heightPixels) {
            Toast.makeText(requireContext(), "Landscape Mode", Toast.LENGTH_SHORT).show()
            modeLandscape()
        } else {
            Toast.makeText(requireContext(), "Portrait Mode", Toast.LENGTH_SHORT).show()
            modePortrait()
        }
        binding.btnAddTransaction.setOnClickListener {
            val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragment_container, InputMoneyInFragment())
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }
    }

    private fun modePortrait() {
        viewModel.getAllTransaction().observe(viewLifecycleOwner) { data ->
            adapter = MoneyInAdapter()
            adapter.setList(data as ArrayList<MoneyIn>)
            binding.rvMoneyIn.layoutManager = LinearLayoutManager(requireContext())
            binding.rvMoneyIn.adapter = adapter
        }
    }

    private fun modeLandscape() {
        val list = ArrayList<MoneyIn>()
        list.add(MoneyIn(0, "Kasir Perangkat 1", "Bos", "Tambahan Modal", "Pendapatan Lain", "/path", "13:00", "25 Maret 2022", 100000))
        list.add(MoneyIn(0, "Kasir Perangkat 1", "Bos", "Tambahan Modal", "Pendapatan Lain", "/path", "13:00", "25 Maret 2022", 100000))
        list.add(MoneyIn(0, "Kasir Perangkat 1", "Bos", "Tambahan Modal", "Pendapatan Lain", "/path", "13:00", "24 Maret 2022", 100000))
        list.add(MoneyIn(0, "Kasir Perangkat 1", "Bos", "Tambahan Modal", "Pendapatan Lain", "/path", "13:00", "24 Maret 2022", 100000))
        list.add(MoneyIn(0, "Kasir Perangkat 1", "Bos", "Tambahan Modal", "Pendapatan Lain", "/path", "13:00", "24 Maret 2022", 100000))
        list.add(MoneyIn(0, "Kasir Perangkat 1", "Bos", "Tambahan Modal", "Pendapatan Lain", "/path", "13:00", "24 Maret 2022", 100000))

        val listMoneyIn = modifierListMoneyIn(list)
        val adapter = MoneyInLandsAdapter()
        adapter.setList(listMoneyIn)
        binding.rvMoneyIn.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMoneyIn.adapter = adapter
    }

}