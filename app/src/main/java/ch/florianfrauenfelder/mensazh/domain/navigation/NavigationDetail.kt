package ch.florianfrauenfelder.mensazh.domain.navigation

import android.os.Parcelable
import ch.florianfrauenfelder.mensazh.domain.model.MensaState
import ch.florianfrauenfelder.mensazh.domain.model.Menu
import kotlinx.parcelize.Parcelize

@Parcelize
data class NavigationDetail(
  val mensaState: MensaState,
  val selectedMenu: Menu,
) : Parcelable
