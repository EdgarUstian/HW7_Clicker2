package clicker2.networking

import akka.actor.Actor
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
      Database.setupTable()
      if (!Database.playerExists(username)){
        Database.createPlayer(username)
      }
      else {
        Database.loadGame(username, game)
      }
    case Update =>
      game.update(System.nanoTime())
      sender() ! GameState(game.toJSON())
    case Save => Database.saveGame(
      username,
      game.gold,
      game.equipment("shovel").numberOwned,
      game.equipment("excavator").numberOwned,
      game.equipment("mine").numberOwned,
      game.lastUpdateTime)
    case ClickGold => game.clickGold()
    case buy: BuyEquipment => game.buyEquipment(buy.equipmentID)
  }
}
