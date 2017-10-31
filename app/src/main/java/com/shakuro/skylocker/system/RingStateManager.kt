package com.shakuro.skylocker.system

import android.content.Context
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import io.reactivex.Observable

class RingStateManager(val context: Context) {

    fun register(): Observable<Unit> {
        return Observable.create<Unit> { observer ->
            try {
                val manager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val listener: PhoneStateListener = object : PhoneStateListener() {

                    override fun onCallStateChanged(state: Int, incomingNumber: String) {
                        super.onCallStateChanged(state, incomingNumber)
                        when (state) {
                            TelephonyManager.CALL_STATE_RINGING -> observer.onNext(Unit)
                        }
                    }
                }
                // subscribe for ringing state
                manager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
                // unsubscribe on dispose
                observer.setCancellable { manager.listen(listener, PhoneStateListener.LISTEN_NONE) }
            } catch (error: Throwable) {
                observer.onError(error)
            }
        }.publish()
    }
}