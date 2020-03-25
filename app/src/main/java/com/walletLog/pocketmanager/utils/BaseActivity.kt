package com.walletLog.pocketmanager.utils

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.walletLog.pocketmanager.R
import com.novoda.merlin.Merlin


open class BaseActivity : AppCompatActivity() {

    var merlin: Merlin?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)


        merlin = Merlin.Builder().withConnectableCallbacks().build(this)

        merlin?.registerConnectable {
            Utility.showToast(this,"Back Online!")
        }

        merlin = Merlin.Builder().withDisconnectableCallbacks().build(this)

        merlin?.registerDisconnectable {
            Utility.showToast(this,"No internet connection")
        }
    }

    override fun onResume() {
        super.onResume()
        merlin?.bind()
    }

    override fun onPause() {
        merlin?.unbind()
        super.onPause()
    }

}
