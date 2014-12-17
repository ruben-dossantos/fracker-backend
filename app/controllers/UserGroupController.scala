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

//        users_groups.delete(id_user, id_group)
        Ok(Json.toJson(user_group))
      } catch {
        case e: Exception => Ok(e.getMessage)
      }
    }.getOrElse(BadRequest("invalid json"))
  }


}
