package main.scala.components

import main.scala.tools.Identifier
import main.scala.architecture.{ComponentCreator, Component}
import scala.xml.{NodeBuffer, NodeSeq}

/**
 * Created by Christian Treffs
 * Date: 21.03.14 10:06
 */
object ChildEntity extends ComponentCreator{
  override def fromXML(xml: NodeBuffer): Component = ???
}
case class ChildEntity(childIdentifier: Identifier)  extends Component {
  override def toXML: NodeBuffer = ???
}
