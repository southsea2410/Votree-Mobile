package com.example.votree.data.product

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.votree.data.productCatagories.PlantType
import com.example.votree.data.productCatagories.SuitClimate
import com.example.votree.data.productCatagories.SuitEnvironment

@kotlinx.parcelize.Parcelize
@Entity(tableName = "product")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val productName: String,
    val shortDescription: String,
    val description: String,
    val averageRate: Double,
    val quantityOfRate: Int,
    val price: Double,
    val inventory: Int,
    val quantitySold: Int,
    val type: PlantType,
    val suitEnvironment: SuitEnvironment,
    val suitClimate: SuitClimate,
    val saleOff: Double
) : Parcelable {
    override fun toString(): String {
        return "Product(id=$id, name='$productName', price=$price)"
    }
}
