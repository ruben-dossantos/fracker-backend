package controllers

import play.api._
import play.api.mvc._
import argonaut.Argonaut._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def getUsers = Action {
    Ok("{'name': 'ruben', password: '30vinte'}").as("application/json")
  }

}