package com.progix.fridgex.light.fragment.starred

import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.progix.fridgex.light.R
import com.progix.fridgex.light.activity.MainActivity
import com.progix.fridgex.light.adapter.starred.StarProductsAdapter
import com.progix.fridgex.light.helper.callbacks.ActionModeCallback
import com.progix.fridgex.light.helper.interfaces.ActionInterface
import kotlinx.coroutines.*

class StarProductsFragment : Fragment(), ActionInterface {

    private var job: Job? = null
    var adapter: StarProductsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_star_products, container, false)

        val recycler: RecyclerView = v.findViewById(R.id.starProductsRecycler)
        prodRecycler = recycler
        val annotationCard: MaterialCardView = v.findViewById(R.id.annotationCard)
        prodAnno = annotationCard

        val loading: CircularProgressIndicator = v.findViewById(R.id.loading)
        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            startCoroutine()
            loading.visibility = View.GONE
            if (productsList.isNotEmpty()) {
                adapter = StarProductsAdapter(requireContext(), productsList)
                adapter!!.init(tHis())
                recycler.adapter = adapter
            } else {
                annotationCard.startAnimation(
                    AnimationUtils.loadAnimation(
                        requireContext(),
                        R.anim.item_animation_fall_down
                    )
                )
                annotationCard.visibility = View.VISIBLE
                recycler.visibility = View.GONE
            }
        }

        return v
    }

    private fun tHis(): StarProductsFragment {
        return this
    }

    private suspend fun startCoroutine() =
        withContext(Dispatchers.IO) {
            val pairList: ArrayList<Pair<String, String>> = ArrayList()

            val cursor: Cursor =
                MainActivity.mDb.rawQuery("SELECT * FROM products WHERE is_starred = 1", null)
            cursor.moveToFirst()

            while (!cursor.isAfterLast) {
                pairList.add(Pair(cursor.getString(2), cursor.getString(1)))
                cursor.moveToNext()
            }
            pairList.sortBy { it.first }

            cursor.close()

            productsList = pairList

            @Suppress("BlockingMethodInNonBlockingContext")
            Thread.sleep(200)
        }

    companion object {
        var prodRecycler: RecyclerView? = null
        var prodAnno: MaterialCardView? = null
        var productsList: ArrayList<Pair<String, String>> = ArrayList()
    }

    override fun onSelectedItemsCountChanged(count: Int) {
        val callback = ActionModeCallback()
        callback.init(adapter!!, 4)
        if (MainActivity.actionMode == null) MainActivity.actionMode =
            (requireContext() as MainActivity).startSupportActionMode(callback)
        if (count > 0) MainActivity.actionMode?.title = "$count"
        else MainActivity.actionMode?.finish()
    }

}