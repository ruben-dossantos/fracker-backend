package controllers

import models.User
import models.UsersTable._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.{DBAction, _}
import play.api.libs.json.Json
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

  def login = TODO

}
