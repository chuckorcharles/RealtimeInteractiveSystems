package main.scala.components

import main.scala.architecture.{ComponentCreator, Component}
import main.scala.tools.Identifier
import scala.xml.Node
import main.scala.systems.input.Key
import main.scala.entities.Entity
import main.scala.math.Vec3f
import main.scala.io.EntityTemplateLoader

/**
 * Created by Christian Treffs
 * Date: 21.05.14 22:26
 */
object Text extends ComponentCreator {
  override def fromXML(xml: Node): Option[Text] = {
    //TODO
    None
  }
}

class Text() extends Component {

  var text: String = ""


  override def newInstance(identifier: Identifier): Component = new Text()
  override def toXML: Node = {
    <text>//TODO</text>
  }
}
