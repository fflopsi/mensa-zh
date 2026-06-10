package ch.florianfrauenfelder.mensazh.domain.navigation

enum class Destination(val code: String) {
  Today("Today"), Tomorrow("Tomorrow"), ThisWeek("ThisWeek"), NextWeek("NextWeek");

  companion object {
    fun fromCode(code: String) =
      entries.firstOrNull { it.code == code } ?: error("$code is not a destination")
  }
}
