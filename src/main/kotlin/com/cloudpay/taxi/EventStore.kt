package com.cloudpay.taxi

interface EventStore {
    fun append(event: RideEvent)
    fun load(rideId: RideId): List<RideEvent>
}
