package ntrllog.github.io.notifycation

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText

class CustomDialog(a: Activity?, private val content: String) : Dialog(
    a!!
), View.OnClickListener {
    private lateinit var mDialogResult: OnMyDialogResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_create_notification)

        val content = findViewById<EditText>(R.id.dialog_content)
        content.setText(this.content) // grab notification text and put it in the input
        content.setSelectAllOnFocus(true) // highlight all text on tap
        content.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES // auto capitalize first letter

        val enter = findViewById<Button>(R.id.dialog_enter)
        enter.setOnClickListener {
            val s = content.text.toString()
            mDialogResult.finish(s)
            dismiss()
        }
    }

    override fun onClick(v: View) {}

    fun setDialogResult(dialogResult: OnMyDialogResult) {
        mDialogResult = dialogResult
    }

    interface OnMyDialogResult {
        fun finish(result: String?)
    }
}