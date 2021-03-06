package main.scala.architecture

import main.scala.systems.input.SimulationContext
import scala.collection.mutable
import main.scala.entities.Entity
import scala.collection.mutable.ArrayBuffer

/**
 * Created by Christian Treffs
 * Date: 14.03.14 18:16
 */

trait Engine {

  private val _entities:mutable.HashMap[/*Class[_ <: Entity]*/ String, Entity] = new mutable.HashMap[/*Class[_ <: Entity]*/ String, Entity]
  private val _systems: mutable.HashMap[Class[_ <: System], System] = new mutable.HashMap[Class[_ <: System], System]
  private val _families: mutable.HashMap[Class[_ <: Node], Family] = new mutable.HashMap[Class[_ <: Node], Family]

  private val activeSystems: ArrayBuffer[System] = ArrayBuffer()


  def pause(excludedSystems: Seq[Class[_ <: System]]) {
    if(activeSystems.isEmpty) {
      for ((sysClass,sys) <- _systems) {
        if(!excludedSystems.contains(sysClass)) {
          sys.active match {
            case true => activeSystems += sys
            case false =>
          }
          activeSystems.foreach(_.active = false)
        }
      }
    }
  }
  def resume() {
    activeSystems.foreach(_.active = true)
    activeSystems.clear()
  }

  def start(): Engine
  def createNewGame(title: String, assetsPath: String): Engine
  def shutdown(): Unit
  protected def gameLoop(): Unit


  def entities:  mutable.HashMap[/*Class[_ <: Entity]*/ String, Entity] = _entities
  def systems: mutable.HashMap[Class[_ <: System], System] = _systems
  def families: mutable.HashMap[Class[_ <: Node], Family] = _families



  def remove (entity: Entity): Engine = this.-=(entity)
  def -= (entity: Entity): Engine = {
    _entities - entity.toString
    this
  }
  def add (entity: Entity): Engine = this.+=(entity)
  def += (entity: Entity): Engine = {
    _entities.put(entity.identifier.toString, entity)
    //println(_entities.toList)
    this
  }
  def remove (system: System): Engine = this.-=(system)
  def -= (system: System): Engine = {
    _systems - system.getClass
    //system.deinit()
    this
  }
  def add (system: System): Engine = {system.initialize(); this.+=(system)}
  def += (system: System): Engine = {
    _systems.put(system.getClass, system)
    //system.init()
    this
  }


  def remove (family: Family): Engine = this.-=(family)
  def -= (family: Family): Engine = {
    _families - family.nodeClass
    this
  }
  def add (family: Family): Engine = this.+=(family)
  def += (family: Family): Engine = {
    _families.put(family.nodeClass, family)
    this
  }


  def getSystem(systemClass : Class[_ <: System]) = systems.apply(systemClass)

  def componentAdded(entity: Entity){
    for(family <- families.values) family.addIfMatch(entity)
  }

  def registerFamily(fam: Family){
    families.put(fam.nodeClass, fam)
    for(entity <- entities.values){
      fam.addIfMatch(entity)
    }
  }

  def getNodeList(nodeClass : Node) : List[Node] = {
     families.get(nodeClass.getClass) match {
       case Some(fam) => fam.nodes.toList
       case None =>
         val family: Family = new Family(nodeClass.getClass)
         families.put(nodeClass.getClass, family)
         for (entity <- entities.values){
           family.addIfMatch(entity)
         }
         family.nodes.toList
     }
  }


  def updateSystems(context: SimulationContext): Engine = {
    systems.values.foreach(_.process(context))
    this
  }
}
