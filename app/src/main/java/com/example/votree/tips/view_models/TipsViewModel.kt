package com.example.votree.tips.view_models

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.votree.tips.models.Author
import com.example.votree.tips.models.Comment
import com.example.votree.tips.models.ProductTip
import com.example.votree.tips.models.Vote
import com.example.votree.users.models.Store
import com.example.votree.users.models.User
import com.example.votree.utils.AuthHandler
import com.example.votree.utils.SingleLiveEvent
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject

class TipsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    val tipList = MutableLiveData<List<ProductTip>>()
    val topTipList = MutableLiveData<List<ProductTip>>()
    val commentList = SingleLiveEvent<List<Comment>>()
    private var tipListDocuments : QuerySnapshot? = null
    private val collectionRef by lazy { firestore.collection("ProductTip") }

    fun queryAllTips() {
        collectionRef
            .whereEqualTo("approvalStatus", 1)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                tipListDocuments = documents
                tipList.value = documents.toObjects(ProductTip::class.java)
                Log.d("TipsViewModel", "Done getting tips")
            }
            .addOnFailureListener{
                Log.d("TipsViewModel", "Error getting documents: ", it)
            }
    }

    fun queryTopTips() {
        collectionRef
            .whereEqualTo("approvalStatus", 1)
            .orderBy("vote", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { documents ->
                topTipList.value = documents.toObjects(ProductTip::class.java)
                Log.d("TipsViewModel", "Done getting top tips: " + topTipList.value?.size)
            }
            .addOnFailureListener{
                Log.d("TipsViewModel", "Error getting documents: ", it)
            }
    }

    fun getAuthor(userId : String) : SingleLiveEvent<Author?> {
        val author = SingleLiveEvent<Author?>()
        val userRef = firestore.collection("users").document(userId)

        var fullname = ""
        var storeName = ""
        userRef.get()
            .addOnSuccessListener { document ->
                val name = document.toObject<User>()?.fullName
                fullname = name ?: ""
                Log.d("TipsViewModel", "Author name: $name")
                val storeId = document.toObject<User>()?.storeId
                if (storeId !== null) {
                    val storeRef = firestore.collection("stores").document(storeId)
                    storeRef.get()
                        .addOnSuccessListener { document ->
                            val name = document.toObject<Store>()?.storeName
                            storeName = name ?: ""
                            Log.d("TipsViewModel", "Store name: $name")
                            author.value = Author(userId,fullname , storeName)
                        }
                        .addOnFailureListener { e ->
                            author.value = null
                            Log.w("TipsViewModel", "Error getting store data", e)
                        }
                }
                else {
                    author.value = Author(userId, fullname, storeName)
                }
            }
            .addOnFailureListener { e ->
                Log.w("TipsViewModel", "Error getting user data", e)
            }
        return author
    }

    fun castVote(tip: ProductTip, isUpvote: Boolean) {
        val voteRef = collectionRef.document(tip.id).collection("votes")
        val docRef = collectionRef.document(tip.id)
        docRef.update("vote_count", FieldValue.increment(if (isUpvote) 1 else -1))
            .addOnSuccessListener {
                Log.d("TipsViewModel", "[Vote] vote_count updated:" + if (isUpvote) "+1" else "-1")
            }
            .addOnFailureListener { e ->
                Log.w("TipsViewModel", "[Vote] Error updating vote_count ", e)
            }
        val vote = Vote(
            userId = AuthHandler.firebaseAuth.currentUser?.uid!!,
            upvote = isUpvote,
        )
        voteRef.document(AuthHandler.firebaseAuth.currentUser?.uid!! + '_' + isUpvote.toString()).set(vote)
            .addOnSuccessListener {
                Log.d("TipsViewModel", "[Vote] Vote added to database" + vote.toString())
            }
            .addOnFailureListener { e ->
                Log.w("TipsViewModel", "[Vote] Error adding vote to database" + vote.toString())
            }
    }

    fun unVoteTip(tip: ProductTip, upvote: Boolean) {
        val docRef = collectionRef.document(tip.id)
        val voteRef = docRef.collection("votes").document((AuthHandler.firebaseAuth.currentUser!!.uid) + '_' + upvote.toString())

        voteRef.get().addOnSuccessListener {
            voteRef.delete()
                .addOnSuccessListener {
                    Log.d("TipsViewModel", "[Unvote] Vote removed from database")
                }
                .addOnFailureListener { e ->
                    Log.w("TipsViewModel", "[Unvote] Error removing vote from database", e)
                }
            if (upvote)
                docRef.update("vote_count", FieldValue.increment(-1))
                    .addOnSuccessListener {
                        Log.d("TipsViewModel", "[Unvote] vote_count updated")
                    }
                    .addOnFailureListener { e ->
                        Log.w("TipsViewModel", "[Unvote] Error updating vote_count ", e)
                    }
            else
                docRef.update("vote_count", FieldValue.increment(1))
                    .addOnSuccessListener {
                        Log.d("TipsViewModel", "[Unvote] vote_count updated")
                    }
                    .addOnFailureListener { e ->
                        Log.w("TipsViewModel", "[Unvote] Error updating vote_count ", e)
                    }
        }
    }

    fun getIsUpvoted(tipId: String): SingleLiveEvent<Boolean?> {
        val isUpvoted = SingleLiveEvent<Boolean?>()
        val voteRef = collectionRef.document(tipId).collection("votes")
        voteRef
            .whereEqualTo("userId", AuthHandler.firebaseAuth.currentUser?.uid)
            .limit(1)
            .get()
            .addOnSuccessListener { document ->
                if (document.isEmpty) {
                    isUpvoted.value = null
                    return@addOnSuccessListener
                }
                isUpvoted.value = document.toObjects(Vote::class.java)[0].upvote
            }
            .addOnFailureListener{
                isUpvoted.value = null
            }
        return isUpvoted
    }

    fun castComment(tip: ProductTip, content: String) {
        val commentRef = collectionRef.document(tip.id).collection("comments")
        val comment = Comment(
            userId = AuthHandler.firebaseAuth.currentUser?.uid!!,
            content = content,
        )
        commentRef.add(comment)
            .addOnSuccessListener {
                Log.d("TipsViewModel", "[Comment] Comment added to database" + comment.toString())
            }
            .addOnFailureListener { e ->
                Log.w("TipsViewModel", "[Comment] Error adding comment to database" + comment.toString())
            }
    }

    fun queryComments(tip: ProductTip) {
        val commentList = SingleLiveEvent<List<Comment>>()

        val commentRef = collectionRef.document(tip.id).collection("comments")
        val userRef = firestore.collection("users")

        commentRef
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { documents ->
                val queriedComments = documents.toObjects(Comment::class.java)

                for (i in queriedComments.indices){
                    userRef.document(queriedComments[i].userId).get()
                        .addOnSuccessListener { document ->
                            queriedComments[i].fullName = document.toObject<User>()?.fullName ?: ""
                            queriedComments[i].avatar = document.toObject<User>()?.avatar ?: ""
                            Log.d("TipsViewModel", "Comment $i: " + queriedComments[i].toString())
                            if (i == queriedComments.size - 1){
                                Log.d("TipsViewModel", "Done getting comments's names and avatars")
                                commentList.postValue(queriedComments)
                            }
                        }
                        .addOnFailureListener{
                            Log.d("TipsViewModel", "Error getting author name and avatar: ", it)
                        }

                }
                Log.d("TipsViewModel", "Done getting comments")
            }
            .addOnFailureListener{
                Log.d("TipsViewModel", "Error getting comments: ", it)
            }
        return
    }
}