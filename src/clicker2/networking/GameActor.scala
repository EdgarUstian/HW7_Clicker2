package clicker2.networking

import akka.actor.Actor
import akka.io.Tcp.Register
import clicker2.Game

case object Update

case object ClickGold

case object Save

case object Setup

case class BuyEquipment(equipmentID: String)

class GameActor(username: String) extends Actor {

  val game: Game = new Game(username)

  override def receive: Receive = {
    case Setup =>
      sender() ! Register(self)
      Database.setupTable()
      if (!Database.playerExists(username)){
//        println("PlayerNew")
        Database.createPlayer(username)
//        println("NewDatabase")
      }
      else {
        Database.loadGame(username, game)
//        println("PlayerExisting")
      }
    case Update =>
      game.update(System.nanoTime())
      println("TimeUpdate")
      sender() ! GameState(game.toJSON())
      println("JSONUpdate")
    case Save => Database.saveGame(
      username,
      game.gold,
      game.equipment("shovel").numberOwned,
      game.equipment("excavator").numberOwned,
      game.equipment("mine").numberOwned,
      game.lastUpdateTime)
//      println("ValuesSave")
    case ClickGold =>
      game.clickGold()
//      println("ClickGold")
    case buy: BuyEquipment =>
      game.buyEquipment(buy.equipmentID)
//      println("Buy", buy.equipmentID)
  }
}
