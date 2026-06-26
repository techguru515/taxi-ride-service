package com.cloudpay.taxi

import java.time.Instant

class RideService(
    private val eventStore: EventStore = InMemoryEventStore(),
) {
    fun createRide(rideId: RideId, occurredAt: Instant = Instant.EPOCH) {
        if (getEvents(rideId).isNotEmpty()) {
            throw RideAlreadyExists(rideId)
        }

        eventStore.append(RideEvent.RideCreated(rideId, occurredAt))
    }

    fun acceptRide(rideId: RideId, occurredAt: Instant = Instant.EPOCH) {
        append(rideId) { ride -> ride.accept(occurredAt) }
    }

    fun markDriverArrived(rideId: RideId, occurredAt: Instant = Instant.EPOCH) {
        append(rideId) { ride -> ride.markDriverArrived(occurredAt) }
    }

    fun pickUpPassenger(rideId: RideId, occurredAt: Instant = Instant.EPOCH) {
        append(rideId) { ride -> ride.pickUpPassenger(occurredAt) }
    }

    fun finishRide(rideId: RideId, occurredAt: Instant = Instant.EPOCH) {
        append(rideId) { ride -> ride.finish(occurredAt) }
    }

    fun cancelRide(rideId: RideId, occurredAt: Instant = Instant.EPOCH) {
        append(rideId) { ride -> ride.cancel(occurredAt) }
    }

    fun getStatus(rideId: RideId, at: Instant? = null): RideStatus {
        val rideEvents = getEvents(rideId)
        if (rideEvents.isEmpty()) {
            throw RideNotFound(rideId)
        }

        val eventsAtTime = rideEvents
            .filter { event -> at == null || !event.occurredAt.isAfter(at) }
            .sortedBy { event -> event.occurredAt }

        if (eventsAtTime.isEmpty()) {
            throw RideNotFound(rideId)
        }

        return Ride.fromHistory(eventsAtTime).status
    }

    fun getEvents(rideId: RideId): List<RideEvent> =
        eventStore.load(rideId)

    private fun append(rideId: RideId, produceEvent: (Ride) -> RideEvent) {
        val rideEvents = getEvents(rideId)
        if (rideEvents.isEmpty()) {
            throw RideNotFound(rideId)
        }

        eventStore.append(produceEvent(Ride.fromHistory(rideEvents)))
    }
}
