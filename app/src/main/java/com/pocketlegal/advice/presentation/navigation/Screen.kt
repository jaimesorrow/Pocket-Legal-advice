package com.pocketlegal.advice.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("sign_up")
    object Home : Screen("home")
    object Directory : Screen("legal_directory")
    object Search : Screen("search?query={query}") {
        fun withQuery(q: String) = "search?query=$q"
    }
    object Category : Screen("category/{categoryId}") {
        fun withId(id: String) = "category/$id"
    }
    object LawDetail : Screen("law/{lawId}") {
        fun withId(id: String) = "law/$id"
    }
    object AiChat : Screen("ai_chat")
    object Profile : Screen("profile")
}
