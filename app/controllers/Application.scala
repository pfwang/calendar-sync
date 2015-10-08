
package controllers

import calendarsync.database.DBActions
import calendarsync.facebook.FbApi
import calendarsync.fbevents.EventSync._
import models.User
import play.api._
import play.api.i18n.{Messages, I18nSupport}
import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current
import play.api.data._
import play.api.data.Forms._
import play.api.db._
import play.api.i18n.I18nSupport
import play.i18n.MessagesApi
import play.api.Play.current
import  play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.i18n.Messages.Implicits._
import FbApi._


import scala.util.{Success, Failure}

object Application extends Controller{

  def googleLogin = Action {
    val clientId = scala.util.Properties.envOrElse("GOOGLE_CLIENT_ID", "no client id")
    val redirectUri = scala.util.Properties.envOrElse("GOOGLE_REDIRECT_URI", "no redirect uri")
    val scope = scala.util.Properties.envOrElse("GOOGLE_SCOPE", "no google scope")
    val accessType = "offline"
    val approvalPrompt = "force"
    val baseUrl = "https://accounts.google.com/o/oauth2/auth"
    Redirect(s"$baseUrl?scope=$scope&redirect_uri=$redirectUri&response_type=code&client_id=$clientId&approval_prompt=$approvalPrompt&access_type=$accessType");
  }

  def googleLoginResult(error:Option[String], code: Option[String]) = Action {
    error match {
      case Some(errorMessage) => Unauthorized(s"Login to google failed with error message: $errorMessage")
      case None =>
          code match {
            case Some(authCode) => Redirect(routes.Application.googleTokenExchange(authCode))
            case None => NotFound("Google auth code not found")
          }
    }
  }

  def googleTokenExchange(authCode: String) = Action {
    Redirect(routes.GoogleTokenExchange.exchange(authCode))
  }

  def createNewAccount() = Action { implicit result =>
    val tokenForm = Form(
      tuple(
        "userId" -> text,
        "fbToken" -> text,
        "refreshToken" -> text
      )
    )
    tokenForm.bindFromRequest.fold(
      failure => Ok(failure.errorsAsJson.toString()),
      {
      case (userId, fbToken, refreshToken) =>
        getLongTermToken(fbToken) match {
          case Success(token) =>
            val longTermFbToken = token.split("=")(1)

            DBActions.createNewUser(longTermFbToken, refreshToken) match {
              case Success(_) =>

                Ok("new sync created")
              case Failure(error) =>
                BadRequest(s"sync creation failed: $error")
            }
          case Failure(error) =>
            BadRequest(s"sync creation failed: $error")
        }
      }
    )
  }

  def facebookLogin(refreshToken: Option[String]) = Action {
    Ok(views.html.homepage(refreshToken.get))
  }

  def calendarEventsSync() = Action {
    syncEvents()
    DBActions.selectAllUsers() match {
      case Success(list:List[User]) =>
        FbApi.getEvents(list.head.fbToken) match {
          case Success(events)=>
            Ok(events.toString)
          case Failure(error)=>
            Ok(error.toString)
        }
        //Ok("hi")
      case Failure(error) =>
        Ok(error.toString)
    }


    //Ok("done")
  }

  def index = Action {
    val signUpForm = Form(
      tuple(
        "username" -> text,
        "password" -> text
      )
    )
    Ok("hi")
  }

  def submit = Action {
    Ok("good")
  }

  def db = Action {
    var out = ""
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement

      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)")
      stmt.executeUpdate("INSERT INTO ticks VALUES (now())")

      val rs = stmt.executeQuery("SELECT tick FROM ticks")

      while (rs.next) {
        out += "Read from DB: " + rs.getTimestamp("tick") + "\n"
      }
    } finally {
      conn.close()
    }

    Ok(out)
  }
}
