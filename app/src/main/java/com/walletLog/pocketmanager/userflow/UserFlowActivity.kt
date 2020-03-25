package com.walletLog.pocketmanager.userflow

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.walletLog.pocketmanager.R
import com.walletLog.pocketmanager.home.ui.HomeActivity
import com.walletLog.pocketmanager.userflow.adapter.CustomPagerAdapter
import com.walletLog.pocketmanager.utils.BaseActivity
import com.walletLog.pocketmanager.utils.Constants
import com.walletLog.pocketmanager.utils.PrefManager
import kotlinx.android.synthetic.main.activity_user_flow.*


class UserFlowActivity : BaseActivity() {

    private var viewPager:ViewPager?=null
    private var imageList=ArrayList<Int>()
    private var textList = ArrayList<String>()
    private var dotLayour:LinearLayout?=null
    private var dot: Array<TextView>?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_flow)

        viewPager = findViewById(R.id.viewpager)
        dotLayour = findViewById(R.id.layout_dot)

        imageList.add(R.drawable.tut_addamountdialog)
        imageList.add(R.drawable.tut_addtransactiondialog)
        imageList.add(R.drawable.tut_transactionlist)
        imageList.add(R.drawable.tut_transactioninfodialog)
        imageList.add(R.drawable.tut_summarydialog)

        textList.add("1. Add your Budget of the Month")
        textList.add("2. Add your Transaction")
        textList.add("3. List of Transactions")
        textList.add("4. View/Edit Transaction Info")
        textList.add("5. View Summary of Month")

        val pagerAdapter = CustomPagerAdapter(this,imageList,textList)
        viewPager!!.adapter = pagerAdapter
        viewPager!!.pageMargin = 20
        addDot(0)

        tv_previous_tutorial.setOnClickListener {
            viewPager!!.currentItem = viewPager!!.currentItem-1
        }
        tv_next_tutorial.setOnClickListener {
            viewPager!!.currentItem = viewPager!!.currentItem+1
        }

        viewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                tv_previous_tutorial.isEnabled = position != 0

                addDot(position)

                if (position == imageList.size-1){
                    tv_next_tutorial.setOnClickListener {
                        startActivity(Intent(this@UserFlowActivity,HomeActivity::class.java))
                        val prefManager = PrefManager(this@UserFlowActivity)
                        prefManager.saveBoolean(Constants.INTRODUCTION_DONE,true)
                        finish()
                    }
                }

            }
        })

    }

    fun addDot(page_position: Int) {
        dot = Array<TextView>(imageList.size){
            TextView(this)
        }
        layout_dot.removeAllViews()
        for (i in dot!!.indices) {
            dot!![i] = TextView(this)
            dot!![i].text = Html.fromHtml("&#9673;")
            dot!![i].textSize = 30F
            dot!![i].setTextColor(resources.getColor(R.color.grey))
            layout_dot.addView(dot!![i])
        }
        //active dot
        dot!![page_position].setTextColor(resources.getColor(R.color.colorPrimary))
    }
}
