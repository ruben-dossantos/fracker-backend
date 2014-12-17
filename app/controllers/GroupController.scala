package controllers

import models.Group
import models.GroupsTable._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.{DBAction, _}
import play.api.libs.json.Json
import play.api.mvc._

/**
 * Created by ruben on 17-12-2014.
 *
 */
object GroupController extends Controller{

  def all = DBAction { implicit rs =>
    Ok(Json.toJson(groups.list))
  }

  def create = DBAction(parse.json){ implicit rs =>
    rs.request.body.validate[Group].map { group =>
      try {
        groups.insert(group)
        Ok(Json.toJson(group))
      } catch {
        case e: Exception => Ok(e.getMessage)
      }
    }.getOrElse(BadRequest("invalid json"))
  }

}
