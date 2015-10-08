package calendarsync.google

import java.util

import controllers.routes
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import play.api.libs.json.Json

import scala.util.Try


object GoogleApi{
  def getToken (refreshToken: String): Try[Option[String]] = {
    Try {
      val baseUrl = "https://www.googleapis.com/oauth2/v3/token"
      val clientId = scala.util.Properties.envOrElse("GOOGLE_CLIENT_ID", "no client id")
      val clientSecret = scala.util.Properties.envOrElse("GOOGLE_CLIENT_SECRET", "no secret id")
      val grantType = "refresh_token"
      val client = HttpClientBuilder.create().build()
      val post = new HttpPost(baseUrl)

      val nameValuePairs = new util.ArrayList[NameValuePair](1)
      nameValuePairs.add(new BasicNameValuePair("refresh_token", refreshToken))
      nameValuePairs.add(new BasicNameValuePair("client_id", clientId))
      nameValuePairs.add(new BasicNameValuePair("client_secret", clientSecret))
      nameValuePairs.add(new BasicNameValuePair("grant_type", grantType))
      post.setEntity(new UrlEncodedFormEntity(nameValuePairs))

      val response = client.execute(post)
      val responseStr = EntityUtils.toString(response.getEntity())
      val accessToken: Option[String] = (Json.parse(responseStr) \ "access_token").asOpt[String]
      post.releaseConnection()
      accessToken
    }
  }
}