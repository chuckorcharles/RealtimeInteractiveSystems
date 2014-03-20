package main.scala.components

import main.scala.architecture.Component
import scala.xml.NodeSeq

/**
 * Created by Christian Treffs
 * Date: 21.03.14 00:46
 */
case class Gun(lifetimeProjectile1: Long, coolDown1: Long, timeOfLastShot1: Long = 0) extends Component {
  private var _lifetimeProjectile: Long = lifetimeProjectile1
  private var _coolDown: Long = coolDown1
  private var _timeOfLastShot: Long = timeOfLastShot1

  def lifetimeProjectile: Long = _lifetimeProjectile
  def lifetimeProjectile_=(t: Long) = _lifetimeProjectile = t

  def coolDown: Long = _coolDown
  def coolDown_=(t: Long) = _coolDown = t

  def timeOfLastShot: Long = _timeOfLastShot
  def timeOfLastShot_=(t: Long) = _timeOfLastShot = t

  override def toXML: NodeSeq = ???


}