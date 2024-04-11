import com.google.firebase.Timestamp

interface DialogFragmentListener {
    fun updateExpireBanDateToFirestore(daysToAdd: Int, userId: String)
}