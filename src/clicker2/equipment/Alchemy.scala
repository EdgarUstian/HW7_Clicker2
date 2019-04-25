package clicker2.equipment

class Alchemy extends Equipment{

  this.name = "Alchemy"

  override def goldPerSecond(): Double = {
    this.numberOwned * 40.0
  }
  override def goldPerClick(): Double = {
    this.numberOwned * 5000.0
  }

  override def costOfNextPurchase(): Double = {
    10000 * Math.pow(1.1, this.numberOwned)
  }

}
