package al10101.android.decimalai.ui

import al10101.android.decimalai.ui.HandwritingView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    private lateinit var view: HandwritingView
    private var rendererSet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // No layout, only the handwriting view. We also set the renderer flag to
        // manage the change in activity cycles
        view = HandwritingView(this)
        rendererSet = true

        setContentView(view)

    }

    override fun onPause() {
        super.onPause()
        if (rendererSet) {
            view.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (rendererSet) {
            view.onResume()
        }
    }

}