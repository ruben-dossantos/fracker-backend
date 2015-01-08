package models

/**
 * Created by ruben on 17-12-2014.
 *
 */
import play.api.db.slick.Config.driver.simple._
import play.api.libs.json._


case class User(id: Option[Long], username: String, first_name: Option[String], last_name: Option[String], password: Option[String], lat: Option[String], lon: Option[String], timestamp: Option[Long], preferenceDistance: Option[Double])

object User {
  implicit val userWrites = new Writes[User] {
    def writes(u: User): JsValue = {
      Json.obj(
        "id" -> u.id,
        "username" -> u.username,
        "first_name" -> u.first_name,
        "last_name" -> u.last_name,
        "lat" -> u.lat,
        "lon" -> u.lon,
        "preferenceDistance" -> u.preferenceDistance
      )
    }
  }
}

class UsersTable(tag: Tag) extends Table[User](tag, "users"){
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def username = column[String]("username")
  def first_name = column[String]("first_name")
  def last_name = column[String]("last_name")
  def password = column[String]("password", O.Nullable)
  def lat = column[String]("lat", O.Nullable)
  def lon = column[String]("lon", O.Nullable)
  def timestamp = column[Long]("timestamp", O.Nullable)
  def preferenceDistance = column[Double]("preference_distance", O.Nullable)

  def uniqueUsername = index("username", username, unique= true)

  override def * = (id.?, username, first_name.?, last_name.?, password.?, lat.?, lon.?, timestamp.?, preferenceDistance.?) <> ((User.apply _).tupled, User.unapply)
}

object UsersTable {
  val users = TableQuery[UsersTable]
  implicit val catFormat  = Json.format[User]

  def findUserById(id: Long) = {
    users.filter(u => u.id === id)
  }

  def findUserByUsername(username: String) = {
    users.filter(u => u.username === username)
  }

  def findLogin(username: String, password: Option[String])= {
    users.filter(u => u.username === username && u.password === password)
  }

}