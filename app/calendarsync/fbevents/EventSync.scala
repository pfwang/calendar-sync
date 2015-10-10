package calendarsync.fbevents

import calendarsync.database.DBActions
import calendarsync.facebook.FbApi
import calendarsync.google.GoogleApi
import models.{Event, User}

import scala.util.{Failure, Success, Try}

object EventSync {
  def syncEvents(): Try[Unit] = {
    Try {
      println("syncEvents")
      val listUsers: Try[List[User]] = DBActions.selectAllUsers()
      listUsers match {
        case Success(users) =>
          val accessTokenUser: List[Option[User]] = users.map(user =>
            GoogleApi.getToken(user.googleToken) match {
              case Success(maybeToken) =>
                maybeToken match {
                  case Some(token) =>
                    println("getGoogleToken")
                    Some(User(user.id, user.fbToken, token))
                  case None =>
                    println("No google access token recieved")
                    None
                }
              case Failure(error) =>
                println(error.toString)
                None
            })
          accessTokenUser.foreach { user =>
            user.foreach { user =>
              val userDbEvents: Set[Long] = DBActions.selectAllEvents(user.id) match {
                case Success(listDbEvents) =>
                  listDbEvents.toSet[Long]
                case Failure(error) =>
                  throw error
              }
              val userFbEvents: Try[List[Event]] = FbApi.getEvents(user.fbToken)
              val newEvents = userFbEvents match {
                case Success(fbEvents) =>
                  fbEvents.filter(event =>
                    (userDbEvents.count(dbEventId =>
                      dbEventId == event.id) == 0) && event.start_time.isDefined && event.end_time.isDefined)
                case Failure(error) =>
                  throw error
              }
              DBActions.addEvents(user.id, newEvents) match {
                case Success(_) =>
                  println("before google")
                  //throw new Exception("reach2")
                  newEvents.foreach(event =>
                  GoogleApi.createEvent(user, event))
                  println("after google")
                case Failure(error)=>
                  println(error.toString)
              }
            }
          }
      }

    }

  }

  def getNewEvents: Try[List[Event]] = {

    Success(List.empty[Event])
  }
}
