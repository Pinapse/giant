// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.doors

import net.psforever.objects.Player
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.packet.game.UseItemMessage

/**
  * A structure-owned server object that is a "door" that can open and can close.
  * @param ddef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
  */
class Door(private val ddef: DoorDefinition) extends Amenity {
  private var openState: Option[Player] = None

  def isOpen: Boolean = openState.isDefined

  def Open: Option[Player] = openState

  def Open_=(player: Player): Option[Player] = {
    Open_=(Some(player))
  }

  def Open_=(open: Option[Player]): Option[Player] = {
    openState = open
    Open
  }

  /** Doors do not have health, so only check if they are damageable. */
  override def CanDamage: Boolean = Definition.Damageable

  def Definition: DoorDefinition = ddef
}

object Door {

  /**
    * Entry message into this `Door` that carries the request.
    * @param player the player who sent this request message
    * @param msg the original packet carrying the request
    */
  final case class Use(player: Player, msg: UseItemMessage)

  /**
    * A basic `Trait` connecting all of the actionable `Door` response messages.
    */
  sealed trait Exchange

  /**
    * Message that carries the result of the processed request message back to the original user (`player`).
    * @param player the player who sent this request message
    * @param msg the original packet carrying the request
    * @param response the result of the processed request
    */
  final case class DoorMessage(player: Player, msg: UseItemMessage, response: Exchange)

  /**
    * This door will open.
    */
  final case class OpenEvent() extends Exchange

  /**
    * This door will close.
    */
  final case class CloseEvent() extends Exchange

  /**
    * This door will do nothing.
    */
  final case class NoEvent() extends Exchange

  type LockingMechanismLogic = (PlanetSideServerObject, Door) => Boolean

  final case class UpdateMechanism(mechanism: LockingMechanismLogic) extends Exchange

  case object Lock extends Exchange

  case object Unlock extends Exchange

  /**
    * Overloaded constructor.
    * @param tdef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
    */
  def apply(tdef: DoorDefinition): Door = {
    new Door(tdef)
  }

  import akka.actor.ActorContext

  /**
    * Instantiate and configure a `Door` object.
    * @param id the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality
    * @return the `Door` object
    */
  def Constructor(id: Int, context: ActorContext): Door = {
    import akka.actor.Props
    import net.psforever.objects.GlobalDefinitions

    val obj = Door(GlobalDefinitions.door)
    obj.Actor = context.actorOf(Props(classOf[DoorControl], obj), s"${GlobalDefinitions.door.Name}_$id")
    obj
  }

  import net.psforever.types.Vector3

  /**
    * Instantiate and configure a `Door` object that has knowledge of both its position and outwards-facing direction.
    * The assumption is that this door will be paired with an IFF Lock, thus, has conditions for opening.
    * @param pos the position of the door
    * @param id the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality
    * @return the `Door` object
    */
  def Constructor(pos: Vector3)(id: Int, context: ActorContext): Door = {
    import net.psforever.objects.GlobalDefinitions
    Constructor(pos, GlobalDefinitions.door)(id, context)
  }

  /**
    * Instantiate and configure a `Door` object that has knowledge of both its position and outwards-facing direction.
    * The assumption is that this door will be paired with an IFF Lock, thus, has conditions for opening.
    * @param pos the position of the door
    * @param ddef the definition for this specific type of door
    * @param id the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality
    * @return the `Door` object
    */
  def Constructor(pos: Vector3, ddef: DoorDefinition)(id: Int, context: ActorContext): Door = {
    import akka.actor.Props

    val obj = Door(ddef)
    obj.Position = pos
    obj.Actor = context.actorOf(Props(classOf[DoorControl], obj), s"${ddef.Name}_$id")
    obj
  }
}
