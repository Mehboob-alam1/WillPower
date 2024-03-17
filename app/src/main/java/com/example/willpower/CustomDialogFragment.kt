package com.example.willpower

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class CustomDialogFragment : DialogFragment() {

    var onPositiveClick: (() -> Unit)? = null
    var onNegativeClick: (() -> Unit)? = null

    override fun onStart() {
        super.onStart()
        val window = dialog?.window

        // Set transparent background
        /*window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setBackgroundDrawable(ColorDrawable(0x80000000.toInt())) // 50% transparent black
        window?.setBackgroundDrawable(ColorDrawable(0x801f3bdb.toInt())) // the 80 is the alfa value for 50% and then hex color.*/

        // Optional: Set the dialog dimensions or other attributes here
        //window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dialog_custom, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize your text boxes and buttons here
        val dialogTitle = view.findViewById<TextView>(R.id.dialogTitle)
        val dialogText1 = view.findViewById<TextView>(R.id.dialogtextView1)
        val dialogText2 = view.findViewById<TextView>(R.id.dialogtextView2)
        val dialogText3 = view.findViewById<TextView>(R.id.dialogtextView3)
        val buttonPositive = view.findViewById<Button>(R.id.buttonPositive)
        val buttonNegative = view.findViewById<Button>(R.id.buttonNegative)

        arguments?.let {
            dialogTitle.text = it.getString(ARG_TITLE, "")
            setTextAndVisibility(dialogText1, it, ARG_TEXT1, ARG_TEXT1_VISIBILITY)
            setTextAndVisibility(dialogText2, it, ARG_TEXT2, ARG_TEXT2_VISIBILITY)
            setTextAndVisibility(dialogText3, it, ARG_TEXT3, ARG_TEXT3_VISIBILITY)
        }

        buttonPositive.setOnClickListener {
            onPositiveClick?.invoke()
            dismiss()
        }

        buttonNegative.setOnClickListener {
            onNegativeClick?.invoke()
            dismiss()
        }
    }

    private fun setTextAndVisibility(textView: TextView, bundle: Bundle, textArg: String, visibilityArg: String) {
        textView.text = bundle.getString(textArg, "")
        textView.visibility = if (bundle.getBoolean(visibilityArg, true)) View.VISIBLE else View.GONE
    }

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_TEXT1 = "arg_text1"
        private const val ARG_TEXT1_VISIBILITY = "arg_text1_visibility"
        private const val ARG_TEXT2 = "arg_text2"
        private const val ARG_TEXT2_VISIBILITY = "arg_text2_visibility"
        private const val ARG_TEXT3 = "arg_text3"
        private const val ARG_TEXT3_VISIBILITY = "arg_text3_visibility"

        fun newInstance(title: String, text1: String, text1Visible: Boolean, text2: String, text2Visible: Boolean, text3: String, text3Visible: Boolean): CustomDialogFragment {
            return CustomDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_TEXT1, text1)
                    putBoolean(ARG_TEXT1_VISIBILITY, text1Visible)
                    putString(ARG_TEXT2, text2)
                    putBoolean(ARG_TEXT2_VISIBILITY, text2Visible)
                    putString(ARG_TEXT3, text3)
                    putBoolean(ARG_TEXT3_VISIBILITY, text3Visible)
                }
            }
        }
    }

    // Optional: Customize other dialog properties
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        // Additional dialog customization can go here
        return dialog
    }
}