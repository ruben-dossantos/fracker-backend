package controllers

import models.UserGroup
import models.UsersGroupsTable._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.{DBAction, _}
import play.api.libs.json.Json
import play.api.mvc._

/**
 * Created by ruben on 17-12-2014.
 *
 */
object UserGroupController extends Controller{

  def joinGroup(id: Long) = DBAction(parse.json){ implicit rs =>
    rs.request.body.validate[UserGroup].map { user_group =>
      try {
        users_groups.insert(user_group)
        Ok(Json.toJson(user_group))
      } catch {
        case e: Exception => Ok(e.getMessage)
      }
    }.getOrElse(BadRequest("invalid json"))
  }

  def abandonGroup(id_user: Long, id_group: Long) = DBAction(parse.json){ implicit rs =>
    rs.request.body.validate[UserGroup].map { user_group =>
      try {

        //users_groups.flatMap(ug => ug.user.filter( u=> u.id === user_group.user.get) && ug.group.filter( g => g.id === user_group.group.get)).delete
        Ok(Json.toJson(user_group))
      } catch {
        case e: Exception => Ok(e.getMessage)
      }
    }.getOrElse(BadRequest("invalid json"))
  }

  def getUserGroups(id: Long) = DBAction{ implicit rs =>

    UserController.findUserById(id) match {
      case Some(user) =>
//        val userGroups = users_groups.filter(ug => ug.user === user.id.get).run

//        Ok(Json.toJson(userGroups))
        Ok("TODO")
      case None => BadRequest("invalid json")
    }
//    rs.request.body.validate[UserGroup].map { user_group =>
//      try {
//
//        users_groups.filter(u => u.user === user_group.user.get && u.group === user_group.group.get).delete
//        Ok(Json.toJson(user_group))
//      } catch {
//        case e: Exception => Ok(e.getMessage)
//      }
//    }.getOrElse(BadRequest("invalid json"))
  }


}
