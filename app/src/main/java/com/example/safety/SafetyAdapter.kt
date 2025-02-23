package com.example.safety

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.safety.databinding.ModelBinding
import com.example.safety.models.UsersListModel
import com.example.safety.ui.UserLocationFragment
import com.google.firebase.firestore.FirebaseFirestore

class SafetyAdapter(private val memberList: UsersListModel) :
    RecyclerView.Adapter<SafetyAdapter.ViewHolder>() {
    val TAG = "Rupam Safety Adapter"
    private lateinit var fdb: FirebaseFirestore

    inner class ViewHolder(var binding: ModelBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ModelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = memberList[position]
        fdb = FirebaseFirestore.getInstance()
        fdb.collection(Constants.FIRESTORE_COLLECTION)
            .document(item.email)
            .get()
            .addOnSuccessListener { doc ->
                val mem = doc.toObject(Users::class.java)!!
                Log.d("Rupam  Safety Adapter ", " $mem")
                holder.binding.nameModel.text = mem.name
                holder.binding.addModel.text = "Latitude: ${mem.lat}\nLongitude: ${mem.long}"
                holder.binding.batPerModel.text = "${mem.batPer}%"
                holder.binding.connectionTvModel.text = mem.connectionInfo
                holder.binding.callImgModel.setOnClickListener {

                    val phoneNum = mem.phoneNumber.toLong()
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:$phoneNum")
                    holder.itemView.context.startActivity(intent)

                }
                holder.itemView.setOnClickListener {
                    val latitude = mem.lat.toDouble()
                    val longitude = mem.long.toDouble()
                    Log.d("@@@@@@@", "$mem.name $latitude $longitude ${mem.long}")
                    val fragment =
                        MapplsMapFragment.newInstance(latitude, longitude, mem.name, false)

                    val activity = it.context as AppCompatActivity
                    activity.supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.container, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }.addOnFailureListener {
                Log.d("Rupam Safety Adapter Error ", "${it.message}")
            }

    }

    override fun getItemCount(): Int {
        return memberList.size
    }
}