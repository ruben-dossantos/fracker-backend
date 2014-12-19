package models

/**
 * Created by ruben on 17-12-2014.
 *
 */
import play.api.db.slick.Config.driver.simple._
import play.api.libs.json.Json
import models.UsersTable._
import models.GroupsTable._

case class UserGroup(user: Option[Long], group: Option[Long])

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
}