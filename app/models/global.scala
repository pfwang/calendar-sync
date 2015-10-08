package models

case class User (id: Int, fbToken: String, googleToken:String)
case class stringPlace (id:Option[String], name: String)
case class Place (id:Long, name: String)
case class Event (id: Long, rsvp:String, description:String, name:String, start_time: String, end_time: String, place: Place)
case class stringEvent (id: String, rsvp_status:String, description:Option[String], name:String, start_time: Option[String], end_time: Option[String], place: Option[stringPlace])
class modelMain {

}
