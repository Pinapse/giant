// Copyright (c) 2017 PSForever
package net.psforever.objects.avatar

import net.psforever.actors.session.AvatarActor
import net.psforever.objects.avatar.scoring.ScoreCard
import net.psforever.objects.definition.{AvatarDefinition, BasicDefinition}
import net.psforever.objects.equipment.{EquipmentSize, EquipmentSlot}
import net.psforever.objects.inventory.LocallyRegisteredInventory
import net.psforever.objects.loadouts.{Loadout, SquadLoadout}
import net.psforever.objects.locker.{LockerContainer, LockerEquipment}
import net.psforever.objects.{GlobalDefinitions, OffhandEquipmentSlot}
import net.psforever.packet.game.objectcreate.{BasicCharacterData, RibbonBars}
import net.psforever.types._
import org.joda.time.{Duration, LocalDateTime, Seconds}

import scala.concurrent.duration._

object Avatar {
  val purchaseCooldowns: Map[BasicDefinition, FiniteDuration] = Map(
    GlobalDefinitions.ams                   -> 10.seconds,
    GlobalDefinitions.ant                   -> 10.seconds,
    GlobalDefinitions.apc_nc                -> 10.seconds,
    GlobalDefinitions.apc_tr                -> 10.seconds,
    GlobalDefinitions.apc_vs                -> 10.seconds,
    GlobalDefinitions.aphelion_flight       -> 10.seconds, //Temporarily - Default is 25 minutes
    GlobalDefinitions.aphelion_gunner       -> 10.seconds, //Temporarily - Default is 25 minutes
    GlobalDefinitions.aurora                -> 10.seconds,
    GlobalDefinitions.battlewagon           -> 10.seconds,
    GlobalDefinitions.colossus_flight       -> 10.seconds, //Temporarily - Default is 25 minutes
    GlobalDefinitions.colossus_gunner       -> 10.seconds, //Temporarily - Default is 25 minutes
    GlobalDefinitions.dropship              -> 10.seconds,
    GlobalDefinitions.flail                 -> 10.seconds,
    GlobalDefinitions.fury                  -> 10.seconds,
    GlobalDefinitions.galaxy_gunship        -> 10.seconds, //Temporary - Default is 10 minutes
    GlobalDefinitions.lodestar              -> 10.seconds,
    GlobalDefinitions.liberator             -> 10.seconds,
    GlobalDefinitions.lightgunship          -> 10.seconds,
    GlobalDefinitions.lightning             -> 10.seconds,
    GlobalDefinitions.magrider              -> 10.seconds,
    GlobalDefinitions.mediumtransport       -> 10.seconds,
    GlobalDefinitions.mosquito              -> 10.seconds,
    GlobalDefinitions.peregrine_flight      -> 10.seconds, //Temporarily - Default is 25 minutes
    GlobalDefinitions.peregrine_gunner      -> 10.seconds, //Temporarily - Default is 25 minutes
    GlobalDefinitions.phantasm              -> 10.seconds,
    GlobalDefinitions.prowler               -> 10.seconds,
    GlobalDefinitions.quadassault           -> 10.seconds,
    GlobalDefinitions.quadstealth           -> 10.seconds,
    GlobalDefinitions.router                -> 10.seconds,
    GlobalDefinitions.switchblade           -> 10.seconds,
    GlobalDefinitions.skyguard              -> 10.seconds,
    GlobalDefinitions.threemanheavybuggy    -> 10.seconds,
    GlobalDefinitions.thunderer             -> 10.seconds,
    GlobalDefinitions.two_man_assault_buggy -> 10.seconds,
    GlobalDefinitions.twomanhoverbuggy      -> 10.seconds,
    GlobalDefinitions.twomanheavybuggy      -> 10.seconds,
    GlobalDefinitions.vanguard              -> 10.seconds,
    GlobalDefinitions.vulture               -> 10.seconds,
    GlobalDefinitions.wasp                  -> 10.seconds,
    GlobalDefinitions.flamethrower          -> 10.seconds,
    GlobalDefinitions.nchev_sparrow         -> 10.seconds,
    GlobalDefinitions.nchev_falcon          -> 10.seconds,
    GlobalDefinitions.nchev_scattercannon   -> 10.seconds,
    GlobalDefinitions.vshev_comet           -> 10.seconds,
    GlobalDefinitions.vshev_quasar          -> 10.seconds,
    GlobalDefinitions.vshev_starfire        -> 10.seconds,
    GlobalDefinitions.trhev_burster         -> 10.seconds,
    GlobalDefinitions.trhev_dualcycler      -> 10.seconds,
    GlobalDefinitions.trhev_pounder         -> 10.seconds
  )

  val useCooldowns: Map[BasicDefinition, FiniteDuration] = Map(
    GlobalDefinitions.medkit           -> 5.seconds,
    GlobalDefinitions.super_armorkit   -> 20.minutes,
    GlobalDefinitions.super_medkit     -> 20.minutes,
    GlobalDefinitions.super_staminakit -> 5.minutes // Temporary - Default value is 20 minutes
  )

  def apply(
      charId: Int,
      name: String,
      faction: PlanetSideEmpire.Value,
      sex: CharacterSex,
      head: Int,
      voice: CharacterVoice.Value
  ): Avatar = {
    Avatar(charId, BasicCharacterData(name, faction, sex, head, voice))
  }

  def makeLocker(): LockerContainer = {
    new LockerContainer({
      val inv = new LocallyRegisteredInventory(numbers = 40150 until 40450) // TODO var bad
      inv.Resize(30, 20)
      inv
    })
  }
}

case class Cooldowns(
    /** Timestamps of when a vehicle or equipment was last purchased */
    purchase: Map[String, LocalDateTime] = Map(),
    /** Timestamps of when a vehicle or equipment was last purchased */
    use: Map[String, LocalDateTime] = Map()
)

case class Loadouts(
    suit: Seq[Option[Loadout]] = Seq.fill(20)(None),
    squad: Seq[Option[SquadLoadout]] = Seq.fill(10)(None)
)

case class ProgressDecoration(
    cosmetics: Option[Set[Cosmetic]] = None,
    ribbonBars: RibbonBars = RibbonBars(),
    firstTimeEvents: Set[String] =
      FirstTimeEvents.Maps ++ FirstTimeEvents.Monoliths ++
        FirstTimeEvents.Standard.All ++ FirstTimeEvents.Cavern.All ++
        FirstTimeEvents.TR.All ++ FirstTimeEvents.NC.All ++ FirstTimeEvents.VS.All ++
        FirstTimeEvents.Generic
)

case class MemberLists(
    friend: List[Friend] = List[Friend](),
    ignored: List[Ignored] = List[Ignored]()
)

case class Avatar(
    /** unique identifier corresponding to a database table row index */
    id: Int,
    basic: BasicCharacterData,
    bep: Long = 0,
    cep: Long = 0,
    stamina: Int = 100,
    fatigued: Boolean = false,
    certifications: Set[Certification] = Set(),
    implants: Seq[Option[Implant]] = Seq(None, None, None),
    shortcuts: Array[Option[Shortcut]] = Array.fill[Option[Shortcut]](64)(None),
    locker: LockerContainer = Avatar.makeLocker(),
    deployables: DeployableToolbox = new DeployableToolbox(),
    lookingForSquad: Boolean = false,
    var vehicle: Option[PlanetSideGUID] = None, // TODO var bad
    decoration: ProgressDecoration = ProgressDecoration(),
    loadouts: Loadouts = Loadouts(),
    cooldowns: Cooldowns = Cooldowns(),
    people: MemberLists = MemberLists(),
    scorecard: ScoreCard = new ScoreCard()
) {
  assert(bep >= 0)
  assert(cep >= 0)

  val br: BattleRank  = BattleRank.withExperience(bep)
  val cr: CommandRank = CommandRank.withExperience(cep)

  def name: String = basic.name

  def faction: PlanetSideEmpire.Value = basic.faction

  def sex: CharacterSex = basic.sex

  def head: Int = basic.head

  def voice: CharacterVoice.Value = basic.voice

  private def cooldown(
      times: Map[String, LocalDateTime],
      cooldowns: Map[BasicDefinition, FiniteDuration],
      definition: BasicDefinition
  ): Option[Duration] = {
    val (_, resolvedName) = AvatarActor.resolvePurchaseTimeName(faction, definition)
    times.get(resolvedName) match {
      case Some(purchaseTime) =>
        val secondsSincePurchase = Seconds.secondsBetween(purchaseTime, LocalDateTime.now())
        cooldowns.get(definition) match {
          case Some(cooldown) if (cooldown.toSeconds - secondsSincePurchase.getSeconds) > 0 =>
            Some(Seconds.seconds((cooldown.toSeconds - secondsSincePurchase.getSeconds).toInt).toStandardDuration)
          case _ => None
        }
      case None =>
        None
    }
  }

  /** Returns the remaining purchase cooldown or None if an object is not on cooldown */
  def purchaseCooldown(definition: BasicDefinition): Option[Duration] = {
    cooldown(cooldowns.purchase, Avatar.purchaseCooldowns, definition)
  }

  /** Returns the remaining use cooldown or None if an object is not on cooldown */
  def useCooldown(definition: BasicDefinition): Option[Duration] = {
    cooldown(cooldowns.use, Avatar.useCooldowns, definition)
  }

  def fifthSlot(): EquipmentSlot = {
    new OffhandEquipmentSlot(EquipmentSize.Inventory) {
      val obj = new LockerEquipment(locker)
      Equipment = obj
    }
  }

  val definition: AvatarDefinition = GlobalDefinitions.avatar

  /** Returns numerical value from 0-3 that is the hacking skill level representation in packets */
  def hackingSkillLevel(): Int = {
    if (
      certifications.contains(Certification.ExpertHacking) || certifications.contains(Certification.ElectronicsExpert)
    ) {
      3
    } else if (certifications.contains(Certification.AdvancedHacking)) {
      2
    } else if (certifications.contains(Certification.Hacking)) {
      1
    } else {
      0
    }
  }

  /** The maximum stamina amount */
  val maxStamina: Int = 100

  /** Return true if the stamina is at the maximum amount */
  def staminaFull: Boolean = {
    stamina == maxStamina
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[Avatar]

  override def equals(other: Any): Boolean =
    other match {
      case that: Avatar =>
        (that canEqual this) &&
          id == that.id
      case _ =>
        false
    }

  /** Avatar assertions
    * These protect against programming errors by asserting avatar properties have correct values
    * They may or may not be disabled for live applications
    */
  assert(stamina <= maxStamina && stamina >= 0)
  assert(head >= 0) // TODO what's the max value?
  assert(implants.length <= 3)
  assert(implants.flatten.map(_.definition.implantType).distinct.length == implants.flatten.length)
  assert(br.implantSlots >= implants.flatten.length)
}
