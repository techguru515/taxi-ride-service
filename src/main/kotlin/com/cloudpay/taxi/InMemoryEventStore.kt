package com.cloudpay.taxi

class InMemoryEventStore : EventStore {
    private val events = mutableListOf<RideEvent>()

    override fun append(event: RideEvent) {
        events.add(event)
    }

    override fun load(rideId: RideId): List<RideEvent> =
        events.filter { event -> event.rideId == rideId }
}
