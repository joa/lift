package net.liftweb.mapper

/*                                                *\
 (c) 2006-2007 WorldWide Conferencing, LLC
 Distributed under an Apache License
 http://www.apache.org/licenses/LICENSE-2.0
 \*                                                 */

import java.sql.{Connection, ResultSet, Statement, PreparedStatement}
import javax.sql.{ DataSource}
import javax.naming.{Context, InitialContext}
import scala.collection.mutable._
import net.liftweb.util._
import net.liftweb.util.Lazy._

object DB {
  private val threadStore = new ThreadLocal
  private val envContext = Lazy(() => (new InitialContext).lookup("java:/comp/env").asInstanceOf[Context])
  
  /**
    * can we get a JDBC connection from JNDI?
    */
  def jndiJdbcConnAvailable_? : boolean = {
    val touchedEnv = envContext.calculated_?
    
    val ret = try {
      (envContext.lookup(DefaultConnectionIdentifier.jndiName).asInstanceOf[DataSource].getConnection) != null
    } catch {
      case e => false
    }
        
    if (!touchedEnv) envContext.reset
    ret
  }
  
  // var connectionManager: Option[ConnectionManager] = None
  private val connectionManagers = new HashMap[ConnectionIdentifier, ConnectionManager];
  
  def defineConnectionManager(name: ConnectionIdentifier, mgr: ConnectionManager) {
    connectionManagers(name) = mgr
  }
  
  private def info : HashMap[ConnectionIdentifier, (SuperConnection, int)] = {
    threadStore.get.asInstanceOf[HashMap[ConnectionIdentifier, (SuperConnection, int)]] match {
      case null =>
	val tinfo = new HashMap[ConnectionIdentifier, (SuperConnection, int)];
	threadStore.set(tinfo)
	tinfo

      case v => v
    }
  }
  

  private def newConnection(name : ConnectionIdentifier) : SuperConnection = {
    val ret = new SuperConnection(connectionManagers.get(name).flatMap(_.newConnection(name)).getOrElse {envContext.lookup(name.jndiName).asInstanceOf[DataSource].getConnection})
    ret.setAutoCommit(false)
    ret
  }
  
  
  private def releaseConnection(conn : SuperConnection) : unit = conn.close
  
  private def getConnection(name : ConnectionIdentifier): SuperConnection =  {
    var ret = info.get(name) match {
      case None => (newConnection(name), 1)
      case Some(c) => (c._1, c._2 + 1)
    }
    info(name) = ret
    ret._1
  }
  
  def releaseConnectionNamed(name : ConnectionIdentifier) {
    info.get(name) match {
      case Some(c)  => if (c._2 == 1) c._1.commit
      info(name) = (c._1, c._2 - 1)
      
      case _ =>
    }
  }
  
  def statement[T](db : SuperConnection)(f : (Statement) => T) : T =  {
    val st = db.createStatement
    try {
      f(st)
    } finally {
      st.close
    }
  }
  
  def exec[T](db : SuperConnection, query : String)(f : (ResultSet) => T) : T = {
    statement(db) {st => 
      f(st.executeQuery(query))
      }
  }
  
  def exec[T](statement : PreparedStatement)(f : (ResultSet) => T) : T = {
    val rs = statement.executeQuery
    try {
      f(rs)
    } finally {
      rs.close
    }
  }
  
  def prepareStatement[T](statement : String, conn: SuperConnection)(f : (PreparedStatement) => T) : T = {
    val st = conn.prepareStatement(statement)
      try {
	f(st)
      } finally {
        st.close
      }
  }
  
  def prepareStatement[T](statement : String,keys: int, conn: SuperConnection)(f : (PreparedStatement) => T) : T = {
        val st = conn.prepareStatement(statement, keys)
      try {
        f(st)
      } finally {
        st.close
      }
  }
  
  /*
  def use[T](f : (SuperConnection) => T) : T = {
    this.use("")(f)
  }
  */
    
  def use[T](name : ConnectionIdentifier)(f : (SuperConnection) => T) : T = {
    val conn = getConnection(name)
    try {
      f(conn)
    } finally {
      releaseConnectionNamed(name)
    }
  }
}

class SuperConnection(val connection: Connection) {
  val brokenLimit_? : Lazy[boolean] = Lazy( () => connection.getMetaData.getDatabaseProductName ==  "Apache Derby")
}

object SuperConnection {
  implicit def superToConn(in: SuperConnection): Connection = in.connection
}

trait ConnectionIdentifier {
  def jndiName: String
}
case object DefaultConnectionIdentifier extends ConnectionIdentifier {
  var jndiName = "lift"
}
