package com.sullivan.signearadmin.ui_reservation.state

sealed class ReservationState {
    object NotRead : ReservationState()
    object NotConfirm : ReservationState()
    object Confirm : ReservationState()
    class Cancel(reason: String = "") : ReservationState()
    class Reject(reason: String = "") : ReservationState()
    object Served : ReservationState()
}
