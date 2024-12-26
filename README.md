<p align="center">
 <img width="100px" src="https://github.com/likespro.png" align="center" alt="Competitive Template" />
 <h2 align="center">Competitive Programming Statistics Bot</h2>
 <p align="center">A Discord bot to track users on competitive programming platforms</p>
</p>
<p align="center">
    <a href="https://github.com/likespro/cp-programming-stats-bot/actions/workflows/release-branch.yml">
      <img alt="Build Passing" src="https://github.com/likespro/cp-programming-stats-bot/workflows/Release Branch Workflow/badge.svg" />
    </a>
    <a href="https://github.com/likespro/cp-programming-stats-bot/graphs/contributors">
      <img alt="GitHub Contributors" src="https://img.shields.io/github/contributors/likespro/cp-programming-stats-bot" />
    </a>
    <a href="https://github.com/likespro/cp-programming-stats-bot/issues">
      <img alt="Issues" src="https://img.shields.io/github/issues/likespro/cp-programming-stats-bot?color=0088ff" />
    </a>
    <a href="https://github.com/likespro/cp-programming-stats-bot/pulls">
      <img alt="GitHub pull requests" src="https://img.shields.io/github/issues-pr/likespro/cp-programming-stats-bot?color=0088ff" />
    </a>
  </p>
<p align="center">
    <a href="https://github.com/likespro/cp-programming-stats-bot/actions/workflows/main-branch.yml">
      <img alt="Build Passing" src="https://github.com/likespro/cp-programming-stats-bot/workflows/Main Branch Workflow/badge.svg" />
    </a>
    <a href="https://app.codacy.com/gh/likespro/cp-programming-stats-bot/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade"><img src="https://app.codacy.com/project/badge/Grade/95680a7d945249c4bad2aa933930cc35"/></a>
    <a href="https://github.com/likespro/cp-programming-stats-bot">
      <img alt="Git Size" src="https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/wiki/likespro/cp-programming-stats-bot/internal-git-size.md" />
    </a>
    <a href="https://github.com/likespro/cp-programming-stats-bot">
      <img alt="Git File Count" src="https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/wiki/likespro/cp-programming-stats-bot/internal-git-file-count.md" />
    </a>
    <a href="https://github.com/likespro/cp-programming-stats-bot">
      <img alt="Git Lines Of Code" src="https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/wiki/likespro/cp-programming-stats-bot/internal-git-lines-of-code.md" />
    </a>
  </p>





## Overview
The goal of this Discord bot is to bring users' statistics from different competitive programming platforms right to Discord.
## How it works
* Bot uses 2 text channels in your Guild to publish statistics: one (by default named `statistics`) to publish global leaderboard of tracked users, ranked by their rating, and another one (by default named `contests-statistics`) to publish current tracked users standings for each contest that is going on or ended no earlier than one week before.
* Bot automatically updates statistics nearly each 60 seconds and edits messages with outdated statistics 
* To track new user just execute `/track <platform> <username> <track_this_user_just_for_fun_or_not?>` in Discord Guild where the bot was installed

**Currently supported platforms:**
* CodeForces
## Requirements
* Running MongoDB server - you can download it on www.mongodb.com or use free cluster from MongoDB Atlas - https://cloud.mongodb.com
* Discord bot token - you can create it on https://discord.com/developers/applications. The bot needs to be installed to your Guild and need to have scopes `applications.commands`, `bot` and permissions `Manage Channels`, `Send Messages`. You also need to enable all Privileged Gateway Intents. You can set all them in your dashboard before bot installation to Guild.
## How to install (traditional)
* Build jars with `./gradlew build` or download already built fat jar from the last release on https://github.com/likespro/cp-programming-stats-bot/releases
* Ensure you have set all necessary environment variables (see the [list](#environment-variables) below)
* Run fat jar with Java 21+: `java -jar cp-programming-stats-bot-fat-<version>.jar`. If you built jars manually, they will be located in `build/libs/`
## How to install (docker)
* Pull image: `docker image pull ghcr.io/likespro/cp-programming-stats-bot:latest`
* Run the image with `docker run ghcr.io/likespro/cp-programming-stats-bot:latest`. You can set environment variables (see the [list](#environment-variables) below) by `-e <VARIABLE>=<VALUE>`, for example: `docker run -e MONGODB_URL="mongodb+srv://example.com" -e MONGODB_DATABASE="stats-database" ghcr.io/likespro/cp-programming-stats-bot:latest`
## How to install (helm)
* Add repository with `helm repo add cp-programming-stats-bot https://likespro.github.io/cp-programming-stats-bot` and update it with `helm repo update`
* Deploy Helm Chart: `helm install <pod_name> cp-programming-stats-bot/cp-programming-stats-bot`. You can set environment variables (see the [list](#environment-variables) below) by `--set env.<VARIABLE>=<VALUE>`, for example: `helm install discord-bot-pod-name cp-programming-stats-bot/cp-programming-stats-bot --set env.MONGODB_URL="mongodb+srv://example.com" --set env.MONGODB_DATABASE="stats-database"`
## Environment variables
* Set `MONGODB_URL` environment variable to valid URL of your MongoDB server, for example: `mongodb+srv://[username:password@]host[/[defaultauthdb][?options]]`
* [ Optional ] Set `MONGODB_DATABASE` environment variable to valid MongoDB database name. If not specified, `cp-programming-stats-bot` will be used as name
* [ Optional ] Set `DISCORD_BOT_TOKEN` environment variable to valid Discord bot token. If not specified, you will need to set `<database>/misc/config/discordBotToken` field manually after the first launch
* [ Optional ] Set `DISCORD_GUILD_ID` environment variable to valid Discord Guild ID. You can get ID of any guild by enabling Developer mode in `Discord/Settings/Advanced` and then right-click on the Guild you want to use and copy its ID. If not specified, you will need to set `<database>/misc/config/discordGuildId` field manually after the first launch
* [ Optional ] Set `DISCORD_GLOBAL_STATISTICS_CHANNEL_ID` environment variable to valid ID of the Discord text channel where to publish global statistics. You can get ID of any text channel by enabling Developer mode in `Discord/Settings/Advanced` and then right-click on the text channel you want to use and copy its ID. If not specified, bot will try to automatically find a text channel with name "statistics". If no such channels were found, bot will create a new text channel with this name
* [ Optional ] Set `DISCORD_CONTESTS_STATISTICS_CHANNEL_ID` environment variable to valid ID of the Discord text channel where to publish contests statistics. You can get ID of any text channel by enabling Developer mode in `Discord/Settings/Advanced` and then right-click on the text channel you want to use and copy its ID. If not specified, bot will try to automatically find a text channel with name "contests-statistics". If no such channels were found, bot will create a new text channel with this name
## Screenshots
Global Statistics
![Global Statistics](https://github.com/likespro/cp-programming-stats-bot/blob/main/screenshots/global_statistics.png?raw=true)
Contest Statistics
![Contest Statistics](https://github.com/likespro/cp-programming-stats-bot/blob/main/screenshots/contest_statistics.png?raw=true)