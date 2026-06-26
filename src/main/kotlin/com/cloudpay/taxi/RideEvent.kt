package com.cloudpay.taxi

import java.time.Instant

sealed interface RideEvent {
    val rideId: RideId
    val occurredAt: Instant

    data class RideCreated(
        override val rideId: RideId,
        override val occurredAt: Instant = Instant.EPOCH,
    ) : RideEvent

    data class RideAccepted(
        override val rideId: RideId,
        override val occurredAt: Instant = Instant.EPOCH,
    ) : RideEvent

    data class DriverArrived(
        override val rideId: RideId,
        override val occurredAt: Instant = Instant.EPOCH,
    ) : RideEvent

    data class PassengerPickedUp(
        override val rideId: RideId,
        override val occurredAt: Instant = Instant.EPOCH,
    ) : RideEvent

    data class RideFinished(
        override val rideId: RideId,
        override val occurredAt: Instant = Instant.EPOCH,
    ) : RideEvent

    data class RideCanceled(
        override val rideId: RideId,
        override val occurredAt: Instant = Instant.EPOCH,
    ) : RideEvent
}
