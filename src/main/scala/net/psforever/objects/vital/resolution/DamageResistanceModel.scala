// Copyright (c) 2017 PSForever
package net.psforever.objects.vital.resolution

import net.psforever.objects.vital.base.DamageType
import net.psforever.objects.vital.damage.DamageCalculations
import net.psforever.objects.vital.interaction.{DamageInteraction, DamageResult}
import net.psforever.objects.vital.resistance.ResistanceSelection
import net.psforever.objects.vital.{NoResistanceSelection, NoResolutions}

/**
  * The functionality that is necessary for interaction of a vital game object with the rest of the hostile game world.
  */
trait DamageAndResistance {
  def DamageUsing: DamageCalculations.Selector

  def ResistUsing: ResistanceSelection

  def Model: ResolutionCalculations.Form

  def calculate(data: DamageInteraction): ResolutionCalculations.Output

  def calculate(data: DamageInteraction, resolution: DamageType.Value): ResolutionCalculations.Output
}

object DamageAndResistance {

  /**
    * A pass-through function.
    * @param data garbage in
    * @return garbage out
    */
  def doNothingFallback(data: DamageInteraction): ResolutionCalculations.Output = { _: Any =>
    DamageResult(data.target, data.target, data)
  }
}

/**
  * The functionality that is necessary for interaction of a vital game object with the rest of the hostile game world.<br>
  * <br>
  * A vital object can be hurt or damaged or healed or repaired (HDHR).
  * The actual implementation of how that works is left to the specific object and its interfaces, however.
  * The more involved values that are applied to the vital object are calculated by a series of functions
  * that contribute different values, e.g., the value for being damaged.
  * "Being damaged" is also not the same for all valid targets:
  * some targets don't utilize the same kinds of values in the same way as another,
  * and some targets utilize a different assortment of values than either of the first two examples.
  * The damage model is a common interface for producing those values
  * and reconciling those values with a valid target object
  * without much fuss.<br>
  * <br>
  * By default, nothing should do anything of substance.
  * @see `Vitality`
  */
trait DamageResistanceModel extends DamageAndResistance {

  /** the functionality that processes damage; required */
  private var damageUsing: DamageCalculations.Selector = DamageCalculations.AgainstNothing

  /** the functionality that processes resistance; optional */
  private var resistUsing: ResistanceSelection = NoResistanceSelection

  /** the functionality that prepares for damage application actions; required */
  private var model: ResolutionCalculations.Form = NoResolutions.calculate

  def DamageUsing: DamageCalculations.Selector = damageUsing

  def DamageUsing_=(selector: DamageCalculations.Selector): DamageCalculations.Selector = {
    damageUsing = selector
    DamageUsing
  }

  def ResistUsing: ResistanceSelection = resistUsing

  def ResistUsing_=(selector: ResistanceSelection): ResistanceSelection = {
    resistUsing = selector
    ResistUsing
  }

  def Model: ResolutionCalculations.Form = model

  def Model_=(selector: ResolutionCalculations.Form): ResolutionCalculations.Form = {
    model = selector
    Model
  }

  /**
    * Magic stuff.
    * @param data the historical damage information
    * @return a function literal that encapsulates delayed modification instructions for certain objects
    */
  def calculate(data: DamageInteraction): ResolutionCalculations.Output = {
    val res: ResistanceSelection.Format = ResistUsing(data)
    Model(DamageUsing, res, data)
  }

  /**
    * Magic stuff.
    * @param data the historical damage information
    * @param resolution an explicit damage resolution overriding the one provided
    * @return a function literal that encapsulates delayed modification instructions for certain objects
    */
  def calculate(data: DamageInteraction, resolution: DamageType.Value): ResolutionCalculations.Output = {
    val res: ResistanceSelection.Format = ResistUsing(resolution)
    Model(DamageUsing, res, data)
  }
}
