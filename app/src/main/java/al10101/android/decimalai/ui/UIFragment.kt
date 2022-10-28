package al10101.android.decimalai.ui

import al10101.android.decimalai.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData

class UIFragment: Fragment() {

    private var digit: Int? = null
    private var cert = 0f

    private lateinit var predictionTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ui, container, false)

        digitLiveData.value = null
        certainLiveData.value = 0f

        predictionTextView = view.findViewById(R.id.prediction_text_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        digitLiveData.observe(
            viewLifecycleOwner,
            {
                digit = it
                updateUI()
            }
        )

        certainLiveData.observe(
            viewLifecycleOwner,
            {
                cert = it
                updateUI()
            }
        )

    }

    private fun updateUI() {
        predictionTextView.text = getString(R.string.prediction_text_view, digit, cert)
    }

    companion object {
        var digitLiveData: MutableLiveData<Int> = MutableLiveData()
        var certainLiveData: MutableLiveData<Float> = MutableLiveData()
        fun newInstance() = UIFragment()
    }

}