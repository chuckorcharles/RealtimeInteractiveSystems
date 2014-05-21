package main.scala.components

import main.scala.architecture.Component
import main.scala.systems.input.{Key, Triggers}
import scala.xml.Node
import main.scala.tools.Identifier

/**
 * User: uni
 * Date: 19.05.14
 * Time: 21:06
 * This is a RIS Project class
 */
case class DriveControl(triggerForward: Triggers = Triggers(Key._W), triggerBackward: Triggers = Triggers(Key._S), triggerLeft: Triggers = Triggers(Key._A),
                        triggerRight: Triggers = Triggers(Key._D), triggerBoost: Triggers = Triggers(Key.ShiftLeft)) extends Component {

  def newInstance(identifier: Identifier): Component = new DriveControl(triggerForward, triggerBackward,
                  triggerLeft, triggerRight, triggerBoost)

  def toXML: Node = ???
}
