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
import com.example.votree.models.User

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
        var storeId = ""
        db.collection("users").whereEqualTo("id", accountId)
            .addSnapshotListener() { snapshots, e ->
                if (e != null) {
                    Log.w("ProductListActivity", "listen:error", e)
                    return@addSnapshotListener
                }

                for (doc in snapshots!!) {
                    Log.d("ProductListActivity", "storeId: ${doc.toObject(User::class.java)}")
                    db.collection(collectionName).whereEqualTo("storeId", doc.toObject(User::class.java).storeId)
                        .addSnapshotListener { snapshots2, e2 ->
                            if (e2 != null) {
                                Log.w("ProductListActivity", "listen:error", e2)
                                return@addSnapshotListener
                            }

                            productList.clear()
                            for (doc2 in snapshots2!!) {
                                val product = doc2.toObject(Product::class.java)
                                product.id = doc2.id
                                Log.d("ProductListActivity", "product: ${doc2.toObject(Product::class.java)}")
                                productList.add(product)
                            }
                            adapter.setData(productList.sortedByDescending { it.createdAt })
                        }
                }
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