// Copyright (c) 2017 PSForever
package net.psforever.objects.loadouts

import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.definition._

/**
  * A blueprint of a vehicle's mounted weapons and its inventory items, saved in a specific state.
  * This previous state can be restored on an apporpriate vehicle template
  * by reconstructing the items (if permitted).
  * Mismatched vehicles may produce no loadout or an imperfect loadout depending on specifications.<br>
  * <br>
  * The second tab on an `repair_silo` window is occupied by the list of "Favorite" `Loadout` blueprints.
  * The five-long list is initialized with `FavoritesMessage` packets assigned to the "Vehicle" list.
  * Specific entries are added or removed using `FavoritesRequest` packets,
  * re-established using other conventional game packets.
  * @param label the name by which this inventory will be known when displayed in a Favorites list;
  *              field gets inherited
  * @param visible_slots simplified representation of the `Equipment` that can see "seen" on the target;
  *              field gets inherited
  * @param inventory simplified representation of the `Equipment` in the target's inventory or trunk;
  *              field gets inherited
  * @param vehicle_definition the original type of vehicle whose state is being populated
  */
final case class VehicleLoadout(
    label: String,
    visible_slots: List[Loadout.SimplifiedEntry],
    inventory: List[Loadout.SimplifiedEntry],
    vehicle_definition: VehicleDefinition
) extends EquipmentLoadout(label, visible_slots, inventory)

object VehicleLoadout {

  /**
    * The variant of the battleframe vehicle.
    * Why these numbers map to the specific type of battleframe is a mystery.
    * @see `FavoritesMessage`
    * @param definition the vehicle's definition
    * @return a number directly indicative of the type
    */
  def DetermineBattleframeSubtype(definition: VehicleDefinition): Int = {
    definition match {
      case GlobalDefinitions.aphelion_flight  => 1
      case GlobalDefinitions.aphelion_gunner  => 2
      case GlobalDefinitions.colossus_flight  => 4
      case GlobalDefinitions.colossus_gunner  => 5
      case GlobalDefinitions.peregrine_flight => 7
      case GlobalDefinitions.peregrine_gunner => 8
      case _                                  => 0
    }
  }
}
