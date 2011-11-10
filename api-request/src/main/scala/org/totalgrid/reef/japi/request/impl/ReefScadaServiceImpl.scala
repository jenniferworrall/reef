/**
 * Copyright 2011 Green Energy Corp.
 *
 * Licensed to Green Energy Corp (www.greenenergycorp.com) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. Green Energy
 * Corp licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.totalgrid.reef.japi.request.impl

import org.totalgrid.reef.japi.request._
import org.totalgrid.reef.japi.client.{ Session, SessionExecutionPool }
import org.totalgrid.reef.sapi.client.{ SubscriptionManagement, RestOperations, SessionPool }
import org.totalgrid.reef.messaging.javaclient.AMQPConnection

/**
 * "Super" interface that includes all of the helpers for the individual services. This could be broken down
 * into smaller functionality based sections or not created at all.
 */
class AllScadaServicePooledWrapper(_sessionPool: SessionExecutionPool, _authToken: String)
    extends AllScadaService with AllScadaServiceImpl with AuthorizedAndPooledClientSource {
  def authToken = _authToken
  def sessionPool = _sessionPool
  override def clientSessionFactory = _sessionPool.getConnection.asInstanceOf[AMQPConnection].getClientSessionFactory
}

class AllScadaServiceWrapper(_session: Session) extends AllScadaService with AllScadaServiceImpl with SingleSessionClientSource {
  def session = convertByCasting(_session)
}

class AuthTokenServicePooledWrapper(_sessionPool: SessionExecutionPool) extends AuthTokenService with AuthTokenServiceImpl with PooledClientSource {
  def sessionPool = _sessionPool
  override def clientSessionFactory = _sessionPool.getConnection.asInstanceOf[AMQPConnection].getClientSessionFactory
}

class AuthTokenServiceWrapper(_session: Session) extends AuthTokenService with AuthTokenServiceImpl with SingleSessionClientSource {
  def session = convertByCasting(_session)
}

class AllScadaServicePooled(sessionPool: SessionPool, authToken: String)
    extends AllScadaService with AllScadaServiceImpl with ClientSource {

  override def _ops[A](block: RestOperations with SubscriptionManagement => A): A = {
    sessionPool.borrow(authToken)(block)
  }
}
