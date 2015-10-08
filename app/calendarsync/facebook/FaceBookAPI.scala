package calendarsync.facebook

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import play.api.libs.json.Reads
import play.api.libs.functional.syntax._
import play.api.libs.json._
import scala.util.{Failure, Success, Try}
import models.{stringPlace, Place, stringEvent}


object FbApi {

  implicit val stringPlaceReads=(
    (__ \ "id").readNullable[String] and
      (__ \ "name").read[String]

    )(stringPlace.apply _)

  implicit val eventReads= (
      (__ \ "id").read[String] and
      (__ \ "rsvp_status").read[String] and
      (__ \ "description").readNullable[String] and
      (__ \ "name").read[String] and
      (__ \ "start_time").readNullable[String] and
      (__ \ "end_time").readNullable[String] and
      (__ \ "place").readNullable[stringPlace]
  )(stringEvent.apply _)

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

  def getEvents(token: String): Try[List[stringEvent]] = {
    val timestamp: Long = System.currentTimeMillis / 1000
    val url = s"https://graph.facebook.com//v2.4/me/events?access_token=$token"
    val client = HttpClientBuilder.create().build()
    val get = new HttpGet(url)

    val response = client.execute(get)
    val responseStr = EntityUtils.toString(response.getEntity)
    println("hi")
    val json = Json.parse(responseStr)
    println(Json.prettyPrint(json))
    val listEventsJS = (json \ "data").as[JsValue].validate[List[stringEvent]]
    println("hi3")
    println(Json.stringify(json))
    val listEvents: Try[List[stringEvent]] = listEventsJS match {
      case JsSuccess(list: List[stringEvent], _) => Success(list)
      case JsError(error) =>
        println(error.toString())
        Failure(throw new Exception(error.toString()))
    }
    get.releaseConnection()
    listEvents
  }
}
