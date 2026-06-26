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

    @Test
    fun `accepting a pending ride stores a RideAccepted event and changes status to accepted`() {
        val service = RideService()
        val rideId = RideId("ride-1")

        service.createRide(rideId)
        service.acceptRide(rideId)

        assertEquals(RideStatus.ACCEPTED, service.getStatus(rideId))
        assertEquals(
            listOf(
                RideEvent.RideCreated(rideId),
                RideEvent.RideAccepted(rideId),
            ),
            service.getEvents(rideId),
        )
    }

    @Test
    fun `marking driver arrived stores a DriverArrived event and changes status to waiting`() {
        val service = RideService()
        val rideId = RideId("ride-1")

        service.createRide(rideId)
        service.acceptRide(rideId)
        service.markDriverArrived(rideId)

        assertEquals(RideStatus.WAITING, service.getStatus(rideId))
        assertEquals(
            listOf(
                RideEvent.RideCreated(rideId),
                RideEvent.RideAccepted(rideId),
                RideEvent.DriverArrived(rideId),
            ),
            service.getEvents(rideId),
        )
    }
}
