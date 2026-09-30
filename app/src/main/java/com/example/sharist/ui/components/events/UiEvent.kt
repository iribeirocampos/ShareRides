package com.example.sharist.ui.components.events


sealed class UiEvent {

    data class Success(
        val message: String
    ) : UiEvent()

    data class Error(
        val message: String
    ) : UiEvent()

    data class Warning(
        val message: String
    ) : UiEvent()

    data class Info(
        val message: String
    ) : UiEvent()
}