package models

/**
 * Created by ruben on 17-12-2014.
 *
 */
import play.api.db.slick.Config.driver.simple._
import play.api.libs.json.Json

case class UserGroup(user: Option[Long], group: Option[Long])

class UsersGroupsTable(tag: Tag) extends Table[UserGroup](tag, "users_groups"){
  def user = column[Long]("user")
  def group = column[Long]("group")

  def uniqueUserGroup = index ("user_group", (user, group), unique = true)

  override def * = (user.?, group.?) <> (UserGroup.tupled, UserGroup.unapply)
}

object UsersGroupsTable {
  val users_groups = TableQuery[UsersGroupsTable]
  implicit val catFormat  = Json.format[UserGroup]
}