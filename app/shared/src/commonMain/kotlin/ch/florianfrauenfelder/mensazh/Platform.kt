package ch.florianfrauenfelder.mensazh

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform