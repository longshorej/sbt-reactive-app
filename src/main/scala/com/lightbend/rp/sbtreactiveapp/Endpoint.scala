/*
 * Copyright 2017 Lightbend, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lightbend.rp.sbtreactiveapp

import scala.collection.immutable.Seq

sealed trait Endpoint {
  def name: String
  def port: Int
  def protocol: String
  def version: Option[Version]
}

case class HttpEndpoint(name: String, portIngress: PortIngress, httpIngress: Seq[HttpIngress], version: Option[Version] = Some(MajorVersion)) extends Endpoint {
  val protocol: String = "http"
  val port: Int = portIngress.port
}

object HttpEndpoint {
  def apply(name: String, port: Int, ingress: HttpIngress*): HttpEndpoint = new HttpEndpoint(name, PortIngress(port), ingress.toVector)
}

case class TcpEndpoint(name: String, ingress: PortIngress, version: Option[Version] = Some(MajorVersion)) extends Endpoint {
  val protocol: String = "tcp"
  val port: Int = ingress.port
}

object TcpEndpoint {
  def apply(name: String, port: Int): TcpEndpoint = new TcpEndpoint(name, PortIngress(port))
}

case class UdpEndpoint(name: String, ingress: PortIngress, version: Option[Version] = Some(MajorVersion)) extends Endpoint {
  val protocol: String = "udp"
  val port: Int = ingress.port
}

object UdpEndpoint {
  def apply(name: String, port: Int): UdpEndpoint = new UdpEndpoint(name, PortIngress(port))
}
