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
package org.totalgrid.reef.client.sapi.rpc.impl

import org.totalgrid.reef.client.sapi.rpc.impl.builders.{ AuthTokenRequestBuilders => Builder }

import org.totalgrid.reef.client.sapi.rpc.AuthTokenService
import org.totalgrid.reef.api.sapi.client.rpc.framework.HasAnnotatedOperations

trait AuthTokenServiceImpl extends HasAnnotatedOperations with AuthTokenService {

  override def createNewAuthorizationToken(user: String, password: String) = ops.operation("Failed to get auth token for user: " + user) {
    _.put(Builder.requestAuthToken(user, password)).map(_.one.map(_.getToken))
  }

  override def deleteAuthorizationToken(token: String) = ops.operation("Couldn't delete auth token") {
    _.delete(Builder.deleteAuthToken(token)).map(_.one)
  }
}

