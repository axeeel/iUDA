package com.example.iuda.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import com.example.iuda.R
import com.example.iuda.databinding.FragmentNotificationsBinding
import com.example.iuda.iuda.Companion.prefs


class NotificationsFragment : Fragment() {
    private lateinit var binding: FragmentNotificationsBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cargar estados guardados
        loadSwitchStates()

        // Listener para el switch de todas las notificaciones
        binding.switchAllNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.saveAllNotificationsSwitchState(isChecked)
            if (isChecked) {
                // Si switch_all_notifications está en ON, pon los demás en ON y guarda el estado
                setAndSaveSwitchState(binding.switchFriendRequest, true)
                setAndSaveSwitchState(binding.switchAlertNotification, true)
                setAndSaveSwitchState(binding.switchAlarmNotification, true)
            }
        }

        // Crea un listener común para los otros switches
        val individualSwitchListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            when (buttonView.id) {
                R.id.switch_friend_request -> prefs.saveFriendRequestSwitchState(isChecked)
                R.id.switch_alert_notification -> prefs.saveAlertNotificationSwitchState(isChecked)
                R.id.switch_alarm_notification -> prefs.saveAlarmNotificationSwitchState(isChecked)
            }
            // Si alguno de los switches individuales está en OFF, pon switch_all_notifications en OFF
            if (!isChecked) {
                setAndSaveSwitchState(binding.switchAllNotifications, false)
            }
        }

        // Aplica el listener a los otros switches
        binding.switchFriendRequest.setOnCheckedChangeListener(individualSwitchListener)
        binding.switchAlertNotification.setOnCheckedChangeListener(individualSwitchListener)
        binding.switchAlarmNotification.setOnCheckedChangeListener(individualSwitchListener)
    }

    private fun loadSwitchStates() {
        binding.switchAllNotifications.isChecked = prefs.getAllNotificationsSwitchState()
        binding.switchFriendRequest.isChecked = prefs.getFriendRequestSwitchState()
        binding.switchAlertNotification.isChecked = prefs.getAlertNotificationSwitchState()
        binding.switchAlarmNotification.isChecked = prefs.getAlarmNotificationSwitchState()
    }

    private fun setAndSaveSwitchState(switch: CompoundButton, isChecked: Boolean) {
        switch.isChecked = isChecked
        when (switch.id) {
            R.id.switch_all_notifications -> prefs.saveAllNotificationsSwitchState(isChecked)
            R.id.switch_friend_request -> prefs.saveFriendRequestSwitchState(isChecked)
            R.id.switch_alert_notification -> prefs.saveAlertNotificationSwitchState(isChecked)
            R.id.switch_alarm_notification -> prefs.saveAlarmNotificationSwitchState(isChecked)
        }
    }


}