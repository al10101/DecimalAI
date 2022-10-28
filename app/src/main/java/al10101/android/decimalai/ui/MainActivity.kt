package al10101.android.decimalai.ui

import al10101.android.decimalai.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up two fragments in the same activity: GL and UI
        val isFragmentContainerEmpty = savedInstanceState == null
        if (isFragmentContainerEmpty) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.gl_fragment, GLFragment.newInstance())
                .add(R.id.ui_fragment, UIFragment.newInstance())
                .commit()
        }

    }

}