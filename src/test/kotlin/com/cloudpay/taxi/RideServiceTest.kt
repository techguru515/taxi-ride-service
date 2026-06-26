package com.cloudpay.taxi

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertFailsWith

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

    @Test
    fun `picking up passenger stores a PassengerPickedUp event and changes status to driving`() {
        val service = RideService()
        val rideId = RideId("ride-1")

        service.createRide(rideId)
        service.acceptRide(rideId)
        service.markDriverArrived(rideId)
        service.pickUpPassenger(rideId)

        assertEquals(RideStatus.DRIVING, service.getStatus(rideId))
        assertEquals(
            listOf(
                RideEvent.RideCreated(rideId),
                RideEvent.RideAccepted(rideId),
                RideEvent.DriverArrived(rideId),
                RideEvent.PassengerPickedUp(rideId),
            ),
            service.getEvents(rideId),
        )
    }

    @Test
    fun `finishing a ride stores a RideFinished event and changes status to finished`() {
        val service = RideService()
        val rideId = RideId("ride-1")

        service.createRide(rideId)
        service.acceptRide(rideId)
        service.markDriverArrived(rideId)
        service.pickUpPassenger(rideId)
        service.finishRide(rideId)

        assertEquals(RideStatus.FINISHED, service.getStatus(rideId))
        assertEquals(
            listOf(
                RideEvent.RideCreated(rideId),
                RideEvent.RideAccepted(rideId),
                RideEvent.DriverArrived(rideId),
                RideEvent.PassengerPickedUp(rideId),
                RideEvent.RideFinished(rideId),
            ),
            service.getEvents(rideId),
        )
    }

    @Test
    fun `canceling a pending ride stores a RideCanceled event and changes status to canceled`() {
        val service = RideService()
        val rideId = RideId("ride-1")

        service.createRide(rideId)
        service.cancelRide(rideId)

        assertEquals(RideStatus.CANCELED, service.getStatus(rideId))
        assertEquals(
            listOf(
                RideEvent.RideCreated(rideId),
                RideEvent.RideCanceled(rideId),
            ),
            service.getEvents(rideId),
        )
    }

    @Test
    fun `canceling an accepted ride stores a RideCanceled event and changes status to canceled`() {
        val service = RideService()
        val rideId = RideId("ride-1")

        service.createRide(rideId)
        service.acceptRide(rideId)
        service.cancelRide(rideId)

        assertEquals(RideStatus.CANCELED, service.getStatus(rideId))
        assertEquals(
            listOf(
                RideEvent.RideCreated(rideId),
                RideEvent.RideAccepted(rideId),
                RideEvent.RideCanceled(rideId),
            ),
            service.getEvents(rideId),
        )
    }

    @Test
    fun `canceling a waiting ride stores a RideCanceled event and changes status to canceled`() {
        val service = RideService()
        val rideId = RideId("ride-1")

        service.createRide(rideId)
        service.acceptRide(rideId)
        service.markDriverArrived(rideId)
        service.cancelRide(rideId)

        assertEquals(RideStatus.CANCELED, service.getStatus(rideId))
        assertEquals(
            listOf(
                RideEvent.RideCreated(rideId),
                RideEvent.RideAccepted(rideId),
                RideEvent.DriverArrived(rideId),
                RideEvent.RideCanceled(rideId),
            ),
            service.getEvents(rideId),
        )
    }

    @Test
    fun `driver cannot arrive before ride is accepted`() {
        val service = RideService()
        val rideId = RideId("ride-1")

        service.createRide(rideId)

        val error = assertFailsWith<InvalidRideTransition> {
            service.markDriverArrived(rideId)
        }

        assertEquals("Cannot move ride ride-1 from PENDING using DriverArrived", error.message)
        assertEquals(listOf(RideEvent.RideCreated(rideId)), service.getEvents(rideId))
    }

    @Test
    fun `passenger cannot be picked up before driver arrives`() {
        val service = RideService()
        val rideId = RideId("ride-1")

        service.createRide(rideId)
        service.acceptRide(rideId)

        val error = assertFailsWith<InvalidRideTransition> {
            service.pickUpPassenger(rideId)
        }

        assertEquals("Cannot move ride ride-1 from ACCEPTED using PassengerPickedUp", error.message)
        assertEquals(
            listOf(
                RideEvent.RideCreated(rideId),
                RideEvent.RideAccepted(rideId),
            ),
            service.getEvents(rideId),
        )
    }

    @Test
    fun `ride cannot be canceled once passenger has been picked up`() {
        val service = RideService()
        val rideId = RideId("ride-1")

        service.createRide(rideId)
        service.acceptRide(rideId)
        service.markDriverArrived(rideId)
        service.pickUpPassenger(rideId)

        val error = assertFailsWith<InvalidRideTransition> {
            service.cancelRide(rideId)
        }

        assertEquals("Cannot move ride ride-1 from DRIVING using RideCanceled", error.message)
        assertEquals(RideStatus.DRIVING, service.getStatus(rideId))
    }

    @Test
    fun `status can be queried at a point in time`() {
        val service = RideService()
        val rideId = RideId("ride-1")
        val createdAt = Instant.parse("2026-06-26T10:00:00Z")
        val acceptedAt = Instant.parse("2026-06-26T10:05:00Z")
        val arrivedAt = Instant.parse("2026-06-26T10:10:00Z")

        service.createRide(rideId, occurredAt = createdAt)
        service.acceptRide(rideId, occurredAt = acceptedAt)
        service.markDriverArrived(rideId, occurredAt = arrivedAt)

        assertEquals(RideStatus.PENDING, service.getStatus(rideId, at = createdAt.plusSeconds(1)))
        assertEquals(RideStatus.ACCEPTED, service.getStatus(rideId, at = acceptedAt.plusSeconds(1)))
        assertEquals(RideStatus.WAITING, service.getStatus(rideId, at = arrivedAt.plusSeconds(1)))
        assertEquals(RideStatus.WAITING, service.getStatus(rideId))
    }

    @Test
    fun `unknown ride status cannot be queried`() {
        val service = RideService()
        val rideId = RideId("missing-ride")

        val error = assertFailsWith<RideNotFound> {
            service.getStatus(rideId)
        }

        assertEquals("Ride missing-ride was not found", error.message)
    }

    @Test
    fun `status is rebuilt from events stored outside the service`() {
        val eventStore = InMemoryEventStore()
        val writer = RideService(eventStore)
        val reader = RideService(eventStore)
        val rideId = RideId("ride-1")

        writer.createRide(rideId)
        writer.acceptRide(rideId)
        writer.markDriverArrived(rideId)

        assertEquals(RideStatus.WAITING, reader.getStatus(rideId))
        assertEquals(
            listOf(
                RideEvent.RideCreated(rideId),
                RideEvent.RideAccepted(rideId),
                RideEvent.DriverArrived(rideId),
            ),
            reader.getEvents(rideId),
        )
    }

    @Test
    fun `ride cannot be created twice with the same id`() {
        val service = RideService()
        val rideId = RideId("ride-1")

        service.createRide(rideId)

        val error = assertFailsWith<RideAlreadyExists> {
            service.createRide(rideId)
        }

        assertEquals("Ride ride-1 already exists", error.message)
        assertEquals(listOf(RideEvent.RideCreated(rideId)), service.getEvents(rideId))
    }
}
