package com.rjs.smsforward

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

class SMSRuleAdapter(
    private val baseActivity: BaseActivity,
    private val smsRuleList: List<SMSRuleData>,
    private val smsRuleActionListener: SMSRuleActionListener?
) : ArrayAdapter<SMSRuleData>(baseActivity, R.layout.activity_main, smsRuleList) {

    interface SMSRuleActionListener {
        fun onRuleSelect(position: Int)
        fun onRuleMoveUp(position: Int)
        fun onRuleMoveDown(position: Int)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = baseActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(R.layout.listview_sms_rule_row_item, parent, false)

        val rlRuleContainer = rowView.findViewById<RelativeLayout>(R.id.rlRuleContainer)
        val llNegKeyContainer = rowView.findViewById<LinearLayout>(R.id.llNegKeyContainer)
        val txtRuleKeyword = rowView.findViewById<TextView>(R.id.txtRuleKeyword)
        val txtRuleNegKeyword = rowView.findViewById<TextView>(R.id.txtRuleNegKeyword)
        val txtRuleForward = rowView.findViewById<TextView>(R.id.txtRuleForward)
        val btnUp = rowView.findViewById<ImageView>(R.id.imageViewUp)
        val btnDown = rowView.findViewById<ImageView>(R.id.imageViewDown)

        val smsRuleData = smsRuleList[position]

        txtRuleKeyword.text = smsRuleData.keyword
        txtRuleForward.text = smsRuleData.phoneNumber

        if (smsRuleData.negKeyword.isNotEmpty()) {
            llNegKeyContainer.visibility = View.VISIBLE
            txtRuleNegKeyword.text = smsRuleData.negKeyword
        } else {
            llNegKeyContainer.visibility = View.GONE
        }

        btnUp.setImageDrawable(ContextCompat.getDrawable(
            baseActivity,
            if (position == 0) R.drawable.arrow_up_gray else R.drawable.arrow_up
        ))
        btnDown.setImageDrawable(ContextCompat.getDrawable(
            baseActivity,
            if (position == smsRuleList.size - 1) R.drawable.arrow_down_gray else R.drawable.arrow_down
        ))

        rlRuleContainer.setOnClickListener {
            smsRuleActionListener?.onRuleSelect(position)
        }
        btnUp.setOnClickListener {
            smsRuleActionListener?.onRuleMoveUp(position)
        }
        btnDown.setOnClickListener {
            smsRuleActionListener?.onRuleMoveDown(position)
        }

        return rowView
    }
}
