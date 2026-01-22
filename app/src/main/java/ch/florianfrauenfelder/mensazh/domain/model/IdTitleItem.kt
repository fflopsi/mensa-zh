package ch.florianfrauenfelder.mensazh.domain.model

import kotlin.uuid.Uuid

interface IdTitleItem {
  val id: Uuid
  val title: String
}
