package com.rjs.smsforward.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.rjs.smsforward.R
import com.rjs.smsforward.support.SharedPrefStorage
import org.json.JSONArray

class LogsFragment : Fragment() {

    private var lvLogs: ListView? = null
    private var tvEmptyLogs: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_logs_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lvLogs = view.findViewById(R.id.lvLogs)
        tvEmptyLogs = view.findViewById(R.id.tvEmptyLogs)
        loadLogs()
    }

    override fun onResume() {
        super.onResume()
        loadLogs()
    }

    private fun loadLogs() {
        try {
            val logs = SharedPrefStorage.getLogs(requireContext())
            if (logs.length() == 0) {
                tvEmptyLogs?.visibility = View.VISIBLE
                lvLogs?.visibility = View.GONE
            } else {
                tvEmptyLogs?.visibility = View.GONE
                lvLogs?.visibility = View.VISIBLE
                lvLogs?.adapter = LogAdapter(logs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private inner class LogAdapter(private val logs: JSONArray) : BaseAdapter() {

        override fun getCount(): Int = logs.length()
        override fun getItem(position: Int): Any = logs.getJSONObject(position)
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(requireContext())
                .inflate(R.layout.listview_log_row_item, parent, false)

            val entry = logs.getJSONObject(position)
            val forwarded = entry.optBoolean("forwarded", false)

            val tvStatus = view.findViewById<TextView>(R.id.tvLogStatus)
            val tvTime = view.findViewById<TextView>(R.id.tvLogTime)
            val tvMessage = view.findViewById<TextView>(R.id.tvLogMessage)
            val tvResult = view.findViewById<TextView>(R.id.tvLogResult)

            tvStatus.text = if (forwarded) "✅" else "❌"
            tvTime.text = entry.optString("time", "")
            tvMessage.text = entry.optString("message", "")
            tvResult.text = entry.optString("result", "")

            return view
        }
    }
}
