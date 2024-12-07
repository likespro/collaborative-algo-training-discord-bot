package eth.likespro.codeforces

import eth.likespro.discord.Color
import org.json.JSONObject
import java.net.URL

class CodeForces {
    companion object{
        fun getDiscordColoredUsernameByRating(username: String, rating: Long): String{
            return when{
                rating < 1200 -> Color.GRAY + username + Color.RESET
                rating < 1400 -> Color.GREEN + username + Color.RESET
                rating < 1600 -> Color.CYAN + username + Color.RESET
                rating < 1900 -> Color.BLUE + username + Color.RESET
                rating < 2100 -> Color.PINK + username + Color.RESET
                rating < 2400 -> Color.YELLOW + username + Color.RESET
                rating < 3000 -> Color.RED + username + Color.RESET
                else -> Color.GRAY + username[0] + Color.RED + username.drop(1) + Color.RESET
            }
        }
        fun getUserInfoByUsername(username: String): JSONObject?{
            return try{
                val userinfo = JSONObject(URL("https://codeforces.com/api/user.info?handles=$username&checkHistoricHandles=false").readText())
                userinfo.getJSONArray("result").getJSONObject(0).apply {
                    put("discordColoredUsername", getDiscordColoredUsernameByRating(username, this.getLong("rating")))
                }
            }catch (e: Exception){e.printStackTrace(); null}
        }
        fun getUserResultInContestByUsername(contestId: Long, username: String): JSONObject?{
            return try{
                val userinfo = JSONObject(URL("https://codeforces.com/api/contest.standings?contestId=$contestId&asManager=false&from=1&count=10000&handles=$username&showUnofficial=true").readText())
                userinfo.getJSONObject("result").getJSONArray("rows").getJSONObject(0)
            }catch (e: Exception){e.printStackTrace(); null}
        }
    }
}