package com.cloudpay.taxi

class RideService {
    private val events = mutableListOf<RideEvent>()

    fun createRide(rideId: RideId) {
        events.add(RideEvent.RideCreated(rideId))
    }

    fun acceptRide(rideId: RideId) {
        append(rideId, RideEvent.RideAccepted(rideId))
    }

    fun markDriverArrived(rideId: RideId) {
        append(rideId, RideEvent.DriverArrived(rideId))
    }

    fun pickUpPassenger(rideId: RideId) {
        append(rideId, RideEvent.PassengerPickedUp(rideId))
    }

    fun finishRide(rideId: RideId) {
        append(rideId, RideEvent.RideFinished(rideId))
    }

    fun cancelRide(rideId: RideId) {
        append(rideId, RideEvent.RideCanceled(rideId))
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

    private fun append(rideId: RideId, event: RideEvent) {
        val currentStatus = getStatus(rideId)
        if (!canApply(currentStatus, event)) {
            throw InvalidRideTransition(rideId, currentStatus, event)
        }

        events.add(event)
    }

    private fun canApply(currentStatus: RideStatus, event: RideEvent): Boolean =
        when (event) {
            is RideEvent.RideCreated -> true
            is RideEvent.RideAccepted -> currentStatus == RideStatus.PENDING
            is RideEvent.DriverArrived -> currentStatus == RideStatus.ACCEPTED
            is RideEvent.PassengerPickedUp -> currentStatus == RideStatus.WAITING
            is RideEvent.RideFinished -> currentStatus == RideStatus.DRIVING
            is RideEvent.RideCanceled ->
                currentStatus in setOf(RideStatus.PENDING, RideStatus.ACCEPTED, RideStatus.WAITING)
        }
}
