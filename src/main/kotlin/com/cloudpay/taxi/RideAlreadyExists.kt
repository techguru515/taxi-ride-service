package com.cloudpay.taxi

class RideAlreadyExists(rideId: RideId) : IllegalStateException(
    "Ride ${rideId.value} already exists",
)
