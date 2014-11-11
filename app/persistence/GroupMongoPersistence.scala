package persistence

import akka.actor.{ActorLogging, Props}
import models.Group
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import reactivemongo.bson.BSONDocument

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._

/**
 * Created by ruben on 11-11-2014.
 *
 */
object GroupMongoPersistence {
  def props(db_name: String = "fracker", collection_name: String = "groups"): Props = {
    Props(classOf[GroupMongoPersistence], db_name, collection_name)
  }
}

class GroupMongoPersistence (db_name: String, collection_name: String) extends GroupPersistence with ActorLogging{

  var driver: MongoDriver = null
  var connection: MongoConnection = null
  var db: DefaultDB = null
  var groups: BSONCollection = null

  def withMongoConnection[T](body: => T): Try[T] = {
    Try{
      if(groups == null){
        driver = new MongoDriver(context.system)
        connection = driver.connection(Seq("localhost"))
        db = connection.db(db_name)
        groups = db.collection[BSONCollection](collection_name)
        groups.indexesManager.ensure(Index(List(("name", Ascending)), unique = true))
      }

      body
    }
  }

  override def createGroup(group: Group): Option[Int] = ???

  override def readGroup(id: Int): Option[Group] = {
    withMongoConnection {
      val query = BSONDocument("id" -> id)
      Await.result(groups.find(query).one[Group], 5.seconds)
    } match {
      case Success(user) => user
      case Failure(_) => None
    }
  }

  override def deleteGroup(id: Int): Boolean = {
    withMongoConnection {
      groups.remove(BSONDocument("id" -> id))
    } match {
      case Success(_) => true
      case Failure(_) => false
    }
  }

  override def updateGroup(group: Group): Boolean = ???
}
