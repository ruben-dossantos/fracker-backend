package models

/**
 * Created by ruben on 17-12-2014.
 *
 */
import play.api.db.slick.Config.driver.simple._
import play.api.libs.json.{JsValue, Writes, Json}
import models.UsersTable._

case class Group(id: Option[Long], name: String, password: String, owner: Option[Long])

class GroupsTable(tag: Tag) extends Table[Group](tag, "groups"){
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def password = column[String]("password")
  def owner = column[Long]("owner", O.Default(1))

  def user = foreignKey("owner_FK", owner, users)(_.id)
  def uniqueName = index("name", name, unique = true)

  override def * = (id.?, name, password, owner.?) <> ((Group.apply _).tupled, Group.unapply)
}

object Group {
  implicit val groupWrites = new Writes[Group] {
    def writes(g: Group): JsValue = {
      Json.obj(
        "id" -> g.id,
        "name" -> g.name
      )
    }
  }
}

object GroupsTable {
  val groups = TableQuery[GroupsTable]
  implicit val catFormat  = Json.format[Group]

  def findGroupById(id: Long) = {
    groups.filter(g => g.id === id)
  }

  def findGroupByName(name: String) = {
    groups.filter(g => g.name like("%" + name + "%"))
  }

  def test(list: List[Long]) = {
    try{    
        var first = true
        var group: Query[GroupsTable,GroupsTable#TableElementType,Seq] = groups.filter(g => g.id =!= list(0))
        list map { id =>
            if(first){
                first = false
            } else {
                group = group.filter(g => g.id =!= id)
            }
        }
        group
    } catch {
	case e:Exception => groups.filter(g => g.id =!= 0L)
    }
 }
}
