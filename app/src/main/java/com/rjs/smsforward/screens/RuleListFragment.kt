package com.rjs.smsforward.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView

import com.rjs.smsforward.BaseActivity
import com.rjs.smsforward.R
import com.rjs.smsforward.SMSRuleAdapter
import com.rjs.smsforward.SMSRuleData
import com.rjs.smsforward.support.Constants
import com.rjs.smsforward.support.SharedPrefStorage

import org.json.JSONArray

import androidx.fragment.app.Fragment

class RuleListFragment : Fragment() {

    private val mBase: BaseActivity
        get() = requireActivity() as BaseActivity

    private var smsRuleList = ArrayList<SMSRuleData>()
    private var lvSmsRule: ListView? = null
    private var tvEmptyRules: TextView? = null
    private var smsRuleAdapter: SMSRuleAdapter? = null

    fun onRuleListUpdate(rlActionType: Constants.RLActionType, position: Int, smsRule: SMSRuleData?) {
        try {
            when (rlActionType) {
                Constants.RLActionType.RULE_ADD -> smsRule?.let { smsRuleList.add(it) }
                Constants.RLActionType.RULE_EDIT -> {
                    smsRuleList.removeAt(position)
                    smsRule?.let { smsRuleList.add(position, it) }
                }
                Constants.RLActionType.RULE_DELETE -> smsRuleList.removeAt(position)
            }
            smsRuleAdapter?.notifyDataSetChanged()
            updateEmptyState()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateEmptyState() {
        if (smsRuleList.isEmpty()) {
            tvEmptyRules?.visibility = View.VISIBLE
            lvSmsRule?.visibility = View.GONE
        } else {
            tvEmptyRules?.visibility = View.GONE
            lvSmsRule?.visibility = View.VISIBLE
        }
    }

    private fun changeRulePosition(upward: Boolean, fromPosition: Int) {
        try {
            val shiftPossible = if (upward) fromPosition > 0 else fromPosition < smsRuleList.size - 1
            if (shiftPossible) {
                val smsRuleData = SharedPrefStorage.getSMSRuleData(mBase, "[]")
                val smsRuleArr = JSONArray(smsRuleData)
                val fromSmsRule = smsRuleArr.optJSONObject(fromPosition)
                val toPosition = if (upward) fromPosition - 1 else fromPosition + 1
                val toSmsRule = smsRuleArr.optJSONObject(toPosition)

                smsRuleArr.put(toPosition, fromSmsRule)
                smsRuleList.removeAt(toPosition)
                smsRuleList.add(toPosition, SMSRuleData(fromSmsRule))

                smsRuleArr.put(fromPosition, toSmsRule)
                smsRuleList.removeAt(fromPosition)
                smsRuleList.add(fromPosition, SMSRuleData(toSmsRule))

                SharedPrefStorage.saveSMSRuleData(mBase, smsRuleArr.toString())
                smsRuleAdapter?.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_rule_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            lvSmsRule = view.findViewById(R.id.lvSmsRule)
            tvEmptyRules = view.findViewById(R.id.tvEmptyRules)
            val smsRuleData = SharedPrefStorage.getSMSRuleData(mBase, "[]")
            val smsRuleArr = JSONArray(smsRuleData)
            smsRuleList.clear()
            for (i in 0 until smsRuleArr.length()) {
                smsRuleList.add(SMSRuleData(smsRuleArr.optJSONObject(i)))
            }
            smsRuleAdapter = SMSRuleAdapter(mBase, smsRuleList, object : SMSRuleAdapter.SMSRuleActionListener {
                override fun onRuleSelect(position: Int) {
                    mBase.showBottomSheetDialog(true, position)
                }

                override fun onRuleMoveUp(position: Int) {
                    changeRulePosition(true, position)
                }

                override fun onRuleMoveDown(position: Int) {
                    changeRulePosition(false, position)
                }
            })
            lvSmsRule?.adapter = smsRuleAdapter
            updateEmptyState()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
