package com.bennyhuo.shell.api

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivity

/**
 * Created by benny on 07/03/2018.
 */
class MainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        noSessionActivity.onClick {
            startActivity<ShellNoSessionActivity>()
        }

        sessionActivity.onClick {
            startActivity<ShellSessionActivity>()
        }
    }
}