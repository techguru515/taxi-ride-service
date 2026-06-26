package com.cloudpay.taxi

sealed interface RideEvent {
    val rideId: RideId

    data class RideCreated(override val rideId: RideId) : RideEvent
    data class RideAccepted(override val rideId: RideId) : RideEvent
    data class DriverArrived(override val rideId: RideId) : RideEvent
}
