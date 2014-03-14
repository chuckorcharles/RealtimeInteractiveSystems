package main.scala.app

import ogl.app.{StopWatch, Input}
import main.scala.tools.DC
import org.lwjgl.opengl.GL11._
import main.scala.world.entities.{MeshEntity, SimulationRegistry, Cube}
import main.scala.input.SimulationContext
import org.lwjgl.LWJGLException
import org.lwjgl.opengl.{PixelFormat, GL11, DisplayMode, Display}
import main.scala.shader.Shader
import main.scala.io.{Collada, Mesh}
import main.scala.math.Mat4f

/**
 * Created by Christian Treffs
 * Date: 19.11.13 16:39
 */

object GameApp {
  def main(args: Array[String]) {


    val app = new GameApp
    app.start()


    /*
  val system = ActorSystem("mySystem")

  implicit val myActor = system.actorOf(Props[MyActor], "myActor")

  myActor ! TestMessage("hallo")

  val sig = new Signal[Int](2)

  val func = {x: Int => }

  myActor ! ObserveSignal(sig)(func)

  sig.update(234)

  sig.update(456)     */

  }
}

class GameApp {
  private var prefferedFPS: Int = -1
  private var fieldOfView: Float = -1
  private var nearPlane: Float = -1
  private var farPlane: Float = -1

  var entities: SimulationRegistry = null
  var context: SimulationContext = null
  var input: Input = null
  var time: StopWatch = null


  def start() {

    // set debug level
    DC.debugLevel = 0

    val width = 800
    val height = 600
    val prefFPS = 100
    val FOV = 60f
    val nearPl = 0.1f
    val farPl = 100f
    val multiSampling = false
    val vSyncEnabled = true

    initDisplay("RIS Game", width, height, FOV, nearPl, farPl, prefFPS,vSyncEnabled, multiSampling)
      initGL()
      initApp()

      //the game loop
      loop()
  }

  def initDisplay(title: String, width: Int, height: Int, fov: Float, nP: Float, fP: Float, fps: Int, vSync: Boolean = true, multiSampling: Boolean = false) = {
    val dm = new DisplayMode(width, height)

    Display.setDisplayMode(dm)
    Display.setTitle(title)

    if (multiSampling) Display.create(new PixelFormat().withSamples(8))
    else Display.create()


    Display.setVSyncEnabled(vSync)
    prefferedFPS = fps
    Display.setSwapInterval(1)
    fieldOfView = fov
    nearPlane = nP
    farPlane = fP

  }

  def initGL() = {
    GL11.glViewport(0, 0, Display.getWidth, Display.getHeight) //TODO: necessary?

    GL11.glMatrixMode(GL11.GL_PROJECTION_MATRIX)
    GL11.glLoadIdentity()

    //GL11.glOrtho(0, Display.getWidth(), 0, Display.getHeight(), 1, -1);
    GL11.glOrtho(0, Display.getWidth, Display.getHeight, 0, 1, -1)

    GL11.glMatrixMode(GL11.GL_MODELVIEW)
    GL11.glLoadIdentity()

    GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)  // Black
  }

  def initApp(): Unit = {

    // init a default shader
    val defaultShader = Shader.init()

    // set the default shader as default for all the meshes
    Mesh.defaultShader(defaultShader)

    // load all stuff
    val colladaFiles = Map[Symbol, String](
      'SkyBox         -> "src/main/resources/SkyBox/SkyBox.dae",
      'CompanionCube  -> "src/main/resources/CompanionCube/CompanionCube.dae",
      'Tank           -> "src/main/resources/T-90/T-90.dae",
      'PhoneBooth     -> "src/main/resources/PhoneBooth/PhoneBooth.dae",
      'Roads          -> "src/main/resources/Roads/Roads.dae"
    )

    // load collada files and create meshes -> now everything is available via Mesh.get()
    Collada.load(colladaFiles)



    input = new Input
    time = new StopWatch()


    // CREATE SIMULATION CONTEXT
    context = new SimulationContext()

    // CREATE ENTITY REGISTRY
    entities = new SimulationRegistry()

    // ADD INITIAL ENTITIES
    //entities += new Cube("Cube1")
    entities += new MeshEntity(Mesh.get('CompanionCube))


    // INIT PHYSICS


    //INITIALIZE ALL ENTITIES
    entities.initAll(context)

    // set initial deltaT
    context.updateDeltaT()
  }


  def loop() {
    while (!Display.isCloseRequested) {
      //input.update()
      Display.sync(prefferedFPS)

      input.setWindowSize(Display.getWidth, Display.getHeight)

      simulate(time.elapsed, input)

      display(Display.getWidth, Display.getHeight)

      Display.update()
    }

    close()
  }

  def simulate(elapsed: Float, input: Input): Unit = {

    // INPUT
    // update user input
    context.updateInput(input)


    // PHYSICS
    //simulate all entities
    entities.simulateAll(context)


  }

  def display(width: Int, height: Int): Unit = {
    // Adjust the the viewport to the actual window size. This makes the
    // rendered image fill the entire window.
    glViewport(0, 0, width, height)

    // Clear all buffers.
    glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT)



    //shader.activate

    // Assemble the transformation matrix that will be applied to all
    // vertices in the vertex shader.
    val aspect: Float = width.asInstanceOf[Float] / height.asInstanceOf[Float]

    // The perspective projection. Camera space to NDC.

    context.setProjectionMatrix(Mat4f.projection(fieldOfView, aspect, nearPlane, farPlane))
    context.setViewMatrix(Mat4f.identity)
    context.setModelMatrix(Mat4f.translation(0,0,-6f) * Mat4f.rotation(1 ,1,0, 45f))
    //Shader.setProjectionMatrix(projectionMatrix)

    // display objects

    //camera.activate

    //cube.display

    // GRAPHICS
    // render all entities
   entities.renderAll(context)


  }

  def close() {
    DC.log("Shutting down")
    //TODO: stop thread clean up and end
    Display.destroy()
    DC.log("Program Ended")
    System.exit(0)
  }
}