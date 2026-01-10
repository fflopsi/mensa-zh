package ch.florianfrauenfelder.mensazh.models

import ch.florianfrauenfelder.mensazh.services.providers.MensaProvider
import ch.florianfrauenfelder.mensazh.ui.Destination
import ch.florianfrauenfelder.mensazh.ui.Weekday

data class Params(
    val destination: Destination,
    val weekday: Weekday,
    val language: MensaProvider.Language,
)
