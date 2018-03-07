package com.bennyhuo.shell.api

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import kotlinx.android.synthetic.main.activity_shell.*

/**
 * Created by benny on 03/03/2018.
 */
class ShellNoSessionActivity : AppCompatActivity(){
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shell)

        cmd.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                val cmdText = "${cmd.text}\n"
                cmd.setText("")
                val spannableString = SpannableString("$ $cmdText")
                val foregroundColorSpan = ForegroundColorSpan(Color.RED)
                spannableString.setSpan(foregroundColorSpan, 0, spannableString.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
                result.append(spannableString)
                Shell.run(cmdText){
                    handler.post { result.append(it) }
                }
                true
            } else {
                false
            }
        }
    }
}