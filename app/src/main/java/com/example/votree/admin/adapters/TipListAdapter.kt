package com.example.votree.admin.adapters

import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.admin.interfaces.OnItemClickListener
import com.example.votree.models.Tip
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class TipListAdapter(private var listener: OnItemClickListener) :
    RecyclerView.Adapter<TipListAdapter.ViewHolder>() {

    private val storage = Firebase.storage
    private val storageRef = storage.reference

    private var tipList = emptyList<Tip>()

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val tipView = LayoutInflater.from(parent.context).inflate(R.layout.item_tip, parent, false)
        return ViewHolder(tipView)
    }

    override fun getItemCount(): Int {
        return tipList.size
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currentTip = tipList[position]
        val firstImageUrl = currentTip.imageList.firstOrNull()



        val forestRef = storageRef.child("images/productTips/1.jpg")

        Log.d("TipListAdapter", "forestRef: $forestRef")

        Glide.with(viewHolder.itemView.context)
            .load("https://firebasestorage.googleapis.com/v0/b/votree-mobile-app.appspot.com/o/images%2FproductTips%2F1.jpg?alt=media&token=cfc4cf1b-f9fa-407c-93d8-13e6e8a2177f")
            .into(viewHolder.itemView.findViewById(R.id.list_item_avatar))

        Log.d("Hi", "Hi")

        viewHolder.itemView.findViewById<TextView>(R.id.list_item_title).text = currentTip.title
        viewHolder.itemView.findViewById<TextView>(R.id.list_item_short_description).text = currentTip.shortDescription
        viewHolder.itemView.setOnClickListener {
            listener.onTipItemClicked(viewHolder.itemView, position)
        }
//        val imageView = viewHolder.itemView.findViewById<ImageView>(R.id.list_item_avatar)

        // Load the first image URL into the ImageView
//        if (firstImageUrl != null) {
//            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(firstImageUrl)
//            storageRef.getBytes(Long.MAX_VALUE)
//                .addOnSuccessListener { bytes ->
//                    // Decode the byte array into a Bitmap
//                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
//                    // Set the Bitmap to the ImageView
//                    imageView.setImageBitmap(bitmap)
//                }
//                .addOnFailureListener { exception ->
//                    // Handle any errors
//                }
//        } else {
//            // If no image URL is available, you can set a default image or hide the ImageView
//            imageView.setImageResource(R.drawable.default_image)
//        }

//        viewHolder.itemView.findViewById<LinearLayout>(R.id.tip_row_layout).setOnClickListener {
//            val context = it.context
//            val sharedPref = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
//            sharedPref?.edit()?.apply {
//                putString("Full Name", currentTip.name)
//                putString("Birthday", currentTip.dateOfBirth)
//                val genderOptionId = when (currentStudent.gender) {
//                    "Male" -> R.id.maleOpt
//                    "Female" -> R.id.femaleOpt
//                    else -> R.id.otherOpt
//                }
//                putInt("Gender", genderOptionId)
//                apply()
//            }
//            val student_tmp = com.models.Student("${currentStudent.id}", currentStudent.name, currentStudent.className, currentStudent.dateOfBirth, currentStudent.gender)
//            val intent = Intent(context, StudentInformationActivity::class.java)
//            intent.putExtra("student", student_tmp)
//            intent.putExtra("studentOrder", position)
//            context.startActivity(intent)

//        }
//        viewHolder.itemView.findNavController().navigate(action)
    }
//
//    fun setData(student: List<Student>) {
//        this.studentList = student
//        notifyDataSetChanged()
//    }
//
//    private fun getStudentDOBAndGender(student: Student): String {
//        return "${student.dateOfBirth} - ${student.gender}"
//    }

    fun setData(tips: List<Tip>) {
        this.tipList = tips
        notifyDataSetChanged()
    }

    fun getTip(position: Int): Parcelable {
        return tipList[position]
    }

//    fun handleData(tip: Tip) {
//        viewHolder.itemView.findViewById<TextView>(R.id.list_item_title).text = currentTip.title
//    }
}