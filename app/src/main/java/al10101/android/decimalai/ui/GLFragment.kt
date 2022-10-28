package al10101.android.decimalai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class GLFragment: Fragment() {

    private var view: HandwritingView? = null
    private var rendererSet = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // No layout, only the handwriting view. We also set the renderer flag to
        // manage the change in activity cycles
        view = activity?.let { HandwritingView(it) }
        rendererSet = true
        return view
    }

    override fun onPause() {
        super.onPause()
        if (rendererSet) {
            view?.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (rendererSet) {
            view?.onResume()
        }
    }

    companion object {
        fun newInstance() = GLFragment()
    }

}