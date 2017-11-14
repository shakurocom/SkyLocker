package com.shakuro.skylocker

import com.shakuro.skylocker.system.SchedulersProvider
import io.reactivex.schedulers.Schedulers

class TestSchedulers : SchedulersProvider {
    override fun ui() = Schedulers.trampoline()
    override fun computation() = Schedulers.trampoline()
    override fun trampoline() = Schedulers.trampoline()
    override fun newThread() = Schedulers.trampoline()
    override fun io() = Schedulers.trampoline()
}