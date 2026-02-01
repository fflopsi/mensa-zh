package ch.florianfrauenfelder.mensazh.domain.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlin.uuid.Uuid

@Parcelize
data class NavigationDetail(
  val mensaId: Uuid,
  val menuIndex: Int,
) : Parcelable
