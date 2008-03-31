/*
 * Copyright 2007-2008 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package net.liftweb.builtin.snippet

import net.liftweb.http._
import net.liftweb.http.S._
import scala.xml._
import net.liftweb.util.Helpers._
import net.liftweb.util.{Can, Full}

 
/**
 * This class is a built in snippet that allows rendering only messages (Errors, Warnings, Notices)
 * that are associated with the id provided. Typically this will be used near by form fields to 
 * indicate that a certain field failed the validation.
 *
 * E.g.
 * <pre>
 *   &lt;input type="text" value="" name="132746123548765"/&gt;&lt;lift:msg id="user_msg"/&gt;
 * 
 * or
 * 
 *   &lt;input type="text" value="" name="132746123548765"/&gt;&lt;lift:msg id="user_msg" 
 *                                                        errorClass="error_class" 
 *                                                        warningClass="warning_class" 
 *                                                        noticeClass="notice_class"/&gt;
 * </pre>
 */
class Msg { 
  
  def render(styles: NodeSeq): NodeSeq = {
    
     val msgs: (String) => NodeSeq = (id) => {
       val f = messagesById(id) _
       List((f(S.errors), attr("errorClass")),
               (f(S.warnings), attr("warningClass")),
               (f(S.notices), attr("noticeClass"))).flatMap {
                 case (msg, style) =>
                   msg.toList match {
                     case Nil => Nil
                     case msgList => style match {
                       case Full(s) => msgList flatMap (t => <span>{t}</span> % ("class" -> s)) 
                       case _ => msgList flatMap ( n => n )
                     }
                   }
       }
    }
    
    attr("id") match {
      case Full(id) => <span>{msgs(id)}</span> % ("id" -> id)
      case _ => NodeSeq.Empty
    }
    
  }
}
