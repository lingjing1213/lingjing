package com.jing.toys

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jing.R

/**
 *@Author：hifter
 *@Package：com.jing.toys
 *@Project：灵静
 *@name：coyoteTwoInitalButtonAreaFragment
 *@Date：2024/10/20  下午5:09
 *@Filename：coyoteTwoInitalButtonAreaFragment
 *@Version：1.0.0
 */

class coyoteTwoInitalButtonAreaFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    //    这个方法的返回值就是会显示出的view的对象
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_coyote_two_inital_button_area, container, false)
    }

    companion object {
        //        创建一个fragment的实例，由于fragment需要空参的构造方法，同时又需要通过put方法来填入参数，因此通常通过newInstance方法来作为构造方法的代替。
        @JvmStatic
        fun newInstance() = coyoteTwoInitalButtonAreaFragment()
    }
}
/**
 * 可以让activity实现这个回调接口，
 * 然后fragment通过这个接口让activity去执行对应的功能。
 * 避免需要将对应操作的代码放到不同的fragment里不方便管理和调用。
 * （不确定是否必要）
 */

interface coyoteTwoInitalButtonAreaCallback{

}