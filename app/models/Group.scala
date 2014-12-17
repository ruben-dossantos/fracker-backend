package models

/**
 * Created by ruben on 17-12-2014.
 *
 */
import play.api.db.slick.Config.driver.simple._
import play.api.libs.json.Json

case class Group(id: Option[Long], name: String, password: String)

class GroupsTable(tag: Tag) extends Table[Group](tag, "groups"){
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def password = column[String]("password")

  def uniqueName = index("name", name, unique = true)

  override def * = (id.?, name, password) <> (Group.tupled, Group.unapply)
}

object GroupsTable {
  val groups = TableQuery[GroupsTable]
  implicit val catFormat  = Json.format[Group]
}