package com.example.votree.admin.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.example.votree.R
import com.example.votree.admin.activities.AdminMainActivity
import com.example.votree.admin.adapters.BaseListAdapter
import com.example.votree.admin.adapters.ProductListAdapter
import com.example.votree.admin.interfaces.OnItemClickListener
import com.example.votree.models.Product
import com.example.votree.products.fragments.ProductDetailFragment
import com.google.firebase.firestore.Query

class ProductDialogFragment : BaseDialogFragment<Product>() {

    override val adapter: BaseListAdapter<Product> by lazy { createAdapter(this) }
    override val collectionName: String = "products"
    override val dialogTitle: String = "List of Products"
    override val accountIdKey: String = "account_id"

    companion object {
        private const val ACCOUNT_ID = "account_id"
        fun newInstance(accountId: String): ProductDialogFragment {
            val fragment = ProductDialogFragment()
            val args = Bundle()
            args.putString(ACCOUNT_ID, accountId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun createAdapter(listener: OnItemClickListener): BaseListAdapter<Product> {
        return ProductListAdapter(listener, true)
    }

    override fun fetchDataFromFirestore(accountId: String?) {
        val productList = mutableListOf<Product>()
        db.collection(collectionName).whereEqualTo("storeId", accountId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ProductListActivity", "listen:error", e)
                    return@addSnapshotListener
                }

                productList.clear()
                for (doc in snapshots!!) {
                    val product = doc.toObject(Product::class.java)
                    product.id = doc.id
                    productList.add(product)
                }
                adapter.setData(productList)
            }
    }

    override fun onItemSelected(position: Int) {
        onProductItemClicked(null, adapter.getSelectedPosition())
    }

    override fun onProductItemClicked(view: View?, position: Int) {
        (activity as AdminMainActivity).onProductItemClicked(view, position)

        val fragment = ProductDetailFragment()
        val bundle = Bundle().apply {
            putParcelable("product", adapter.getItem(position))
        }
        fragment.arguments = bundle

        val fragmentManager = (activity as FragmentActivity).supportFragmentManager

        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack("product_list_fragment").commit()
    }
}