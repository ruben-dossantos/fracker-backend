package controllers

import models.UsersTable._
import models.{UsersGroupsTable, User, UsersTable}
import org.joda.time.DateTime
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.{DBAction, _}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

/**
 * Created by ruben on 17-12-2014.
 *
 */
object UserController extends Controller{

  def all = DBAction { implicit rs =>
    Ok(Json.toJson(users.list))
  }

  def signup = DBAction(parse.json){ implicit rs =>
    rs.request.body.validate[User].map { user =>
      try {
        users.insert(user)
        Ok(Json.toJson(user))
      } catch {
        case e: Exception => Ok(e.getMessage)
      }
    }.getOrElse(BadRequest("invalid json"))
  }

//  def delete = DBAction(parse.json){ implicit rs =>
//    rs.request.body.validate[User].map { user =>
//      try {
//        users.delete(user)
//        Ok(Json.toJson(user))
//      } catch {
//        case e: Exception => Ok(e.getMessage)
//      }
//    }.getOrElse(BadRequest("invalid json"))
//  }

  def login = DBAction(parse.json){ implicit rs =>
    rs.request.body.validate[User].map { user =>
      try {
        val login = UsersTable.findLogin(user.username, user.password).take(1).run
        Ok(Json.toJson(login(0)))
      } catch {
        case e: Exception => Ok(e.getMessage)
      }
    }.getOrElse(BadRequest("invalid json"))
  }


  def getUserByJson(json: JsValue) = {
    var user :Option[User] = None
    json.validate[User].map { u =>
      user = Some(u)
    }
    user
  }


  def updatePosition(id: Long) = DBAction(parse.json){ implicit rs =>
    rs.request.body.validate[User].map { user_json =>
      try {
        val user = UsersTable.findUserById(id).run
        val updated_user = User(user(0).id, user(0).username, user(0).first_name, user(0).last_name, user(0).password, user_json.lat, user_json.lon, Some(new DateTime().getMillis))
        UsersTable.findUserById(id).update(updated_user)

        val list = List()

        val groups = UsersGroupsTable.findUserGroups(id).run
        groups map { group =>
          //println("Group => " + group)
          val friends = UsersGroupsTable.findGroupUsers(group.group.get).run
          friends map { friend =>
            val found_friend = UsersTable.findUserById(friend.user.get).run
            if(found_friend(0).id.get != id && isPositionStillValid(updated_user.timestamp.get, found_friend(0).timestamp.get) && distanceBetweenCoordinates(updated_user.lat.get, updated_user.lon.get, found_friend(0).lat.get, found_friend(0).lon.get) < 15.0){

              println(found_friend(0))
              println(distanceBetweenCoordinates(updated_user.lat.get, updated_user.lon.get, found_friend(0).lat.get, found_friend(0).lon.get) + "Km")
            }
          }
        }

        Ok(Json.toJson(updated_user))
      } catch {
        case e: Exception => Ok(e.getMessage)
      }
    }.getOrElse(BadRequest("invalid json"))
  }


  def isPositionStillValid(myTimestamp: Long, friendTimestamp: Long) :Boolean = {
    if (myTimestamp - friendTimestamp < 1800000) true
    else false
  }


  def distanceBetweenCoordinates(myLat: String, myLon: String, friendLat: String, friendLon: String) = {
    val theta = friendLat.toFloat - myLat.toFloat
    var dist = Math.sin(deg2rad(myLat.toDouble)) * Math.sin(deg2rad(friendLat.toDouble)) + Math.cos(deg2rad(myLat.toDouble)) * Math.cos(deg2rad(friendLat.toDouble)) * Math.cos(deg2rad(theta))
    dist = Math.acos(dist)
    dist = rad2deg(dist)
    dist = dist * 60 * 1.1515
    dist = dist * 1.609344
    dist

  }

  def deg2rad(deg: Double) = deg * Math.PI / 180.0

  def rad2deg(rad: Double) = rad * 180.0 / Math.PI


}
