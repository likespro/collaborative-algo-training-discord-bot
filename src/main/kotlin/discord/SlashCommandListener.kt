package eth.likespro.discord

import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import eth.likespro.localeMessagesDiscord
import eth.likespro.log
import eth.likespro.mongoClient
import eth.likespro.trackedUsersCollection
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bson.Document
import org.json.JSONObject
import reactor.core.publisher.Mono
import java.net.URL


class SlashCommandListener : ListenerAdapter() {
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        super.onSlashCommandInteraction(event)
        log.debug { "Processing command ${event.name}" }
        when(event.name){
            "track" -> {

                val platform = event.options[0].asString
                val username = event.options[1].asString
                val trackForFun = event.options[2].asBoolean
                when(platform.lowercase()){
                    "codeforces" -> {
                        if(username.contains(";") || try {JSONObject(URL("https://codeforces.com/api/user.info?handles=$username&checkHistoricHandles=false").readText()).getString("status") != "OK"} catch (e: Exception){e.printStackTrace(); true}){
                            event.reply(localeMessagesDiscord.getString("cmd_track_error_invalid_username")
                                .replace("%USERNAME_ENTERED", username)).queue()
                            return
                        }
                        val databaseResult = Mono.from(mongoClient.startSession()).flatMap { session ->
                            session.startTransaction()
                            if(Mono.from(trackedUsersCollection.countDocuments(and(eq("platform", platform), eq("username", username)))).block()!! != 0L) {
                                event.reply(localeMessagesDiscord.getString("cmd_track_error_tracked_username")
                                    .replace("%USERNAME_ENTERED", username)
                                    .replace("%PLATFORM_ENTERED", platform)).queue()
                                return@flatMap Mono.from(session.abortTransaction())
                            }
                            Mono.defer {
                                Mono.from(trackedUsersCollection.insertOne(Document()
                                    .append("platform", platform)
                                    .append("username", username)
                                    .append("discordId", -1L)
                                    .append("forFun", trackForFun)
                                    .append("metadata", Document()
                                        .append("lastRating", Int.MAX_VALUE)))).then(Mono.from(session.commitTransaction()).thenReturn(true))
                            }
                        }.block() ?: return
                        event.reply(localeMessagesDiscord.getString("cmd_track_success")
                            .replace("%USERNAME_ENTERED", username)
                            .replace("%PLATFORM_ENTERED", platform)).queue()
                    }
                    else -> {
                        event.reply(localeMessagesDiscord.getString("cmd_track_error_invalid_platform")
                            .replace("%PLATFORM_ENTERED", platform)).queue()
                        return
                    }
                }
            }
        }
    }
}