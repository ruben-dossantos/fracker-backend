package controllers

import models.GroupsTable._
import models.{UsersGroupsTable, Group, GroupsTable}
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
    rs.request.getQueryString("name") match {
      case Some(name) =>
        val found = GroupsTable.findGroupByName(name).run
        Ok( Json.toJson(found))
      case None => Ok(Json.toJson(groups.list))
    }
  }

  def create = DBAction(parse.json){ implicit rs =>
    rs.request.body.validate[Group].map { group =>
      try {
        val groupId = (groups returning groups.map(_.id)) += group
//        groups.insert(group)
        UsersGroupsTable.ownerAutoJoin(group.owner, Some(groupId)).run
        Ok(Json.toJson(group))
      } catch {
        case e: Exception => Ok(e.getMessage)
      }
    }.getOrElse(BadRequest("invalid json"))
  }

}
