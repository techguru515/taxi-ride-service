package com.cloudpay.taxi

class InvalidRideTransition(
    rideId: RideId,
    currentStatus: RideStatus,
    attemptedEvent: RideEvent,
) : IllegalStateException(
    "Cannot move ride ${rideId.value} from $currentStatus using ${attemptedEvent::class.simpleName}",
)
