package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskTitle = intent.getStringExtra("task_title") ?: "Unknown Task"
        val reminderType = intent.getIntExtra("reminder_type", 1)
        
        val notificationHelper = NotificationHelper(context)
        
        val message = if (reminderType == 1) {
            "Tu tarea '$taskTitle' vence en 24 horas."
        } else {
            "¡Urgente! Tu tarea '$taskTitle' vence en 2 horas."
        }
        
        notificationHelper.showNotification("Recordatorio de Tarea", message)
    }
}
