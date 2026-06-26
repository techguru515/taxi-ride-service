package com.cloudpay.taxi

import java.time.Instant

class Ride private constructor(
    private val rideId: RideId,
    val status: RideStatus,
) {
    fun accept(occurredAt: Instant = Instant.EPOCH): RideEvent.RideAccepted =
        nextEvent(RideEvent.RideAccepted(rideId, occurredAt), RideStatus.PENDING)

    fun markDriverArrived(occurredAt: Instant = Instant.EPOCH): RideEvent.DriverArrived =
        nextEvent(RideEvent.DriverArrived(rideId, occurredAt), RideStatus.ACCEPTED)

    fun pickUpPassenger(occurredAt: Instant = Instant.EPOCH): RideEvent.PassengerPickedUp =
        nextEvent(RideEvent.PassengerPickedUp(rideId, occurredAt), RideStatus.WAITING)

    fun finish(occurredAt: Instant = Instant.EPOCH): RideEvent.RideFinished =
        nextEvent(RideEvent.RideFinished(rideId, occurredAt), RideStatus.DRIVING)

    fun cancel(occurredAt: Instant = Instant.EPOCH): RideEvent.RideCanceled {
        val event = RideEvent.RideCanceled(rideId, occurredAt)
        if (status !in CANCELLABLE_STATUSES) {
            throw InvalidRideTransition(rideId, status, event)
        }

        return event
    }

    private fun <T : RideEvent> nextEvent(event: T, requiredStatus: RideStatus): T {
        if (status != requiredStatus) {
            throw InvalidRideTransition(rideId, status, event)
        }

        return event
    }

    companion object {
        private val CANCELLABLE_STATUSES = setOf(RideStatus.PENDING, RideStatus.ACCEPTED, RideStatus.WAITING)

        fun fromHistory(events: List<RideEvent>): Ride {
            if (events.isEmpty()) {
                throw IllegalArgumentException("Ride history cannot be empty")
            }

            val rideId = events.first().rideId
            val status = events
                .sortedBy { event -> event.occurredAt }
                .fold(RideStatus.PENDING) { _, event -> event.toStatus() }

            return Ride(rideId, status)
        }

        private fun RideEvent.toStatus(): RideStatus =
            when (this) {
                is RideEvent.RideCreated -> RideStatus.PENDING
                is RideEvent.RideAccepted -> RideStatus.ACCEPTED
                is RideEvent.DriverArrived -> RideStatus.WAITING
                is RideEvent.PassengerPickedUp -> RideStatus.DRIVING
                is RideEvent.RideFinished -> RideStatus.FINISHED
                is RideEvent.RideCanceled -> RideStatus.CANCELED
            }
    }
}
