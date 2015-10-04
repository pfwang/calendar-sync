package calendarsync.facebook

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils

import scala.util.Try

object API {
  def getLongTermToken(shortTermToken: String): Try[String] = {
    val baseUrl = "https://graph.facebook.com/oauth/access_token"
    val clientId = scala.util.Properties.envOrElse("FB_CLIENT_ID", "no client id")
    val clientSecret = scala.util.Properties.envOrElse("FB_CLIENT_SECRET", "no secret id")
    val grantType = "fb_exchange_token"
    val client = HttpClientBuilder.create().build()
    val get = new HttpGet(s"$baseUrl?grant_type=$grantType&client_id=$clientId&client_secret=$clientSecret&fb_exchange_token=$shortTermToken")

    val response = client.execute(get)
    val responseStr = EntityUtils.toString(response.getEntity)
    val tokenExpirePair = responseStr.split("&")
    get.releaseConnection()
    return Try(tokenExpirePair(0))
  }

  def getEvents(token: String, userId: String): Try[String] = {
    val url = s"https://graph.facebook.com//v2.4/me/events?access_token=$token"
    val client = HttpClientBuilder.create().build()
    val get = new HttpGet(url)

    val response = client.execute(get)
    val responseStr = EntityUtils.toString(response.getEntity)
    get.releaseConnection()
    return Try(responseStr)
  }
}
