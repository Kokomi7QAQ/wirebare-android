package top.sankokomi.wirebare.ui.util

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object Global {

    @Suppress("StaticFieldLeak")
    lateinit var appContext: Context
        private set

    val scope = MainScope()

    enum class AppState { FOREGROUND, BACKGROUND }

    private val _appState = MutableStateFlow(AppState.FOREGROUND)

    val appState = _appState.asStateFlow()

    fun attach(context: Context) {
        appContext = context
        ProcessLifecycleOwner.get().lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    _appState.value = AppState.FOREGROUND
                }

                Lifecycle.Event.ON_STOP -> {
                    _appState.value = AppState.BACKGROUND
                }

                else -> {}
            }
        })
    }

}