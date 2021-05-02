package com.example.googlemap

import android.app.Application
import eraapps.bankasia.bdinternetbanking.apps.room.AtmRoomModel
import java.util.ArrayList

class GlobalVariable : Application() {

    var atmList: ArrayList<AtmRoomModel>? = null
}