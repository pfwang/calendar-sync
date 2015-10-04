package calendarsync.database

import java.sql.ResultSet

import play.api.Play.current
import play.api.db.DB

import scala.util.Try


object DBActions {
  def createNewUser(fbToken: String, googleToken: String):  Try[Unit] ={

      val conn = DB.getConnection()
      try {
        val stmt = conn.createStatement
        Try(stmt.executeUpdate(s"INSERT INTO users (fb_token,  google_refresh_token) VALUES ('$fbToken', '$googleToken')"))
      } finally {
        conn.close()
      }

  }
  def selectAllUsers(): Try[ResultSet] ={
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement
      Try(stmt.executeQuery(s"SELECT user_id FROM users"))
    } finally {
      conn.close()
    }
  }
  def addInitialEvents(): Try[Unit] ={

  }
}
