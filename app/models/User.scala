package models

/**
 * Created by ruben on 17-12-2014.
 *
 */
import play.api.db.slick.Config.driver.simple._
import play.api.libs.json.Json

case class User(id: Option[Long], username: String, first_name: String, last_name: String, password: Option[String], lat: Option[String], lon: Option[String], timestamp: Option[Long])

class UsersTable(tag: Tag) extends Table[User](tag, "users"){
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def username = column[String]("username")
  def first_name = column[String]("first_name")
  def last_name = column[String]("last_name")
  def password = column[String]("password", O.Nullable)
  def lat = column[String]("lat", O.Nullable)
  def lon = column[String]("lon", O.Nullable)
  def timestamp = column[Long]("timestamp", O.Nullable)

  def uniqueUsername = index("username", username, unique= true)

  override def * = (id.?, username, first_name, last_name, password.?, lat.?, lon.?, timestamp.?) <> (User.tupled, User.unapply)
}

object UsersTable {
  val users = TableQuery[UsersTable]
  implicit val catFormat  = Json.format[User]
}