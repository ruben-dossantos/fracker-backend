package models

/**
 * Created by ruben on 17-12-2014.
 *
 */
import play.api.db.slick.Config.driver.simple._
import play.api.libs.json.{JsValue, Writes, Json}
import models.UsersTable._
import models.GroupsTable._

case class UserGroup(user: Option[Long], group: Option[Long])

case class UserGroups(username: String, groups: List[Group])

object UserGroups {
  implicit val userGroupsWrites = new Writes[UserGroups] {
    def writes(ugs: UserGroups): JsValue = {
      Json.obj(
        "username" -> ugs.username,
        "groups" -> ugs.groups
      )
    }
  }
}

case class GroupUsers(name: String, users: List[User])

object GroupUsers {
  implicit val groupUsersWrites = new Writes[GroupUsers] {
    def writes(gus: GroupUsers): JsValue = {
      Json.obj(
        "name" -> gus.name,
        "users" -> gus.users
      )
    }
  }
}

class UsersGroupsTable(tag: Tag) extends Table[UserGroup](tag, "users_groups"){
  def userID = column[Long]("user")
  def groupID = column[Long]("group")

  def uniqueUserGroup = index ("user_group", (userID, groupID), unique = true)
  def user = foreignKey("user_FK", userID, users)(_.id)
  def group = foreignKey("group_FK", groupID, groups)(_.id)

  override def * = (userID.?, groupID.?) <> (UserGroup.tupled, UserGroup.unapply)
}

object UsersGroupsTable {
  val users_groups = TableQuery[UsersGroupsTable]
  implicit val catFormat  = Json.format[UserGroup]

  val implicitCrossJoin = for {
    u <- users
    g <- groups
  } yield(u.username, g.name)


  def findUserGroups(id: Long) = {
    users_groups.filter(ug => ug.userID === id)
  }

  def findGroupUsers(id: Long) = {
    users_groups.filter(ug => ug.groupID === id)
  }

  def ownerAutoJoin(user: Option[Long], group: Option[Long])(implicit s: Session) = {
    users_groups.insert(UserGroup(user, group))
  }
}