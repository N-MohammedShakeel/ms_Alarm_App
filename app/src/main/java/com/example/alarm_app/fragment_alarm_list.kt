package com.example.alarm_app

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alarm_app.databinding.FragmentAlarmListBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AlarmListFragment : Fragment() {

    private lateinit var binding: FragmentAlarmListBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val alarmList = mutableListOf<AlarmItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlarmListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("ALARM_PREFS", Context.MODE_PRIVATE)
        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Retrieve alarms from arguments if passed
        arguments?.let {
            val hour = it.getInt("HOUR", -1)
            val minute = it.getInt("MINUTE", -1)
            if (hour != -1 && minute != -1) {
                alarmList.add(AlarmItem(hour, minute, true))
            }
        }

        val adapter = AlarmListAdapter(alarmList) { position ->
            val alarm = alarmList[position]
            alarm.isActive = !alarm.isActive
            saveAlarmState(alarm)
        }
        recyclerView.adapter = adapter

        // Set up FloatingActionButton
        val fab: FloatingActionButton = view.findViewById(R.id.floataddAlarm)
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_alarmListFragment_to_createAlarmFragment)
        }
    }

    private fun saveAlarmState(alarm: AlarmItem) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("${alarm.hour}:${alarm.minute}", alarm.isActive)
        editor.apply()
    }
}

data class AlarmItem(val hour: Int, val minute: Int, var isActive: Boolean)

class AlarmListAdapter(
    private val items: List<AlarmItem>,
    private val onSwitchChanged: (Int) -> Unit
) : RecyclerView.Adapter<AlarmListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alarm, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val switch: CompoundButton = itemView.findViewById(R.id.alarm_switch)
        private val timeTextView: TextView = itemView.findViewById(R.id.alarm_time)

        fun bind(item: AlarmItem) {
            timeTextView.text = String.format("%02d:%02d", item.hour, item.minute)
            switch.isChecked = item.isActive
            switch.setOnCheckedChangeListener { _, _ -> onSwitchChanged(adapterPosition) }
        }
    }
}
