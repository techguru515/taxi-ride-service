package com.cloudpay.taxi

class RideNotFound(rideId: RideId) : NoSuchElementException(
    "Ride ${rideId.value} was not found",
)
