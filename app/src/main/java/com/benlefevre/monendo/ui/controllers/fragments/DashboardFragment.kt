package com.benlefevre.monendo.ui.controllers.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.benlefevre.monendo.R
import com.benlefevre.monendo.injection.Injection
import com.benlefevre.monendo.ui.viewmodels.DashboardViewModel
import kotlinx.android.synthetic.main.fragment_dashboard.*

class DashboardFragment : Fragment(), View.OnClickListener {

    val viewModel: DashboardViewModel by lazy {
        ViewModelProvider(
            this,
            Injection.providerViewModelFactory(activity!!.applicationContext)
        ).get(DashboardViewModel::class.java)
    }

    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupOnClickListener()
//        val dateBegin = with(Calendar.getInstance()){
//            set(Calendar.DAY_OF_YEAR,-7)
//            time
//        }
//        viewModel.getPainRelationsByPeriod(dateBegin,Date()).observe(viewLifecycleOwner, androidx.lifecycle.Observer {
//            it.forEach {
//                Log.i("benoit","$it")
//            }
//        })
    }

    private fun setupOnClickListener() {
        dashboard_fab.setOnClickListener(this)
    }

    override fun onClick(view: View){
        when(view.id){
            R.id.dashboard_fab -> navController.navigate(R.id.painFragment)
        }
    }
}
