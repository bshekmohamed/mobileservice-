package com.secondlife.mobile.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.secondlife.mobile.data.model.Booking
import com.secondlife.mobile.databinding.FragmentBookingsBinding
import com.secondlife.mobile.ui.adapters.BookingAdapter
import java.util.Date

class BookingsFragment : Fragment() {

    private var _binding: FragmentBookingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var bookingAdapter: BookingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadBookings()
    }

    private fun setupRecyclerView() {
        bookingAdapter = BookingAdapter()
        binding.bookingsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bookingAdapter
        }
    }

    private fun loadBookings() {
        val bookings = listOf(
            Booking(1, "Screen Repair", "Pending", Date(), 999.0),
            Booking(2, "Battery Replacement", "Completed", Date(), 1499.0)
        )
        bookingAdapter.submitList(bookings)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}