package com.example.sharist.ui.components.events

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

object UiEventBus {
    private val _events = Channel<UiEvent>(capacity = Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    suspend fun send(event: UiEvent) = _events.send(event)

    // For non-suspend call sites:
    fun tryEmit(event: UiEvent, scope: CoroutineScope) {
        scope.launch { _events.send(event) }
    }
}