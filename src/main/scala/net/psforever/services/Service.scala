// Copyright (c) 2017 PSForever
package net.psforever.services

import akka.event.{ActorEventBus, SubchannelClassification}
import akka.util.Subclassification
import net.psforever.types.PlanetSideGUID
import scala.collection.concurrent
import scala.jdk.CollectionConverters._
import java.util.concurrent.ConcurrentHashMap
import io.prometheus.client.Counter

object Service {
  final val defaultPlayerGUID: PlanetSideGUID = PlanetSideGUID(0)

  final case class Startup()

  final case class Join(channel: String)
  final case class Leave(channel: Option[String] = None)
  final case class LeaveAll()
}

trait GenericEventBusMsg {
  def channel: String
  def inner: Any
}

trait GenericGuidEventBusMsg extends GenericEventBusMsg {
  def guid: PlanetSideGUID
  def shouldRateLimit: Boolean
}

object GenericEventBus {
  final val genericEventBusPublish = Counter
    .build()
    .name("generic_event_bus_publish")
    .help("The number of events that GenericEventBus has published.")
    .labelNames("channel", "type")
    .register()
}

class GenericEventBus[A <: GenericEventBusMsg] extends ActorEventBus with SubchannelClassification {
  type Event      = A
  type Classifier = String

  protected def classify(event: Event): Classifier = event.channel

  protected def subclassification =
    new Subclassification[Classifier] {
      def isEqual(x: Classifier, y: Classifier)    = x == y
      def isSubclass(x: Classifier, y: Classifier) = x.startsWith(y)
    }

  protected def publish(event: Event, subscriber: Subscriber): Unit = {
    val trimmedEventClassName =
      event.inner.getClass().getName().stripPrefix(event.inner.getClass.getPackageName() + ".").stripSuffix("$")
    GenericEventBus.genericEventBusPublish.labels(event.channel, trimmedEventClassName).inc()
    subscriber ! event
  }
}

object GenericGuidEventBus {
  final val genericGuidEventBusPublish = Counter
    .build()
    .name("generic_guid_event_bus_publish")
    .help("The number of events that GenericGuidEventBus has published.")
    .labelNames("channel", "type")
    .register()
}

class GenericGuidEventBus[A <: GenericGuidEventBusMsg](rateLimit: Double)
    extends ActorEventBus
    with SubchannelClassification {
  private val buffer: concurrent.Map[String, concurrent.Map[String, concurrent.Map[PlanetSideGUID, A]]] =
    new ConcurrentHashMap[String, concurrent.Map[String, concurrent.Map[PlanetSideGUID, A]]]().asScala

  class Scheduler extends Thread {
    val sleepInterval = scala.math.floor(1000.0 / rateLimit).toInt
    override def run(): Unit = {
      while (true) {
        Thread.sleep(sleepInterval)
        buffer.foreachEntry { (channel, map) =>
          map.foreachEntry { (typeName, map) =>
            map.foreachEntry { (guid, event) =>
              publishForce(event)
            }
            map.clear()
          }
        }
      }
    }
  }

  private val handle = new Scheduler()
  handle.start()

  type Event      = A
  type Classifier = String

  protected def classify(event: Event): Classifier = event.channel

  protected def subclassification =
    new Subclassification[Classifier] {
      def isEqual(x: Classifier, y: Classifier)    = x == y
      def isSubclass(x: Classifier, y: Classifier) = x.startsWith(y)
    }

  override def publish(event: Event): Unit = {
    if (rateLimit <= 0 || !event.shouldRateLimit) {
      publishForce(event)
    } else {
      val eventClassName = event.inner.getClass().getName()
      buffer
        .getOrElseUpdate(event.channel, new ConcurrentHashMap[String, concurrent.Map[PlanetSideGUID, A]]().asScala)
        .getOrElseUpdate(eventClassName, new ConcurrentHashMap[PlanetSideGUID, A]().asScala)
        .updateWith(event.guid) { _ => Some(event) }
    }
  }

  protected def publishForce(event: Event): Unit = {
    super.publish(event)
  }

  protected def publish(event: Event, subscriber: Subscriber): Unit = {
    val trimmedEventClassName =
      event.inner.getClass().getName().stripPrefix(event.inner.getClass.getPackageName() + ".").stripSuffix("$")
    GenericGuidEventBus.genericGuidEventBusPublish.labels(event.channel, trimmedEventClassName).inc()
    subscriber ! event
  }
}
