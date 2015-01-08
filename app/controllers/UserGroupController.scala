package controllers

import models._
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

  def abandonGroup(id_user: Long, id_group: Long) = DBAction{ implicit rs =>

      try {

        users_groups.filter(ug => ug.userID === id_user && ug.groupID === id_group).delete
        Status(202)("Successfully deleted")
      } catch {
        case e: Exception => Ok(e.getMessage)
      }

  }

  def getUserGroups(id: Long) = DBAction{ implicit rs =>
    val user = UsersTable.findUserById(id).run
    if( user.size > 0) {
      val userGroups = users_groups.filter(ug => ug.userID === user(0).id.get).run
      var groups: List[Group] = List[Group]()
      userGroups map { ug =>
        val group = GroupsTable.findGroupById(ug.group.get).run
        groups = group(0) :: groups
      }
      val ugs = UserGroups(user(0).username, groups)
      Ok(Json.toJson(ugs))
    } else {
      Status(404)("User not found")
    }
  }


  def getFriends(userId: Long, groupId: Long) = DBAction{ implicit rs =>
    val user = UsersTable.findUserById(userId).run
    val group = GroupsTable.findGroupById(groupId).run
    if( user.size > 0 && group.size > 0) {
      val userGroups = users_groups.filter(ug => ug.groupID === group(0).id.get).run

      var users: List[User] = List[User]()
      userGroups map { ug =>
        val user = UsersTable.findUserById(ug.user.get).run
        users = user(0) :: users
      }
      val gus = GroupUsers(group(0).name, users)
      Ok(Json.toJson(gus))
    } else {
      Status(404)("User or group not found")
    }
  }

}
