package main.scala.event.deprecated

import akka.actor.ActorRef
import scala.collection.mutable
import main.scala.tools.DC

/**
 * Created by Christian Treffs
 * Date: 12.11.13 16:35
 */
class Signal[T](private var value: T)(implicit val owner: ActorRef) {

  /**
   * last update time
   */
  private var _lastUpdate = 0L
  def lastUpdate: Long = _lastUpdate
  private def lastUpdate_= (timestamp: Long): Unit = _lastUpdate = timestamp

  /**
   * all registered observers
   */
  private val observers = new mutable.HashSet[ActorRef]()

  /**
   * the current value of the signal
   * @return the value
   */
  def now() = value


  /**
   * register new observer-actor
   * @param actor the observer
   */
  def registerObserver(actor: ActorRef) = observers.add(actor)

  /**
   * update the signal value and traverse the changes
   * @param newValue the new signal value
   * @param currentActor the actor that caused the value change
   * @tparam A the type of the signal
   * @return ?
   */
  def update[A <: T](newValue: A)(implicit currentActor: ActorRef) {
    lastUpdate = System.nanoTime()
    currentActor match {
      case `owner` => updateObservers(newValue)
      case _       => updateSelf(newValue)
    }
    DC.log("Signal updated", this)
  }

  /**
   * tell the owner actor to update the value of this signal
   * @param v the new value
   */
  private def updateSelf(v: T) {
    owner ! UpdateSignalValue[T](this, v)
  }

  /**
   * tell all observing actors to update (including yourself), if the updating actor is the owner of the signal
   * @param v the new value
   */
  private def updateObservers(v: T) {
    value = v //TODO: is this the right place to update the value?
    observers.foreach(ref => ref ! PublishSignalValueUpdate[T](this, v))
    DC.log("Signal observers updated", this)
  }

  override def toString: String = {
    "Signal [t:"+value.getClass.getSimpleName+", @:"+lastUpdate+"] = '"+now()+"'"
  }
}
