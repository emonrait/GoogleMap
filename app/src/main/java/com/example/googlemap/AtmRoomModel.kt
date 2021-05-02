package eraapps.bankasia.bdinternetbanking.apps.room

import java.io.Serializable


data class AtmRoomModel(


    var sl: Int = 0,


    var atmCode: String = "",


    var atmLocation: String = "",


    var atmLogitude: String = "",


    var atmLatitued: String = "",


    var distance: Float? = 0f


):Serializable

