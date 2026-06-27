package com.cloudpay.taxi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RideTest {
    @Test
    fun `ride can be rebuilt from its event history`() {
        val rideId = RideId("ride-1")
        val ride = Ride.fromHistory(
            listOf(
                RideEvent.RideCreated(rideId),
                RideEvent.RideAccepted(rideId),
                RideEvent.DriverArrived(rideId),
            ),
        )

        assertEquals(RideStatus.WAITING, ride.status)
    }

    @Test
    fun `ride aggregate rejects invalid transitions before producing new events`() {
        val rideId = RideId("ride-1")
        val ride = Ride.fromHistory(listOf(RideEvent.RideCreated(rideId)))

        val error = assertFailsWith<InvalidRideTransition> {
            ride.markDriverArrived()
        }

        assertEquals("Cannot move ride ride-1 from PENDING using DriverArrived", error.message)
    }

    @Test
    fun `ride history cannot skip lifecycle transitions`() {
        val rideId = RideId("ride-1")

        val error = assertFailsWith<InvalidRideTransition> {
            Ride.fromHistory(
                listOf(
                    RideEvent.RideCreated(rideId),
                    RideEvent.DriverArrived(rideId),
                ),
            )
        }

        assertEquals("Cannot move ride ride-1 from PENDING using DriverArrived", error.message)
    }
}
