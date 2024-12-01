package ru.vassuv.familytree.telegrambot

internal object Consts {
    val TELEGRAM_WEBHOOK_BOT_KEY = env("TELEGRAM_WEBHOOK_BOT_KEY")
    val TELEGRAM_DEPLOY_BOT_KEY = env("TELEGRAM_DEPLOY_BOT_KEY")
    val TELEGRAM_DEPLOY_BOT_ID= env("TELEGRAM_DEPLOY_BOT_ID")
    val TELEGRAM_BOT_CHANNEL_ID = envLong("TELEGRAM_BOT_CHANNEL_ID")
    val TELEGRAM_BOT_MAIN_USER_ID = envLong("TELEGRAM_BOT_MAIN_USER_ID")
    val INFO = """
        /start - Начало работы с ботом
        /info - спросить все команды у бота
    """.trimIndent()

    private fun env(key: String): String {
        return runCatching { System.getenv(key) }
            .onFailure { println("getenv($key) no value"); println(it) }
            .getOrNull() ?: ""
    }

    private fun envLong(key: String) = env(key).toLongOrNull() ?: 0L
}