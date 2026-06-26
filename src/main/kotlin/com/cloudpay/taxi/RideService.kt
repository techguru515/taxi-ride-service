package com.cloudpay.taxi

class RideService {
    private val events = mutableListOf<RideEvent>()

    fun createRide(rideId: RideId) {
        events.add(RideEvent.RideCreated(rideId))
    }

    fun getStatus(rideId: RideId): RideStatus {
        getEvents(rideId)
        return RideStatus.PENDING
    }

    fun getEvents(rideId: RideId): List<RideEvent> =
        events.filter { it.rideId == rideId }
}
