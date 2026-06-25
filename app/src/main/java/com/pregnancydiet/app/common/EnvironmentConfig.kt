package com.pregnancydiet.app.common

enum class AppEnvironment {
    Development,
    Production,
}

object EnvironmentConfig {
    val current: AppEnvironment = AppEnvironment.Development
}