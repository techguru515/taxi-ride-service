package com.cloudpay.taxi

sealed interface RideEvent {
    val rideId: RideId

    data class RideCreated(override val rideId: RideId) : RideEvent
}
