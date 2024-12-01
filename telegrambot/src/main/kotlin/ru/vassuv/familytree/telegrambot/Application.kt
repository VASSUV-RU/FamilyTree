package ru.vassuv.familytree.telegrambot

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import ru.vassuv.familytree.telegrambot.calbackquery.buildApiServer
import ru.vassuv.familytree.telegrambot.calbackquery.buildTelegramServer
import ru.vassuv.familytree.telegrambot.calbackquery.buildWeb
import ru.vassuv.familytree.telegrambot.calbackquery.deployApiServer
import ru.vassuv.familytree.telegrambot.calbackquery.deployTelegramServer
import ru.vassuv.familytree.telegrambot.calbackquery.deployWeb
import ru.vassuv.familytree.telegrambot.calbackquery.info as infoCallbackQuery
import ru.vassuv.familytree.telegrambot.calbackquery.register
import ru.vassuv.familytree.telegrambot.calbackquery.restartServer
import ru.vassuv.familytree.telegrambot.command.info as infoCommand
import ru.vassuv.familytree.telegrambot.command.start

fun main() {
    val bot = bot {
        token = Consts.TELEGRAM_DEPLOY_BOT_KEY
        dispatch {
            callbackQueries()
            commands()

            listenWebhooks()
            //other()
        }
    }
    bot.startPolling()
    println("Телеграм бот запущен!!!")
}

private fun Dispatcher.callbackQueries() {
    infoCallbackQuery()
    register()
    restartServer()
    buildApiServer()
    buildTelegramServer()
    buildWeb()
    deployApiServer()
    deployTelegramServer()
    deployWeb()
}


internal fun Dispatcher.commands() {
    infoCommand()
    start()
}