package net.liftweb.http

import net.liftweb.http.js.JsCmds.{Noop, SetHtml}
import net.liftweb.http.js.JsCmd

/**
 * @author Joa Ebert
 * @date 2/15/11
 */
object Zones {
	private object listeners extends RequestVar[Map[String, List[() => SetHtml]]](Map.empty)

	def add(event: String, listener: () => SetHtml) = {
		val map = listeners.is
		listeners(map.updated(event, listener :: map.getOrElse(event, Nil)))
	}

	def dispatch(event: String): JsCmd =
		listeners.is.get(event) match {
			case Some(targets) => targets map { _() }
			case None => Noop
		}
}