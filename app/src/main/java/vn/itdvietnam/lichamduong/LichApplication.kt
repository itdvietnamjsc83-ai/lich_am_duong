package vn.itdvietnam.lichamduong

import android.app.Application
import vn.itdvietnam.lichamduong.reminder.ReminderNotifications
import vn.itdvietnam.lichamduong.reminder.ReminderRepository

class LichApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ReminderNotifications.createChannel(this)
        ReminderRepository.get(this).rescheduleAll()
    }
}
