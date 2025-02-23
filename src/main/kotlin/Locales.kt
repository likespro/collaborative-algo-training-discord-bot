package eth.likespro

import org.bson.Document

class Locales {
    companion object{
        val EN = Document()
            .append("version", 2L)
            .append("locale", "en")
            .append("primitives", Document()
                .append("months", Document()
                    .append("Jan", "Jan")
                    .append("Feb", "Feb")
                    .append("Mar", "Mar")
                    .append("Apr", "Apr")
                    .append("May", "May")
                    .append("Jun", "Jun")
                    .append("Jul", "Jul")
                    .append("Aug", "Aug")
                    .append("Sep", "Sep")
                    .append("Oct", "Oct")
                    .append("Nov", "Nov")
                    .append("Dec", "Dec")))
            .append("codeforces", Document()
                .append("phases", Document()
                    .append("BEFORE", "Will be in future")
                    .append("CODING", "Coding")
                    .append("PENDING_SYSTEM_TEST", "Pending system testing...")
                    .append("SYSTEM_TEST", "System testing...")
                    .append("FINISHED", "Finished")))
            .append("discord", Document()
                .append("global_statistics_channel", "statistics")
                .append("global_statistics_title", "## Global Statistics on %PLATFORM")
                .append("global_statistics_rank", "#")
                .append("global_statistics_username", "Username")
                .append("global_statistics_rating", "Rating")
                .append("global_statistics_contribution", "Contribution")
                .append("global_statistics_failed_fetching", "Failed fetching (maybe they changed usernames)")
                .append("global_statistics_for_fun_disclaimer", "**Users starting with * are tracked just for fun.**")
                .append("global_statistics_last_updated", "Last updated")

                .append("contest_statistics_channel", "contests-statistics")
                .append("contest_statistics_local_rank", "Local #")
                .append("contest_statistics_global_rank", "Global #")
                .append("contest_statistics_username", "Username")
                .append("contest_statistics_penalty", "Penalty")
                .append("contest_statistics_score", "Score")
                .append("contest_statistics_title", "## Contest Statistics on %PLATFORM: %CONTEST | Contest status: %STATUS | Started %STARTED | %ENDS_OR_ENDED")
                .append("contest_statistics_failed_fetching", "Failed fetching (not participated or  changed usernames)")
                .append("contest_statistics_for_fun_disclaimer", "**Users starting with * are tracked just for fun.**")
                .append("contest_statistics_ends", "Ends")
                .append("contest_statistics_ended", "Ended")
                .append("contest_statistics_last_updated", "Last updated")

                .append("cmd_track", "Track specified user on specifies platform")
                .append("cmd_track_platform", "Platform on which to track user. These platforms are supported: CodeForces")
                .append("cmd_track_user", "The user`s to track username/login/handle")
                .append("cmd_track_for_fun", "Will this user be tracked just for fun? These users will be marked with *")
                .append("cmd_track_success", "User %USERNAME_ENTERED is successfully tracked now on platform %PLATFORM_ENTERED. It can take nearly 2 minutes to new username to appear in statistics.")
                .append("cmd_track_error_invalid_platform", "Error: invalid platform entered: %PLATFORM_ENTERED. Currently supported platforms: CodeForces")
                .append("cmd_track_error_invalid_username", "Error: invalid username entered or username is not found: %USERNAME_ENTERED.")
                .append("cmd_track_error_tracked_username", "Error: user %USERNAME_ENTERED is already tracking on platform %PLATFORM_ENTERED."))
    }
}