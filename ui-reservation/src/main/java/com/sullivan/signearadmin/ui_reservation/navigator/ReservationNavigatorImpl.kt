package com.sullivan.signearadmin.ui_reservation.navigator

import android.app.Activity
import com.sullivan.common.ui_common.ex.launchActivity
import com.sullivan.common.ui_common.navigator.ReservationNavigator
import com.sullivan.signearadmin.ui_reservation.ui.RealTimeReservationActivity
import com.sullivan.signearadmin.ui_reservation.ui.ReservationActivity
import javax.inject.Inject

class ReservationNavigatorImpl @Inject constructor() : ReservationNavigator {
    override fun openReservationHome(activity: Activity) {
        activity.launchActivity<ReservationActivity>()
    }

    override fun openRealTimeReservationHome(activity: Activity) {
        activity.launchActivity<RealTimeReservationActivity>()
    }
}