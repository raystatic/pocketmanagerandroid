package com.walletLog.pocketmanager.userflow.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.walletLog.pocketmanager.R

class CustomPagerAdapter(var context: Context,var pager: ArrayList<Int>, var textList:ArrayList<String>): PagerAdapter() {

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return pager.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.main_pager_item,container,false)
        val imageView = view.findViewById<ImageView>(R.id.image_pager_view)
        val textView = view.findViewById<TextView>(R.id.tv_intro_text)
        imageView.setImageResource(pager[position])
        textView.text = textList[position]
        container.addView(view)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getItemPosition(`object`: Any): Int {
        return super.getItemPosition(`object`)
    }

}