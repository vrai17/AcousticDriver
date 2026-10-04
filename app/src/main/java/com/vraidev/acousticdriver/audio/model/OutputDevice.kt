package com.vraidev.acousticdriver.audio.model

enum class OutputDevice(val displayName: String, val iconName: String) {
    PHONE_SPEAKER("Phone Speaker", "VolumeUp"),
    WIRED_HEADSET("Wired Headphones", "Headphones"),
    USB_DAC("USB Audio DAC", "Usb"),
    BLUETOOTH("Bluetooth A2DP", "Bluetooth"),
    BLUETOOTH_LE("Bluetooth LE Audio", "BluetoothAudio")
}
