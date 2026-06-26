package com.cloudpay.taxi

class RideService {
    private val events = mutableListOf<RideEvent>()

    fun createRide(rideId: RideId) {
        events.add(RideEvent.RideCreated(rideId))
    }

    fun acceptRide(rideId: RideId) {
        events.add(RideEvent.RideAccepted(rideId))
    }

    fun markDriverArrived(rideId: RideId) {
        events.add(RideEvent.DriverArrived(rideId))
    }

    fun getStatus(rideId: RideId): RideStatus {
        return getEvents(rideId).fold(RideStatus.PENDING) { _, event ->
            when (event) {
                is RideEvent.RideCreated -> RideStatus.PENDING
                is RideEvent.RideAccepted -> RideStatus.ACCEPTED
                is RideEvent.DriverArrived -> RideStatus.WAITING
            }
        }
    }

    fun getEvents(rideId: RideId): List<RideEvent> =
        events.filter { it.rideId == rideId }
}
