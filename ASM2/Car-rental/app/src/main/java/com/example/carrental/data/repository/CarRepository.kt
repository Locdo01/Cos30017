package com.example.carrental.data.repository

import com.example.carrental.data.model.Car
import com.example.carrental.R

// QUICK FIND: car catalog, vehicle data, drawable images, specifications.
// Keeps the app's sample car records in one place for easy updates.

object CarRepository {

    val cars = listOf(

        // 1. Porsche 911
        Car(
            id = 1,
            name = "Porsche 911",
            model = "911",
            brand = "Porsche",
            year = 2024,
            rating = 4.9f,
            kilometres = 12000,
            dailyCost = 380,
            bodyType = "Coupe",
            transmission = "Automatic",
            fuelType = "Petrol",
            seats = 4,
            fuelConsumption = 11.5f,
            fuelPrice = 1.05f,
            imageCar = R.drawable.img_pc_911,
            detailImagesCar = listOf(
                R.drawable.img_pc_911_front,
                R.drawable.img_pc_911_back,
                R.drawable.img_pc_911_inside
            )
        ),

        // 2. Porsche Macan
        Car(
            id = 2,
            name = "Porsche Macan",
            model = "Macan",
            brand = "Porsche",
            year = 2023,
            rating = 4.7f,
            kilometres = 18000,
            dailyCost = 300,
            bodyType = "SUV",
            transmission = "Automatic",
            fuelType = "Petrol",
            seats = 5,
            fuelConsumption = 10.5f,
            fuelPrice = 1.05f,
            imageCar = R.drawable.img_pc_maccan,
            detailImagesCar = listOf(
                R.drawable.img_pc_maccan_front,
                R.drawable.img_pc_maccan_back,
                R.drawable.img_pc_maccan_inside
            )
        ),

        // 3. Porsche Taycan
        Car(
            id = 3,
            name = "Porsche Taycan",
            model = "Taycan",
            brand = "Porsche",
            year = 2024,
            rating = 4.8f,
            kilometres = 9000,
            dailyCost = 350,
            bodyType = "Sedan",
            transmission = "Automatic",
            fuelType = "Electric",
            seats = 5,
            fuelConsumption = 19.6f,
            fuelPrice = 3.86f,
            imageCar = R.drawable.img_pc_taycan,
            detailImagesCar = listOf(
                R.drawable.img_pc_taycan_front,
                R.drawable.img_pc_taycan_back,
                R.drawable.img_pc_taycan_inside
            )
        ),

        // 4. Toyota Camry
        Car(
            id = 4,
            name = "Toyota Camry",
            model = "Camry",
            brand = "Toyota",
            year = 2022,
            rating = 4.5f,
            kilometres = 25000,
            dailyCost = 150,
            bodyType = "Sedan",
            transmission = "Automatic",
            fuelType = "Petrol",
            seats = 5,
            fuelConsumption = 7.5f,
            fuelPrice = 1.05f,
            imageCar = R.drawable.img_ta_cam,
            detailImagesCar = listOf(
                R.drawable.img_ta_cam_front,
                R.drawable.img_ta_cam_back,
                R.drawable.img_ta_cam_inside
            )
        ),

        // 5. Toyota Fortuner
        Car(
            id = 5,
            name = "Toyota Fortuner",
            model = "Fortuner",
            brand = "Toyota",
            year = 2023,
            rating = 4.6f,
            kilometres = 20000,
            dailyCost = 200,
            bodyType = "SUV",
            transmission = "Automatic",
            fuelType = "Diesel",
            seats = 7,
            fuelConsumption = 9.5f,
            fuelPrice = 0.85f,
            imageCar = R.drawable.img_ta_fortuner_,
            detailImagesCar = listOf(
                R.drawable.img_ta_fortuner_front,
                R.drawable.img_ta_fortuner_back,
                R.drawable.img_ta_fortuner_inside
            )
        ),

        // 6. Volkswagen Teramont
        Car(
            id = 6,
            name = "Volkswagen Teramont",
            model = "Teramont",
            brand = "Volkswagen",
            year = 2022,
            rating = 4.4f,
            kilometres = 28000,
            dailyCost = 220,
            bodyType = "SUV",
            transmission = "Automatic",
            fuelType = "Petrol",
            seats = 7,
            fuelConsumption = 10.5f,
            fuelPrice = 1.05f,
            imageCar = R.drawable.img_tera,
            detailImagesCar = listOf(
                R.drawable.img_tera_front,
                R.drawable.img_tera_front2,
                R.drawable.img_tera_inside
            )
        ),

        // 7. VinFast VF9
        Car(
            id = 7,
            name = "VinFast VF9",
            model = "VF 9",
            brand = "VinFast",
            year = 2024,
            rating = 4.7f,
            kilometres = 8000,
            dailyCost = 250,
            bodyType = "SUV",
            transmission = "Automatic",
            fuelType = "Electric",
            seats = 6,
            fuelConsumption = 22.5f,
            fuelPrice = 3.86f,
            imageCar = R.drawable.img_vf9,
            detailImagesCar = listOf(
                R.drawable.img_vf9_front,
                R.drawable.img_vf9_back,
                R.drawable.img_vf9_inside
            )
        )
    )
}
