package com.rjs.smsforward

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import com.rjs.smsforward.support.Constants
import com.rjs.smsforward.support.SharedPrefStorage

import org.json.JSONArray
import org.json.JSONObject

import androidx.annotation.Nullable

class RuleAddEditDialog(
    private val baseActivity: BaseActivity
) : BottomSheetDialog(baseActivity) {

    private var etKeywords: EditText? = null
    private var etNegKeywords: EditText? = null
    private var etPhoneNumber: EditText? = null
    private var tvTitle: TextView? = null
    private var ivClose: ImageView? = null
    private var ivDelete: ImageView? = null
    private var llClose: LinearLayout? = null
    private var llDelete: LinearLayout? = null
    private var phoneInputLayout: TextInputLayout? = null
    private var cbStopProcessRule: CheckBox? = null
    private var btnRuleAddUpdate: Button? = null
    private var ruleEditPos = -1
    private var isUpdate = false

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rule_add_edit_form)
        llClose = findViewById(R.id.llClose)
        llDelete = findViewById(R.id.llDelete)
        ivClose = findViewById(R.id.ivClose)
        ivDelete = findViewById(R.id.ivDelete)
        tvTitle = findViewById(R.id.tvTitle)
        etKeywords = findViewById(R.id.etKeywords)
        etKeywords?.addTextChangedListener(etChangeListener)
        etNegKeywords = findViewById(R.id.etNegKeywords)
        phoneInputLayout = findViewById(R.id.filledTextFieldPhone)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        etPhoneNumber?.addTextChangedListener(etChangeListener)

        cbStopProcessRule = findViewById(R.id.cbStopProcessRule)

        btnRuleAddUpdate = findViewById(R.id.btnRuleAddUpdate)
        btnRuleAddUpdate?.setOnClickListener {
            addUpdateSMSPostRule()
        }
        llClose?.setOnClickListener {
            resetSMSRuleForm()
            dismiss()
        }
        llDelete?.setOnClickListener {
            removeRule(ruleEditPos)
        }
    }

    private fun removeRule(position: Int) {
        val builder = AlertDialog.Builder(baseActivity)
        builder.setTitle("Delete rule")
        builder.setMessage("Are you sure you wish to delete this rule?")
            .setPositiveButton("Yes") { _, _ ->
                try {
                    val smsRuleData = SharedPrefStorage.getSMSRuleData(baseActivity, "[]")
                    val smsRuleArr = JSONArray(smsRuleData)
                    smsRuleArr.remove(position)
                    SharedPrefStorage.saveSMSRuleData(baseActivity, smsRuleArr.toString())
                    baseActivity.ruleListUpdate(Constants.RLActionType.RULE_DELETE, position, null)
                    BaseActivity.recordItemEvent("rule_deleted", "RuleAddEditDialog")
                    resetSMSRuleForm()
                    dismiss()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }
        val alert = builder.create()
        alert.show()
    }

    fun showDialog(update: Boolean, position: Int) {
        isUpdate = update
        ruleEditPos = position
        show()
        Handler(Looper.getMainLooper()).postDelayed({
            resetSMSRuleForm()
            if (update) {
                editRule()
            }
            llDelete?.visibility = if (isUpdate) View.VISIBLE else View.GONE
            tvTitle?.text = if (isUpdate) "Edit rule" else "Create new rule"
            btnRuleAddUpdate?.text = if (isUpdate) "Save changes" else "Add rule"
        }, 500)
    }

    private val etChangeListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            phoneInputLayout?.error = null
            if (btnRuleAddUpdate != null && etKeywords != null && etPhoneNumber != null) {
                val keywords = etKeywords?.text?.toString()?.trim() ?: ""
                val phoneNumber = etPhoneNumber?.text?.toString()?.trim() ?: ""
                btnRuleAddUpdate?.visibility = if (keywords.isNotEmpty() && phoneNumber.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        if (phone.length < 7 || phone.length > 15) return false
        if (!phone.matches(Regex("^\\+?[0-9]+$"))) return false
        return PhoneNumberUtils.isGlobalPhoneNumber(phone)
    }

    fun addUpdateSMSPostRule() {
        try {
            val smsRuleData = SharedPrefStorage.getSMSRuleData(baseActivity, "[]")
            val smsRuleArr = JSONArray(smsRuleData)
            val smsRule = JSONObject()
            val keywords = etKeywords?.text?.toString()?.trim() ?: ""
            val negKeywords = etNegKeywords?.text?.toString()?.trim() ?: ""
            val phoneNo = etPhoneNumber?.text?.toString()?.trim() ?: ""

            if (!isValidPhoneNumber(phoneNo)) {
                phoneInputLayout?.error = "Enter a valid phone number with country code (e.g. +14155551234)"
                return
            }
            phoneInputLayout?.error = null

            smsRule.put(Constants.Storage.ruleKeyword.name, keywords)
            smsRule.put(Constants.Storage.ruleNegKeyword.name, negKeywords)
            smsRule.put(Constants.Storage.postType.name, Constants.PostItems.SMS.name)
            smsRule.put(Constants.Storage.stopProcessRule.name, cbStopProcessRule?.isChecked ?: false)
            smsRule.put(Constants.Storage.postForwardTo.name, phoneNo)

            if (isUpdate) {
                smsRuleArr.put(ruleEditPos, smsRule)
                baseActivity.ruleListUpdate(Constants.RLActionType.RULE_EDIT, ruleEditPos, SMSRuleData(smsRule))
                BaseActivity.recordItemEvent("rule_edited", "RuleAddEditDialog")
            } else {
                smsRuleArr.put(smsRule)
                baseActivity.ruleListUpdate(Constants.RLActionType.RULE_ADD, -1, SMSRuleData(smsRule))
                BaseActivity.recordItemEvent("rule_created", "RuleAddEditDialog")
            }

            SharedPrefStorage.saveSMSRuleData(baseActivity, smsRuleArr.toString())
            resetSMSRuleForm()
            dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun resetSMSRuleForm() {
        try {
            etKeywords?.setText("")
            etNegKeywords?.setText("")
            cbStopProcessRule?.isChecked = false
            etPhoneNumber?.setText("")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun editRule() {
        try {
            val smsRuleData = SharedPrefStorage.getSMSRuleData(baseActivity, "[]")
            val smsRuleArr = JSONArray(smsRuleData)
            val editRule = SMSRuleData(smsRuleArr.optJSONObject(ruleEditPos))
            etKeywords?.setText(editRule.keyword)
            etNegKeywords?.setText(editRule.negKeyword)
            cbStopProcessRule?.isChecked = editRule.stopProcessRule
            etPhoneNumber?.setText(editRule.phoneNumber)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
