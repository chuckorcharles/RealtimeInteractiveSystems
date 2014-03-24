package main.scala.systems.input

import main.scala.tools.{DisplayManager, DC}
import main.scala.math.{Vec3f, Mat4f}
import main.scala.systems.gfx.Shader
import org.lwjgl.input.Keyboard

/**
 * Created by Christian Treffs
 * Date: 10.12.13 11:17
 *
 * the entire game state
 */
class SimulationContext extends Context {

  var shader: Shader = null
  var modelMatrix: Mat4f = Mat4f.identity
  var projectionMatrix: Mat4f = Mat4f.identity
  var viewMatrix: Mat4f = Mat4f.identity
  var displayWidth: Int = 0
  var displayHeight: Int = 0
  var preferredFPS: Int = -1
  var fieldOfView: Float = -1
  var nearPlane: Float = -1
  var farPlane: Float = -1

  private var _deltaT: Long = 0L

  def setProjectionMatrix(mat: Mat4f): Mat4f = {
    projectionMatrix = mat
    projectionMatrix
  }
  def setViewMatrix(mat: Mat4f): Mat4f = {
    viewMatrix = mat
    viewMatrix
  }

  def setModelMatrix(mat: Mat4f): Mat4f = {
    modelMatrix = mat
    modelMatrix
  }

  def updateDeltaT(): Long = {

    _deltaT = 0L

    deltaT
  }

  def deltaT = _deltaT

  def reset() {  }

  def updateInput(){

    //TODO: https://en.wikipedia.org/wiki/Table_of_keyboard_shortcuts

    Input.keyDownOnceDo(Key._1, _ => DisplayManager.toggleFullscreen()) // FULLSCREEN

    /*Input.keyDownDo(Key.ArrowUp,println)
    Input.keyDownDo(Key.ArrowDown,println)
    Input.keyDownDo(Key.ArrowLeft,println)
    Input.keyDownDo(Key.ArrowRight,println)*/

    /*Input.keyDownDo(Key.BackSpace,println)
    Input.keyDownDo(Key.Enter,println)

    Input.keyDownDo(Key.ShiftLeft,println)
    Input.keyDownDo(Key.ShiftRight,println)

    Input.keyDownDo(Key.CommandRight,println)

    Input.keyDownDo(Key.CommandLeft,println)
    Input.keyDownDo(Key.AltLeft,println)
    Input.keyDownDo(Key.CtrlLeft,println)*/

    Input.mouseButtonDown(MouseButton.Left, _ => println("FIRE"))

    Input.keyDownDo(Key._W, _ => println("FORWARD"))
    Input.keyDownDo(Key._A, _ => println("LEFT"))
    Input.keyDownDo(Key._D, _ => println("RIGHT"))
    Input.keyDownDo(Key._S, _ => println("BACKWARD"))

    Input.keyDownOnceDo(Key.Space,_ => println("JUMP"))

    Input.keyDownOnceDo(Key._R, _ => println("RELOAD"))




    //Input.mouseButtonDown(MouseButton.Right,println)
    //Input.mouseButtonDown(MouseButton.Middle,println)


    //println(Keyboard.getEventKey, Keyboard.getKeyName(Keyboard.getEventKey))


  }

}
