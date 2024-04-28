package com.example.votree.admin.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.example.votree.R
import com.example.votree.admin.activities.AdminMainActivity
import com.example.votree.admin.adapters.BaseListAdapter
import com.example.votree.admin.adapters.TipListAdapter
import com.example.votree.admin.adapters.TransactionListAdapter
import com.example.votree.admin.interfaces.OnItemClickListener
import com.example.votree.models.Transaction

class TransactionDialogFragment : BaseDialogFragment<Transaction>() {

    override val adapter: BaseListAdapter<Transaction> by lazy { createAdapter(this) }
    override val collectionName: String = "transactions"
    override val dialogTitle: String = "List of Transactions"
    override val accountIdKey: String = "account_id"

    companion object {
        private const val ACCOUNT_ID = "account_id"
        fun newInstance(accountId: String): TransactionDialogFragment {
            val fragment = TransactionDialogFragment()
            val args = Bundle()
            args.putString(ACCOUNT_ID, accountId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun createAdapter(listener: OnItemClickListener): BaseListAdapter<Transaction> {
        return TransactionListAdapter(listener, true)
    }

    override fun fetchDataFromFirestore(accountId: String?) {
        val transactionList = mutableListOf<Transaction>()
        db.collection("ProductTip").whereEqualTo("userId", accountId)
            .orderBy("updatedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("TipListActivity", "listen:error", e)
                    return@addSnapshotListener
                }

                transactionList.clear()
                for (doc in snapshots!!) {
                    val transaction = doc.toObject(Transaction::class.java)
                    transaction.id = doc.id
                    transactionList.add(transaction)
                }
                adapter.setData(transactionList)
            }
    }

    override fun onItemSelected(position: Int) {
        onTipItemClicked(null, adapter.getSelectedPosition())
    }

//    override fun onTipItemClicked(view: View?, position: Int) {
//        (activity as AdminMainActivity).onTipItemClicked(view, position)
//        val fragment = TipDetailFragment()
//        val bundle = Bundle().apply {
//            putParcelable("tip", adapter.getItem(position))
//        }
//        fragment.arguments = bundle
//
//        val fragmentManager = (activity as FragmentActivity).supportFragmentManager
//
//        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack("tip_list_fragment").commit()
//    }

    override fun onTransactionItemClicked(view: View?, position: Int) {
        (activity as AdminMainActivity).onTransactionItemClicked(view, position)
        val fragment = TransactionDetailFragment()
        val bundle = Bundle().apply {
            putParcelable("transaction", adapter.getItem(position))
        }
        fragment.arguments = bundle

        val fragmentManager = (activity as FragmentActivity).supportFragmentManager

        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack("transaction_list_fragment").commit()
    }
}