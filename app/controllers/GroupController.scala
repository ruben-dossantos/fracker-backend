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
        rs.request.getQueryString("user") match {
          case Some(userID) =>
            val myGroups = UsersGroupsTable.findUserGroups(userID.toLong).run
            var list = List[Long]()
            myGroups map { myGroup =>
              list = myGroup.group.get :: list
            }
            var found: Query[GroupsTable,GroupsTable#TableElementType,Seq] = GroupsTable.test(list)
            found = found.filter(g => g.name like("%" + name + "%"))
            Ok( Json.toJson(found.run))
          case None =>
            val found = GroupsTable.findGroupByName(name).run
            Ok( Json.toJson(found))
        }
      case None =>
        Ok(Json.toJson(groups.list))
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

//  def test()= DBAction { implicit rs =>
//    val list = List(1L, 2L, 3L)
//    var first = true
//    var group: Query[GroupsTable,GroupsTable#TableElementType,Seq] = groups.filter(g => g.id =!= list(0))
//    list map { id =>
//      if(first){
//        first = false
//      } else {
//        group = group.filter(g => g.id =!= id)
//      }
//    }
//    val found_groups = group.run
//    Ok(Json.toJson(found_groups))
//  }

}
