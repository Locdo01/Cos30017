package com.example.carrental.data.model



// QUICK FIND: booking form data, draft state, start, clear.
// Holds information while the user moves between booking screens.

data class BookingDraft(
    var carId: Int = -1,

    var name: String = "",
    var dateOfBirth: String = "",
    var email: String = "",
    var phone: String = "",
    var pickUpDate: String = "",
    var dropOffDate: String = "",
    var termsAccepted: Boolean = false,

    var idCard: String = "",
    var licenseNumber: String = "",
    var licenseClass: String = "",
    var licenseExpiry: String = "",
    var city: String = "",
    var district: String = "",
    var streetAddress: String = "",
    var additionalDetails: String = ""
)
