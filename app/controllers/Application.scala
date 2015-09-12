package controllers

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
import play.api.i18n.Messages.Implicits._

object Application extends Controller{

  def index = Action {
    val signUpForm = Form(
      tuple(
        "username" -> text,
        "password" -> text
      )
    )
    Ok(views.html.homepage(signUpForm))
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
