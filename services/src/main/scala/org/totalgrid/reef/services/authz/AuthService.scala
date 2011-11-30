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
package org.totalgrid.reef.services.authz

import org.totalgrid.reef.client.proto.Envelope
import org.totalgrid.reef.services.framework.RequestContext

case class AuthDenied(reason: String, status: Envelope.Status)

/**
 *   Interface that acts as a Policy Decision Point (PDP) that can be invoked from multiple locations
 */
trait AuthService {

  /**
   *     @param component Unique system-wide name for component making authorization request
   *     @param action - Component specific action. Could be anything like Rest Verb or CRUD verb
   *     @return None if authorization is given, Some(AuthDenied) otherwise
   */
  def isAuthorized(componentId: String, actionId: String, context: RequestContext): Option[AuthDenied]
}

/**
 * Useful for testing where we don't want to worry about authorization
 */
object NullAuthService extends AuthService {

  final override def isAuthorized(componentId: String, actionId: String, context: RequestContext) = None

}