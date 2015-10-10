package calendarsync.database

import java.sql.ResultSet
import calendarsync.google.GoogleApi._


import models.{Event, stringEvent, User}
import play.api.Play.current
import play.api.db.DB

import scala.util.{Success, Failure, Try}


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
  def selectAllUsers(): Try[List[User]] ={
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement
      Try(stmt.executeQuery(s"SELECT user_id, fb_token, google_refresh_token FROM users")) match {
        case Success(results) =>
          var resultList = List[User]()
           Try {
              while (results.next()){
                getToken(results.getString(3)) match{
                  case Success(maybeToken) =>
                    maybeToken match {
                      case Some(token) =>
                        resultList = resultList :+  User(results.getInt(1), results.getString(2), token)
                    }
                }
              }

            resultList
           }

        case Failure(error) => Failure(error)
      }
    } finally {
      conn.close()
    }
  }

  def selectAllEvents(user_id: Long): Try[List[Long]] = {
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement
      Try(stmt.executeQuery(s"SELECT event_id FROM user_events WHERE user_id = '$user_id'")) match {
        case Success(results) =>
          var resultList = List[Long]()
          Try {
            while (results.next()){
              resultList = resultList :+  results.getLong(1)
            }

            resultList
          }

        case Failure(error) => Failure(error)
      }
    } finally {
      conn.close()
    }
  }

  def addEvents(user_id: Long, events:List[Event]): Try[Unit] = {
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement
      Try{
        events.map(event =>
          stmt.executeUpdate(s"INSERT INTO events VALUES (${event.id}, '${event.start_time.get}', '${event.end_time.get}')")
        )
        events.map(event =>
          stmt.executeUpdate(s"INSERT INTO user_events VALUES ($user_id, ${event.id} )")
        )
      }
    } finally {
      conn.close()
    }

  }

  def addInitialEvents(): Try[Unit] ={
    Try()
  }
}
