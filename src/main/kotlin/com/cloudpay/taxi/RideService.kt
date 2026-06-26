package com.cloudpay.taxi

import java.time.Instant

class RideService {
    private val events = mutableListOf<RideEvent>()

    fun createRide(rideId: RideId, occurredAt: Instant = Instant.EPOCH) {
        events.add(RideEvent.RideCreated(rideId, occurredAt))
    }

    fun acceptRide(rideId: RideId, occurredAt: Instant = Instant.EPOCH) {
        append(rideId, RideEvent.RideAccepted(rideId, occurredAt))
    }

    fun markDriverArrived(rideId: RideId, occurredAt: Instant = Instant.EPOCH) {
        append(rideId, RideEvent.DriverArrived(rideId, occurredAt))
    }

    fun pickUpPassenger(rideId: RideId, occurredAt: Instant = Instant.EPOCH) {
        append(rideId, RideEvent.PassengerPickedUp(rideId, occurredAt))
    }

    fun finishRide(rideId: RideId, occurredAt: Instant = Instant.EPOCH) {
        append(rideId, RideEvent.RideFinished(rideId, occurredAt))
    }

    fun cancelRide(rideId: RideId, occurredAt: Instant = Instant.EPOCH) {
        append(rideId, RideEvent.RideCanceled(rideId, occurredAt))
    }

    fun getStatus(rideId: RideId, at: Instant? = null): RideStatus {
        return getEvents(rideId)
            .asSequence()
            .filter { event -> at == null || !event.occurredAt.isAfter(at) }
            .sortedBy { event -> event.occurredAt }
            .fold(RideStatus.PENDING) { _, event ->
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
