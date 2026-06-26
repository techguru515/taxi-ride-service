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

    fun pickUpPassenger(rideId: RideId) {
        events.add(RideEvent.PassengerPickedUp(rideId))
    }

    fun finishRide(rideId: RideId) {
        events.add(RideEvent.RideFinished(rideId))
    }

    fun cancelRide(rideId: RideId) {
        events.add(RideEvent.RideCanceled(rideId))
    }

    fun getStatus(rideId: RideId): RideStatus {
        return getEvents(rideId).fold(RideStatus.PENDING) { _, event ->
            when (event) {
                is RideEvent.RideCreated -> RideStatus.PENDING
                is RideEvent.RideAccepted -> RideStatus.ACCEPTED
                is RideEvent.DriverArrived -> RideStatus.WAITING
                is RideEvent.PassengerPickedUp -> RideStatus.DRIVING
                is RideEvent.RideFinished -> RideStatus.FINISHED
                is RideEvent.RideCanceled -> RideStatus.CANCELED
            }
        }
    }

    fun getEvents(rideId: RideId): List<RideEvent> =
        events.filter { it.rideId == rideId }
}
