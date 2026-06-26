package com.cloudpay.taxi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RideServiceTest {
    @Test
    fun `creating a ride stores a RideCreated event and starts in pending status`() {
        val service = RideService()
        val rideId = RideId("ride-1")

        service.createRide(rideId)

        assertEquals(RideStatus.PENDING, service.getStatus(rideId))
        assertEquals(
            listOf(RideEvent.RideCreated(rideId)),
            service.getEvents(rideId),
        )
        assertIs<RideEvent.RideCreated>(service.getEvents(rideId).single())
    }
}
