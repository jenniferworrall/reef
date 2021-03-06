/**
 * Copyright 2011 Green Energy Corp.
 *
 * Licensed to Green Energy Corp (www.greenenergycorp.com) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. Green Energy
 * Corp licenses this file to you under the GNU Affero General Public License
 * Version 3.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/agpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.totalgrid.reef.services.core

import org.totalgrid.reef.client.service.proto.Alarms._

import org.totalgrid.reef.client.service.proto.Descriptors
import org.totalgrid.reef.services.framework._

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.ast.{ OrderByArg, ExpressionNode }

import org.totalgrid.reef.client.service.proto.OptionalProtos._
import org.totalgrid.reef.services.framework.SimpleServiceBehaviors.SimpleRead
import org.totalgrid.reef.client.exception.BadRequestException
import org.totalgrid.reef.models._

// implicit proto properties
import SquerylModel._ // implict asParam
import org.totalgrid.reef.client.sapi.types.Optional._
import scala.collection.JavaConversions._

import org.squeryl.dsl.ast.LogicalBoolean

object AlarmQueryService {

  def buildQuery(row: AlarmModel, eventRow: EventStore, select: AlarmSelect): List[Option[LogicalBoolean]] = {
    List(
      select.getStateList.asParam(row.state in _.map(_.getNumber))) ::: select.eventSelect.map(EventQueryService.buildQuery(eventRow, _)).getOrElse(Nil)
  }

  def makeSubscriptionKeyParts(select: AlarmSelect): List[List[String]] = {

    val eventParts = EventQueryService.makeSubscriptionKeyParts(select.getEventSelect)
    val alarmParts = List(Nil: List[String]) ::: eventParts

    alarmParts
  }

  def tupleGroup[A, B](tuples: List[(A, B)]): Map[A, List[B]] = {
    val map = scala.collection.mutable.Map[A, List[B]]()
    for (tup <- tuples) {
      val (k, v) = tup
      if (map contains k) {
        map(k) = v :: map(k)
      } else {
        map(k) = v :: Nil
      }
    }
    map.toMap
  }
}

class AlarmQueryService
    extends ServiceEntryPoint[AlarmList]
    with SimpleRead {

  import AlarmQueryService._

  override val descriptor = Descriptors.alarmList

  override def getSubscribeKeys(req: AlarmList) = {
    createSubscriptionPermutations(makeSubscriptionKeyParts(req.getSelect)).map { ProtoRoutingKeys.generateRoutingKey(_) }
  }
  override val subscriptionClass = classOf[Alarm]

  override def doGet(context: RequestContext, req: AlarmList): AlarmList = {
    import ApplicationSchema._
    import AlarmQueryService._

    if (!req.hasSelect) throw new BadRequestException("Must include select")

    val select = req.getSelect

    // default all queries to max of 1000 events.
    val limit = select.eventSelect.limit getOrElse 1000
    if (limit < 0) {
      throw new BadRequestException("Limit must be non-negative")
    }

    val results =
      from(alarms, events)((alarm, event) =>
        where(SquerylConversions.combineExpressions(buildQuery(alarm, event, select).flatten) and
          alarm.eventId === event.id)
          select ((alarm, event))
          orderBy timeOrder(event.time, select.eventSelect.ascending)).page(0, limit).toList

    val entIds = results.map { case (_, event) => event.entityId }.flatten.distinct

    val entToTypes: List[(Entity, Option[String])] =
      join(entities, entityTypes.leftOuter)((e, t) =>
        where(e.id in entIds)
          select (e, t.map(_.entType))
          on (Some(e.id) === t.map(_.entityId))).toList

    val typMap = AlarmQueryService.tupleGroup(entToTypes)

    typMap.foreach { case (ent, typList) => ent.types.value = typList.flatten }

    val entMap = typMap.keys.map(e => (e.id, e)).toMap

    val alarmProtos =
      results.map {
        case (alarm, event) =>
          event.entityId.foreach { entId =>
            event.entity.value = entMap.get(entId)
          }
          AlarmConversion.convertToProto(alarm, event)
      }

    context.auth.authorize(context, Descriptors.alarm.id, "read", entMap.values.toList.map { _.id })

    val alarmList = AlarmList.newBuilder.addAllAlarms(alarmProtos).build

    alarmList

  }

  def timeOrder(time: ExpressionNode, ascending: Option[Boolean]) = {
    if (ascending getOrElse false)
      new OrderByArg(time).asc
    else
      new OrderByArg(time).desc
  }

}
