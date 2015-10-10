package calendarsync.google


import java.util

import com.google.api.client.auth.oauth2.{BearerToken, Credential}
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.{Event, EventDateTime}
import controllers.routes
import models.stringPlace

//import models.{Event, User}
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import play.api.libs.json.Json

import scala.util.{Failure, Success, Try}


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



  def createEvent(user: models.User, newEvent: models.Event): Unit ={
    println("createEvent")
    val HTTP_TRANSPORT = new NetHttpTransport()
    val JSON_FACTORY = new JacksonFactory()
    val applicationName = scala.util.Properties.envOrElse("GOOGLE_APPLICATION_NAME", "no name")
    def getCalendarService(): com.google.api.services.calendar.Calendar = {
      val credential = new Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(user.googleToken);
      return new com.google.api.services.calendar.Calendar.Builder(
        HTTP_TRANSPORT, JSON_FACTORY, credential)
        .setApplicationName(applicationName)
        .build();
    }

    val service =
      getCalendarService();
    Try{
      var event = new Event()
        .setSummary(newEvent.name)
        .setLocation(newEvent.place.getOrElse(stringPlace(Some(""),"")).name)
        .setDescription(newEvent.description.getOrElse(""))
      println("google date")
      val (sdate, sdate2) = newEvent.start_time.get.splitAt(newEvent.start_time.get.size-2)
      println(sdate+":"+sdate2)
      val startDateTime = new DateTime(sdate+":"+sdate2)
      val start = new EventDateTime()
        .setDateTime(startDateTime)
      //.setTimeZone("America/Los_Angeles")
      event.setStart(start)
      val (edate, edate2) = newEvent.end_time.get.splitAt(newEvent.end_time.get.size-2)
      val endDateTime = new DateTime(edate+":"+edate2)
      val end = new EventDateTime()
        .setDateTime(endDateTime)
      event.setEnd(end)
      val calendarId = "primary"
      event = service.events().insert(calendarId, event).execute()
      println("google add")
      println(event.toString)
      System.out.printf("Event created: %s\n", event.getHtmlLink())
    } match {
      case Success(_) =>
      case Failure(error) =>
        println(error.toString)
    }

  }

}