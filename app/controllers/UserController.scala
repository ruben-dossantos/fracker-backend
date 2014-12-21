package controllers

import models.{UsersTable, User}
import models.UsersTable._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.{DBAction, _}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import play.api.db.slick.Session

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

//  def findUser(user: User)(implicit s: Session) = {
//
//    val this_user = users.filter(u => u.username === user.username).take(1).run
//    this_user(0)
//  }

//  def findUserById(id: Long)(implicit s: Session) = {
//
//    val this_user = users.filter(u => u.id === id).take(1).run
//    if( this_user.size > 0){
//      Some(this_user(0))
//    } else { None }
//  }
}
