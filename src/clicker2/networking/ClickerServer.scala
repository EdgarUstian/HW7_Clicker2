package clicker2.networking

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import play.api.libs.json.Json

case object UpdateGames

case object AutoSave

case class GameState(gameState: String)

class ClickerServer extends Actor {

  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 8000))

  var temporaryActor: ActorRef = _
  var users: Map[String, ActorRef] = Map()
  var clients: Set[ActorRef] = Set()

  override def receive: Receive = {
    case b: Bound => println("Listening on port: " + b.localAddress.getPort)
    case c: Connected =>
      println("Client Connected: " + c.remoteAddress)
      this.clients = this.clients + sender()
      temporaryActor = sender()
      temporaryActor ! Register(self)
    case PeerClosed =>
      println("Client Disconnected: " + sender())
      this.clients = this.clients - sender()
    case r: Received =>
      val user = (Json.parse(r.data.utf8String) \ "username").as[String]
      val action = (Json.parse(r.data.utf8String) \ "action").as[String]
      if (action == "connected") {
        sender() ! Register(self)
        val actor = context.actorOf(Props(classOf[GameActor], user))
        users = users + (user -> actor)
        actor ! Setup
      }
      if (action == "disconnected"){
        users(user) ! PoisonPill
        users = users - user
      }
      if (action == "clickGold") {
        users(user) ! ClickGold
      }
      if (action == "buyEquipment"){
        users(user) ! BuyEquipment((Json.parse(r.data.utf8String) \ "equipmentID").as[String])
      }


    case UpdateGames =>
      for ((user, actor) <- users){
        actor ! Update
      }
    case AutoSave =>
      for ((user, actor) <- users){
        actor ! Save
      }
    case gs: GameState =>
      val delimiter = "~"
      this.temporaryActor ! Write(ByteString(gs.gameState + delimiter))
  }

}


object ClickerServer {

  def main(args: Array[String]): Unit = {
    val actorSystem = ActorSystem()

    import actorSystem.dispatcher

    import scala.concurrent.duration._

    val server = actorSystem.actorOf(Props(classOf[ClickerServer]))

    actorSystem.scheduler.schedule(0 milliseconds, 100 milliseconds, server, UpdateGames)
    actorSystem.scheduler.schedule(0 milliseconds, 5000 milliseconds, server, AutoSave)
  }
}
