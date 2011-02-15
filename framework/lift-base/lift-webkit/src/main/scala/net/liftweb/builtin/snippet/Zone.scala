package net.liftweb.builtin.snippet

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.util.Helpers

import scala.xml.NodeSeq

/**
 * @author Joa Ebert
 * @date 2/15/11
 */
object Zone extends DispatchSnippet {
	def dispatch: DispatchIt = { case _ => render _ }

	def render(in: NodeSeq): NodeSeq =
		S.attr("event") match {
			case Full(event) =>
				val zoneId = Helpers.nextFuncName
				Zones.add(event, () => SetHtml(zoneId, in))
				<div id={zoneId}>{in}</div>
			case _ => in
		}
}