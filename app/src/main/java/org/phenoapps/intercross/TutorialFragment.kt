package org.phenoapps.intercross

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class TutorialFragment : Fragment() {

    var mLayoutId = R.layout.pager_item0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?  {

        mLayoutId = when (arguments?.getInt("position")) {
            0 -> R.layout.pager_item0
            1 -> R.layout.pager_item1
            2 -> R.layout.pager_item2
            3 -> R.layout.pager_item3
            else -> R.layout.pager_item4
        }


        return inflater.inflate(mLayoutId, container, false)

    }
}