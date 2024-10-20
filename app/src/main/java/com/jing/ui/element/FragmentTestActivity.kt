package com.jing.ui.element

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.jing.R
import com.jing.toys.coyoteTwoInitalButtonAreaFragment

class FragmentTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_test)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, coyoteTwoInitalButtonAreaFragment.newInstance())
                .commitNow()
        }
    }
}