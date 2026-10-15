package app.mobiling.client.notification

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AndroidPushTokenEvents {
    private val mutableChanges = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val changes = mutableChanges.asSharedFlow()

    fun changed() {
        mutableChanges.tryEmit(Unit)
    }
}
