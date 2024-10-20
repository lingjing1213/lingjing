package com.jing.ui.element

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.jing.R

class FragmentTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coyotetwo2)
//        if (savedInstanceState == null) {
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.container, DG_LAB2Fragment.newInstance())
//                .commitNow()
//        }
    }
}