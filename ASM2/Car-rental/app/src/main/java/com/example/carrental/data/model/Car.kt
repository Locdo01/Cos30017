package com.example.carrental.data.model

import android.os.Parcel
import android.os.Parcelable
import com.example.carrental.R

// QUICK FIND: car model, vehicle specifications, consumption and fuel price labels.
// Defines the data every car card and details screen displays.

data class Car(
    val id: Int,
    val name: String,
    val model: String,
    val brand: String,
    val year: Int,
    val rating: Float,
    val kilometres: Int,
    val dailyCost: Int,
    val bodyType: String,
    val transmission: String,
    val fuelType: String,
    val seats: Int,

    val fuelConsumption: Float,
    val fuelPrice: Float,

    val imageCar: Int,
    val detailImagesCar: List<Int>
) : Parcelable {
    val consumptionText: String
        get() = if (fuelType == "Electric") {
            "$fuelConsumption kWh / 100 km"
        } else {
            "$fuelConsumption L / 100 km"
        }

    val fuelPriceText: String
        get() = if (fuelType == "Electric") {
            "$fuelPrice / kWh"
        } else {
            "$fuelPrice / L"
        }

    // Writes each car field in the same order used by the Parcel constructor.
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(model)
        parcel.writeString(brand)
        parcel.writeInt(year)
        parcel.writeFloat(rating)
        parcel.writeInt(kilometres)
        parcel.writeInt(dailyCost)
        parcel.writeString(bodyType)
        parcel.writeString(transmission)
        parcel.writeString(fuelType)
        parcel.writeInt(seats)
        parcel.writeFloat(fuelConsumption)
        parcel.writeFloat(fuelPrice)
        parcel.writeInt(imageCar)
        parcel.writeIntArray(detailImagesCar.toIntArray())
    }

    override fun describeContents(): Int = 0

    // Rebuilds a Car from values written by writeToParcel.
    private constructor(parcel: Parcel) : this(
        id = parcel.readInt(),
        name = parcel.readString().orEmpty(),
        model = parcel.readString().orEmpty(),
        brand = parcel.readString().orEmpty(),
        year = parcel.readInt(),
        rating = parcel.readFloat(),
        kilometres = parcel.readInt(),
        dailyCost = parcel.readInt(),
        bodyType = parcel.readString().orEmpty(),
        transmission = parcel.readString().orEmpty(),
        fuelType = parcel.readString().orEmpty(),
        seats = parcel.readInt(),
        fuelConsumption = parcel.readFloat(),
        fuelPrice = parcel.readFloat(),
        imageCar = parcel.readInt(),
        detailImagesCar = parcel.createIntArray()?.toList().orEmpty()
    )

    companion object CREATOR : Parcelable.Creator<Car> {
        override fun createFromParcel(parcel: Parcel): Car = Car(parcel)

        override fun newArray(size: Int): Array<Car?> = arrayOfNulls(size)
    }
}
