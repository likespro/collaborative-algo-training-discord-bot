package eth.likespro

import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.reactivestreams.client.MongoDatabase
import de.vandermeer.asciitable.AsciiTable
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment
import eth.likespro.codeforces.CodeForces
import eth.likespro.discord.SlashCommandListener
import mu.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import org.bson.Document
import org.json.JSONObject
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.URL
import java.util.*
import kotlin.system.exitProcess


val log = KotlinLogging.logger {  }

lateinit var mongoClient: MongoClient
lateinit var database: MongoDatabase
lateinit var problemsCollection: MongoCollection<Document>
lateinit var trackedUsersCollection: MongoCollection<Document>
lateinit var sentMessagesCollection: MongoCollection<Document>
lateinit var miscCollection: MongoCollection<Document>
lateinit var config: Document
lateinit var localesCollection: MongoCollection<Document>
lateinit var localeMessages: Document
lateinit var localeMessagesPrimitives: Document
lateinit var localeMessagesCodeForces: Document
lateinit var localeMessagesDiscord: Document

lateinit var discordBot: JDA

fun main() {
    log.info { "" }
    log.info { "\t.__  .__ __                                                 __  .__\t\t" }
    log.info { "\t|  | |__|  | __ ____   ___________________  ____      _____/  |_|  |__\t" }
    log.info { "\t|  | |  |  |/ // __ \\ /  ___/\\____ \\_  __ \\/  _ \\   _/ __ \\   __\\  |  \\\t" }
    log.info { "\t|  |_|  |    <\\  ___/ \\___ \\ |  |_> >  | \\(  <_> )  \\  ___/|  | |   Y  \\" }
    log.info { "\t|____/__|__|_ \\\\___  >____  >|   __/|__|   \\____/ /\\ \\___  >__| |___|  /" }
    log.info { "\t             \\/    \\/     \\/ |__|                 \\/     \\/          \\/\t" }
    log.info { "\t                                                                            " }
    log.info { "" }
    log.info { "GitHub: @likespro | X (Twitter): @likespro_eth | E-Mail: likespro.eth@gmail.com" }
    log.info { "" }



    log.debug { "Creating MongoDB tools..." }
    try{
        mongoClient = MongoClients.create(System.getenv("MONGODB_URL"))
    } catch (e: Exception){
        log.error { e.stackTraceToString() }
        log.error { "Can not connect to MongoDB database. Double check MONGODB_URL environment variable and network rules." }
        exitProcess(1)
    }
    database = mongoClient.getDatabase(System.getenv("MONGODB_DATABASE") ?: "cp-programming-stats-bot")
    problemsCollection = database.getCollection("problems")
    trackedUsersCollection = database.getCollection("trackedUsers")
    sentMessagesCollection = database.getCollection("sentMessages")
    miscCollection = database.getCollection("misc")
    log.debug { "Getting config..." }
    config = Mono.from(miscCollection.find(eq("type", "config")).first()).block() ?: run {
        log.debug { "Config not found, creating default..." }
        val tempConfig = Document()
            .append("type", "config")
            .append("discordBotToken", System.getenv("DISCORD_BOT_TOKEN") ?: "<TOKEN>")
            .append("locale", "en")
            .append("discordGuildId", System.getenv("DISCORD_GUILD_ID")?.toLong() ?: -1L)
            .append("discordGlobalStatisticsChannelId", System.getenv("DISCORD_GLOBAL_STATISTICS_CHANNEL_ID")?.toLong() ?: -1L)
            .append("discordContestsStatisticsChannelId", System.getenv("DISCORD_CONTESTS_STATISTICS_CHANNEL_ID")?.toLong() ?: -1L)
        Mono.from(miscCollection.insertOne(tempConfig)).block()!!
        tempConfig
    }
    log.debug { "Getting locale..." }
    localesCollection = database.getCollection("locales")
    Mono.from(localesCollection.updateOne(and(eq("version", Locales.EN.getLong("version")), eq("locale", "en")), Updates.setOnInsert(Locales.EN), UpdateOptions().upsert(true))).block()
    localeMessages = Mono.from(localesCollection.find(and(eq("version", Locales.EN.getLong("version")), eq("locale", config.getString("locale")))).first()).block() ?: run{
        log.error { "Not found locale: ${config.getString("locale")}. Please specify a valid one in database/misc/config/locale or add new locale ${config.getString("locale")} in database/locales to proceed! The locale must be version ${Locales.EN}." }
        exitProcess(1)
    }

    localeMessagesPrimitives = localeMessages["primitives"] as Document
    localeMessagesCodeForces = localeMessages["codeforces"] as Document
    localeMessagesDiscord = localeMessages["discord"] as Document



    log.debug { "Setting up discord bot..." }
    if(config.getString("discordBotToken") == "<TOKEN>") {
        if(config.getLong("discordGuildId") == -1L) log.error { "Invalid Discord Guild ID. Please specify a valid one in database/misc/config/discordGuildId to proceed! You can get ID of any Guild by enabling Developer mode in Discord/Settings/Advanced and then right-click on the Guild and copy its ID." }
        log.error { "Invalid discord bot token. Please specify a valid one in database/misc/config/discordBotToken to proceed!" }
        exitProcess(1)
    }
    else{
        discordBot = JDABuilder.createLight(config.getString("discordBotToken"), EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT))
            .addEventListeners(SlashCommandListener())
            .build()
        discordBot.awaitReady()
    }
    val commands: CommandListUpdateAction = discordBot.updateCommands()
    commands.addCommands(
        Commands.slash("track", localeMessagesDiscord.getString("cmd_track"))
            .addOptions(
                OptionData(OptionType.STRING, "platform", localeMessagesDiscord.getString("cmd_track_platform"), true),
                OptionData(OptionType.STRING, "user", localeMessagesDiscord.getString("cmd_track_user"), true),
                OptionData(OptionType.BOOLEAN, "for_fun", localeMessagesDiscord.getString("cmd_track_for_fun"), true),
            )
            .setGuildOnly(true)
    )
    commands.queue()




    while (true){
        try{
            log.debug { "Getting channel..." }
            if(config.getLong("discordGuildId") == -1L){
                log.error { "Invalid Discord Guild ID. Please specify a valid one in database/misc/config/discordGuildId to proceed! You can get ID of any Guild by enabling Developer mode in Discord/Settings/Advanced and then right-click on the Guild and copy its ID." }
                exitProcess(1)
            }
            val guild = discordBot.getGuildById(config.getLong("discordGuildId")) ?: run {
                log.error { "Not found Discord Guild ID. Please specify a valid one in database/misc/config/discordGuildId to proceed! You can get ID of any Guild by enabling Developer mode in Discord/Settings/Advanced and then right-click on the Guild and copy its ID." }
                exitProcess(1)
            }






            // <======================================================>
            // <==================== GLOBAL STATS ====================>
            // <======================================================>






            if(config.getLong("discordGlobalStatisticsChannelId") == -1L){
                log.debug { "Getting channel by name..." }
                val channels = guild.getTextChannelsByName(localeMessagesDiscord.getString("global_statistics_channel"), true).toMutableList()
                if(channels.isEmpty()) {
                    log.debug { "Creating channel..." }
                    channels.add(guild.createTextChannel(localeMessagesDiscord.getString("global_statistics_channel")).complete())
                }
                config["discordGlobalStatisticsChannelId"] = channels[0].idLong
                Mono.from(miscCollection.replaceOne(eq("type", "config"), config)).block()
            }
            var channel = discordBot.getTextChannelById(config.getLong("discordGlobalStatisticsChannelId"))!!

            log.debug { "Getting messages..." }
            var messagesIds = Flux.from(sentMessagesCollection.find(eq("type", "globalStatistics"))).collectList().block()!!.sortedBy { it.getLong("part") }.map { it.getLong("discordMessageId") }

            log.debug { "Creating table..." }
            var messagesContents = mutableListOf(localeMessagesDiscord.getString("global_statistics_title")
                .replace("%PLATFORM", "CodeForces"))
            var lastTableEmpty = false
            var asciiTable = AsciiTable()
            asciiTable.addRule()
            asciiTable.addRow(localeMessagesDiscord.getString("global_statistics_rank"), localeMessagesDiscord.getString("global_statistics_username"), localeMessagesDiscord.getString("global_statistics_rating"), localeMessagesDiscord.getString("global_statistics_contribution"))
            asciiTable.addRule()

            log.debug { "Getting users..." }
            var i = 0
            var failedFetching = mutableListOf<String>()
            val fetchedCodeForcesUsers = Flux.from(trackedUsersCollection.find(eq("platform", "codeforces"))).collectList().block()?.map {
                Thread.sleep(200) // To bypass API Rate Limit
                CodeForces.getUserInfoByUsername(it.getString("username"))?.put("forFun",it.getBoolean("forFun")) ?: run { failedFetching.add(it.getString("username")); null }
            }?.sortedBy { it?.getLong("rating") }?.reversed()

            log.debug { "Building table..." }
            var pushTable = { renderedTable: String ->
                var coloredRenderedTable = renderedTable
                fetchedCodeForcesUsers?.forEach { if(it != null) {
                    coloredRenderedTable = coloredRenderedTable.replace(" "+it.getString("handle")+" ", " "+it.getString("discordColoredUsername")+" ")
                        .replace("*"+it.getString("handle")+" ", "*"+it.getString("discordColoredUsername")+" ")
                } }
                messagesContents.add("```ansi\n$coloredRenderedTable```")
                asciiTable = AsciiTable()
                asciiTable.addRule()
            }
            fetchedCodeForcesUsers?.forEach { userinfo ->
                if(userinfo != null) {
                    asciiTable.addRow(++i, (if(userinfo.getBoolean("forFun")) "*" else "") + userinfo.getString("handle"), userinfo.getLong("rating"), userinfo.getLong("contribution"))
                    asciiTable.addRule()

                    asciiTable.setTextAlignment(TextAlignment.CENTER)
                    val renderedTable = asciiTable.render()
                    if(renderedTable.length > 1500){
                        pushTable(renderedTable)
                        lastTableEmpty = true
                    } else lastTableEmpty = false
                }
            }
            if(!lastTableEmpty){
                asciiTable.setTextAlignment(TextAlignment.CENTER)
                pushTable(asciiTable.render())
            }
            messagesContents.add(localeMessagesDiscord.getString("global_statistics_last_updated") + ": <t:${System.currentTimeMillis() / 1000L}:R>\n"
                    + (if(failedFetching.isNotEmpty()) localeMessagesDiscord.getString("global_statistics_failed_fetching")+": "+failedFetching + "\n\n" else "")
                    + localeMessagesDiscord.getString("global_statistics_for_fun_disclaimer"))
            log.debug { "Tables are\n$messagesContents" }

            log.debug { "Editing messages..." }
            messagesContents.forEachIndexed { index, text ->
                if(index < messagesIds.size) channel.editMessageById(messagesIds[index], text).complete()
                else {
                    val messageId = channel.sendMessage(text).complete().idLong
                    Mono.from(sentMessagesCollection.insertOne(Document()
                        .append("type", "globalStatistics")
                        .append("discordMessageId", messageId)
                        .append("part", index.toLong()))).block()!!
                }
            }






            // <=======================================================>
            // <==================== CONTEST STATS ====================>
            // <=======================================================>






            if(config.getLong("discordContestsStatisticsChannelId") == -1L){
                log.debug { "Getting channel by name..." }
                val channels = guild.getTextChannelsByName(localeMessagesDiscord.getString("contest_statistics_channel"), true).toMutableList()
                if(channels.isEmpty()) {
                    log.debug { "Creating channel..." }
                    channels.add(guild.createTextChannel(localeMessagesDiscord.getString("contest_statistics_channel")).complete())
                }
                config["discordContestsStatisticsChannelId"] = channels[0].idLong
                Mono.from(miscCollection.replaceOne(eq("type", "config"), config)).block()
            }
            channel = discordBot.getTextChannelById(config.getLong("discordContestsStatisticsChannelId"))!!

            log.debug { "Getting contests..." }
            JSONObject(URL("https://codeforces.com/api/contest.list?gym=false").readText()).getJSONArray("result").sortedBy { (it as JSONObject).getLong("startTimeSeconds") }.forEach { contest ->
                contest as JSONObject
                if(System.currentTimeMillis() - contest.getLong("startTimeSeconds") * 1000L > 604800000L) return@forEach
                if(contest.getString("phase") == "BEFORE") return@forEach
                val contestId = contest.getLong("id")
                log.debug { "Processing contest ${contest.getString("name")} ($contestId)" }

                log.debug { "Getting messages..." }
                messagesIds = Flux.from(sentMessagesCollection.find(and(eq("type", "contestStatistics"), eq("platform", "codeforces"), eq("contestId", contestId)))).collectList().block()!!.sortedBy { it.getLong("part") }.map { it.getLong("discordMessageId") }

                log.debug { "Creating table..." }
                messagesContents = mutableListOf(localeMessagesDiscord.getString("contest_statistics_title")
                    .replace("%PLATFORM", "CodeForces")
                    .replace("%CONTEST", contest.getString("name"))
                    .replace("%STATUS", (localeMessagesCodeForces["phases"] as Document).getString(contest.getString("phase")))
                    .replace("%STARTED", "<t:${contest.getLong("startTimeSeconds")}:R>")
                    .replace("%ENDS_OR_ENDED", localeMessagesDiscord.getString("contest_statistics_${if(contest.getLong("startTimeSeconds") + contest.getLong("durationSeconds") > System.currentTimeMillis()) "ends" else "ended"}") + " <t:${contest.getLong("startTimeSeconds") + contest.getLong("durationSeconds")}:R>"))
                lastTableEmpty = false
                asciiTable = AsciiTable()
                asciiTable.addRule()
                val firstRow = mutableListOf(localeMessagesDiscord.getString("contest_statistics_local_rank"), localeMessagesDiscord.getString("contest_statistics_global_rank"), localeMessagesDiscord.getString("contest_statistics_username"))
                JSONObject(URL("https://codeforces.com/api/contest.standings?contestId=$contestId&asManager=false&from=1&count=1&showUnofficial=true").readText()).getJSONObject("result").getJSONArray("problems").forEach { problem ->
                    problem as JSONObject
                    firstRow.add(problem.getString("index"))
                }
                firstRow.add(localeMessagesDiscord.getString("contest_statistics_penalty"))
                firstRow.add(localeMessagesDiscord.getString("contest_statistics_score"))
                asciiTable.addRow(firstRow)
                asciiTable.addRule()

                log.debug { "Getting users..." }
                i = 0
                failedFetching = mutableListOf()
                val fetchedCodeForcesStandings = Flux.from(trackedUsersCollection.find(eq("platform", "codeforces"))).collectList().block()?.map {
                    val userinfo = CodeForces.getUserResultInContestByUsername(contestId, it.getString("username"))?.put("forFun", it.getBoolean("forFun"))?.put("username", it.getString("username"))
                    if(userinfo == null) failedFetching.add(it.getString("username"))
                    Thread.sleep(200)
                    userinfo
                }?.sortedBy { if(it?.getLong("rank") == 0L) Long.MAX_VALUE else it?.getLong("rank")  }

                log.debug { "Building table..." }
                pushTable = { renderedTable: String ->
                    messagesContents.add("```ansi\n$renderedTable```")
                    asciiTable = AsciiTable()
                    asciiTable.addRule()
                }
                fetchedCodeForcesStandings?.forEach { userStandings ->
                    if(userStandings != null) {
                        val thisRow = mutableListOf(if(userStandings.getLong("rank") == 0L) "N/A" else (++i).toString(), if(userStandings.getLong("rank") == 0L) "N/A" else userStandings.getLong("rank").toString(), (if(userStandings.getBoolean("forFun")) "*" else "") + userStandings.getString("username"))
                        userStandings.getJSONArray("problemResults").forEach { problemResult ->
                            problemResult as JSONObject
                            thisRow.add(problemResult.getLong("points").toString() + (if(problemResult.getLong("rejectedAttemptCount") != 0L) " (-" + problemResult.getLong("rejectedAttemptCount") + ")" else ""))
                        }
                        thisRow.add(userStandings.getLong("penalty").toString())
                        thisRow.add(userStandings.getLong("points").toString())
                        asciiTable.addRow(thisRow)
                        asciiTable.addRule()

                        asciiTable.setTextAlignment(TextAlignment.CENTER)
                        val renderedTable = asciiTable.render(92)
                        if(renderedTable.length > 1500){
                            pushTable(renderedTable)
                            lastTableEmpty = true
                        } else lastTableEmpty = false
                    }
                }
                if(!lastTableEmpty){
                    asciiTable.setTextAlignment(TextAlignment.CENTER)
                    pushTable(asciiTable.render(92)) // Width to display username "likespro" clearly :)
                }
                messagesContents.add(localeMessagesDiscord.getString("contest_statistics_last_updated") + ": <t:${System.currentTimeMillis() / 1000L}:R>\n"
                        + (if(failedFetching.isNotEmpty()) localeMessagesDiscord.getString("contest_statistics_failed_fetching")+": "+failedFetching + "\n\n" else "")
                        + localeMessagesDiscord.getString("contest_statistics_for_fun_disclaimer"))
                log.debug { "Tables are\n$messagesContents" }

                log.debug { "Editing messages..." }
                messagesContents.forEachIndexed { index, text ->
                    if(messagesIds.isEmpty()) {
                        val messageId = channel.sendMessage(text).complete().idLong
                        Mono.from(sentMessagesCollection.insertOne(Document()
                            .append("type", "contestStatistics")
                            .append("part", index.toLong())
                            .append("contestId", contestId)
                            .append("platform", "codeforces")
                            .append("discordMessageId", messageId))).block()!!
                    }
                    else if(index < messagesIds.size - 1) channel.editMessageById(messagesIds[index], text).complete()
                    else if(index == messagesIds.size - 1) channel.editMessageById(messagesIds[index], messagesContents.last()).complete()
                    i = index
                }
                if(messagesIds.isEmpty()){
                    (0..<5).forEach { _ ->
                        val messageId = channel.sendMessage("⠀").complete().idLong
                        Mono.from(sentMessagesCollection.insertOne(Document()
                            .append("type", "contestStatistics")
                            .append("part", (++i).toLong())
                            .append("contestId", contestId)
                            .append("platform", "codeforces")
                            .append("discordMessageId", messageId))).block()!!
                    }
                } else{
                    while(++i < messagesIds.size) {
                        channel.editMessageById(messagesIds[i], "⠀").complete()
                    }
                }
            }
        } catch (e: Exception){
            log.error { e.stackTraceToString() }
        }
        Thread.sleep(60*1000)
    }
}