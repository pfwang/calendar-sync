package controllers

import java.util

import controllers.Application._
import org.apache.http.NameValuePair
import org.apache.http.client.HttpClient
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.{HttpClientBuilder, DefaultHttpClient}
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import play.api.mvc.{Action, Controller}
import  play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current


object GoogleTokenExchange extends  Controller{
  def exchange(authCode: String) = Action {
    val baseUrl = "https://www.googleapis.com/oauth2/v3/token"
    val clientId = scala.util.Properties.envOrElse("GOOGLE_CLIENT_ID", "no client id")
    val clientSecret = scala.util.Properties.envOrElse("GOOGLE_CLIENT_SECRET", "no secret id")
    //val redirectUri = "http://localhost:5000/facebooklogin/"
    val redirectUri = scala.util.Properties.envOrElse("GOOGLE_REDIRECT_URI", "no redirect uri")
    val grantType = "authorization_code"
    val client = HttpClientBuilder.create().build()
    val post = new HttpPost(baseUrl)

    val nameValuePairs = new util.ArrayList[NameValuePair](1)
    nameValuePairs.add(new BasicNameValuePair("code", authCode))
    nameValuePairs.add(new BasicNameValuePair("client_id", clientId))
    nameValuePairs.add(new BasicNameValuePair("client_secret", clientSecret))
    nameValuePairs.add(new BasicNameValuePair("redirect_uri", redirectUri))
    nameValuePairs.add(new BasicNameValuePair("grant_type", grantType))
    post.setEntity(new UrlEncodedFormEntity(nameValuePairs))

    val response = client.execute(post)
    val responseStr = EntityUtils.toString(response.getEntity())
    val refreshToken = (Json.parse(responseStr) \ "refresh_token").asOpt[String]
    post.releaseConnection()

    Redirect(routes.Application.facebookLogin(refreshToken))
  }
}
