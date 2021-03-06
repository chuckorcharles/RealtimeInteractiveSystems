package main.scala.architecture

import scala.collection.mutable
import main.scala.tools.DC
import main.scala.event._
import main.scala.event.NodeAdded
import main.scala.entities.Entity

/**
 * Created by Eike on 20.03.14.
 *
 * A collection of Entities with a set of Components
 */
class Family(val nodeClass: Class[_ <: Node]) extends EventReceiver {
 EventDispatcher.subscribe(classOf[Event])(this)
 var _entities: mutable.HashMap[Entity, Node] = mutable.HashMap.empty[Entity, Node]
 implicit val family = this
 val components: List[Class[_ <: Component]] = Node.getDefinition(nodeClass).apply(true)
 val unwanted: List[Class[_ <: Component]] = Node.getDefinition(nodeClass).apply(false)

  def receive(ev: Event ){
    ev match {
      case compRemoved: ComponentRemoved => componentRemoved(compRemoved.ent, compRemoved.comp.getClass)
      case compAdded: ComponentAdded => addIfMatch(compAdded.ent)
      case entAdded: EntityCreated => addIfMatch(entAdded.ent)
      case _ =>
    }
  }

  def entities = _entities
  def nodes = _entities.values

  def addIfMatch(entity: Entity){
    if (!_entities.contains(entity)){

      for(componentClass <- unwanted){
        if(entity.has(componentClass)) {
        return
        }
      }

      for(componentClass <- components){
        if(!entity.has(componentClass)){
          return
        }
      }

      val node = nodeClass.newInstance()
      for (componentClass <- components){
        node.components.put(componentClass, entity.components(componentClass).apply(0))
      }
      entities.put(entity,node)
      EventDispatcher.dispatch(NodeAdded(node))
      DC.log("Family added entity ",(entity,node))
    }

  }


  def componentRemoved(entity : Entity , componentClass : Class[_ <: Component] ) : Unit = {
    if(components.contains(componentClass) && entities.contains(entity)) entities.remove(entity)
}

  def remove(entity: Entity){
    entities.remove(entity)
  }

}