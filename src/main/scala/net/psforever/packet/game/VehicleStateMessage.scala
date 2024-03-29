// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{Angular, PlanetSideGUID, Vector3}
import scodec.Codec
import scodec.codecs._

//TODO write more thorough comments later.
/**
  * Dispatched to report and update the operational condition of a given vehicle.
  * @param vehicle_guid the vehicle
  * @param unk1 na
  * @param pos the xyz-coordinate location in the world
  * @param ang the orientation of the vehicle
  * @param vel optional movement data
  * @param flying flight information, valid only for a vehicle that can fly when in flight;
  *               `Some(7)`, when in a flying state (vertical thrust unnecessary to unlock movement);
  *               `Some(10) - Some(15)`, used by the HART during landing and take-off,
  *               in repeating order: 13, 14, 10, 11, 12, 15;
  *               `None`, when landed and for all vehicles that do not fly
  * @param unk3 na
  * @param unk4 na;
  *             0 by default;
  *             for mosquito, can become various numbers during collision damage
  * @param wheel_direction for ground vehicles, whether and how much the wheels are being turned;
  *                        15 for straight;
  *                        0 for hard right;
  *                        30 for hard left;
  *                        values in between are possible;
  *                        vehicles that hover also influence this field as expected
  * @param is_decelerating If the vehicle is decelerating (in any direction), i.e. if the brake lights are on.
  * @param is_cloaked vehicle is cloaked by virtue of being a Wraith or a Phantasm
  * @see `PlacementData`
  */

/*
    BETA CLIENT DEBUG INFO:
      Guid
      Agreement Id
      Turn
      Brakes
      Position
      Orientation
      Velocity
      Flight Status
      Path Number
      Has Damage Info %d (%d %d %d %d)
      Cloaking
 */
final case class VehicleStateMessage(
    vehicle_guid: PlanetSideGUID,
    unk1: Int,
    pos: Vector3,
    ang: Vector3,
    vel: Option[Vector3],
    flying: Option[Int],
    unk3: Int,
    unk4: Int,
    wheel_direction: Int,
    is_decelerating: Boolean,
    is_cloaked: Boolean
) extends PlanetSideGamePacket {
  type Packet = VehicleStateMessage
  def opcode = GamePacketOpcode.VehicleStateMessage
  def encode = VehicleStateMessage.encode(this)
}

object VehicleStateMessage extends Marshallable[VehicleStateMessage] {

  /**
    * Calculate common orientation from little-endian bit data.
    * @see `Angular.codec_roll`
    * @see `Angular.codec_pitch`
    * @see `Angular.codec_yaw`
    */
  val codec_orient: Codec[Vector3] = (
    ("roll" | Angular.codec_roll(10)) ::
      ("pitch" | Angular.codec_pitch(10)) ::
      ("yaw" | Angular.codec_yaw(10, 90f))
  ).as[Vector3]

  implicit val codec: Codec[VehicleStateMessage] = (
    ("vehicle_guid" | PlanetSideGUID.codec) ::
      ("unk1" | uintL(bits = 3)) ::
      ("pos" | Vector3.codec_pos) ::
      ("ang" | codec_orient) ::
      ("vel" | optional(bool, Vector3.codec_vel)) ::
      ("flying" | optional(bool, uintL(bits = 5))) ::
      ("unk3" | uintL(bits = 7)) ::
      ("unk4" | uint4L) ::
      ("wheel_direction" | uintL(5)) ::
      ("is_decelerating" | bool) ::
      ("is_cloaked" | bool)
  ).as[VehicleStateMessage]
}
