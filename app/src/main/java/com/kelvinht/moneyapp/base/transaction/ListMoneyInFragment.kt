package com.kelvinht.moneyapp.base.transaction

import InputMoneyInFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kelvinht.moneyapp.R
import com.kelvinht.moneyapp.adapter.MoneyInAdapter
import com.kelvinht.moneyapp.data.Transaction
import com.kelvinht.moneyapp.databinding.FragmentTransactionBinding

class ListMoneyInFragment : Fragment() {
    private lateinit var binding: FragmentTransactionBinding
    private lateinit var viewModel: ListMoneyInViewModel
    private lateinit var viewModelFactory: ListMoneyInViewModelFactory
    private lateinit var adapter: MoneyInAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTransactionBinding.inflate(inflater, container, false)
        viewModelFactory = ListMoneyInViewModelFactory(requireContext())
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory)[ListMoneyInViewModel::class.java]
        (activity as AppCompatActivity).supportActionBar?.title = "Uang Masuk"
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getAllTransaction().observe(viewLifecycleOwner) { data ->
            adapter = MoneyInAdapter()
            adapter.setList(data as ArrayList<Transaction>)
            binding.rvMoneyIn.layoutManager = LinearLayoutManager(requireContext())
            binding.rvMoneyIn.adapter = adapter
        }

        binding.btnAddTransaction.setOnClickListener {
            val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragment_container, InputMoneyInFragment())
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }
    }

}