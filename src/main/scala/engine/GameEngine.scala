package main.scala.engine

import main.scala.architecture.Engine
import main.scala.event._
import akka.actor.Actor
import main.scala.tools.ActorsInterface
import ogl.app.StopWatch
import main.scala.tools.{GameConsole, DC}
import org.lwjgl.opengl.GL11._
import main.scala.systems.input.{Input, SimulationContext}
import org.lwjgl.opengl.{PixelFormat, GL11, DisplayMode, Display}
import main.scala.io.EntityDescLoader
import main.scala.math.Mat4f
import main.scala.systems.gfx.{Shader, Mesh}

/**
 * Created by Christian Treffs
 * Date: 14.03.14 18:34
 */
object GameEngine extends Engine with Actor with ActorsInterface {

  // set debug level
  DC.debugLevel = 0

  private var assetsDir: String = null
  private var gameTitle:String = null
  private var entitiesDir:String = null
  private var shaderDir:String = null
  private var meshesDir:String = null

  private val width = 800
  private val height = 600
  private val prefFPS = 100
  private val FOV = 60f
  private val nearPl = 0.1f
  private val farPl = 100f
  private val multiSampling = false
  private val vSyncEnabled = true
  private var prefferedFPS: Int = -1
  private var fieldOfView: Float = -1
  private var nearPlane: Float = -1
  private var farPlane: Float = -1

  //TODO: remove?
  //var entities: SimulationRegistry = null
  var simulationContext: SimulationContext = null
  var time: StopWatch = null



  override def createNewGame(title: String, assetsPath: String = "src/main/resources") = {

    gameTitle = title
    assetsDir = assetsPath

    entitiesDir = assetsDir+"/entities"
    shaderDir = assetsDir+"/shaders"
    meshesDir = assetsDir+"/meshes"

    this

  }

  override def start(): Engine = {
    // init the display
    initDisplay(gameTitle, width, height, FOV, nearPl, farPl, prefFPS,vSyncEnabled, multiSampling)

    // init openGL
    initGL()

    // init game
    initGame()

    //the main game loop
    gameLoop()

    this
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

  def initGame(): Unit = {

    // init a default shader
    val defaultShader = Shader.init()

    // set the default shader as default for all the meshes
    Mesh.defaultShader(defaultShader)

    /*
    // load all stuff
    val colladaFiles = Map[Symbol, String](
      'SkyBox         -> "src/main/resources/meshes/SkyBox/SkyBox.dae",
      'CompanionCube  -> "src/main/resources/meshes/CompanionCube/CompanionCube.dae",
      'Tank           -> "src/main/resources/meshes/T-90/T-90.dae",
      'PhoneBooth     -> "src/main/resources/meshes/PhoneBooth/PhoneBooth.dae",
      'Roads          -> "src/main/resources/meshes/Roads/Roads.dae"
    )

    // load collada files and create meshes -> now everything is available via Mesh.get()
    Collada.load(colladaFiles)
                                                         */
    EntityDescLoader.load(entitiesDir)


    Input.init()

    time = new StopWatch()


    // CREATE SIMULATION CONTEXT
    simulationContext = new SimulationContext()

    // CREATE ENTITY REGISTRY
    // entities = new SimulationRegistry()

    // ADD INITIAL ENTITIES
    //entities += new Cube("Cube1")
    //entities += new MeshEntity(Mesh.get('Turret))
    //entities += new MeshEntity(Mesh.get('ChassisTread))
    //entities += new MeshEntity(Mesh.get('ChassisBody))


    // INIT PHYSICS


    //INITIALIZE ALL ENTITIES
    //entities.initAll(context)

    // set initial deltaT
    simulationContext.updateDeltaT()
  }


  override protected def gameLoop(): Unit = {
    while (!Display.isCloseRequested) {
      //input.update()
      Display.sync(prefferedFPS)

      Input.update(Display.getWidth, Display.getHeight)

      simulate(time.elapsed)

      display(Display.getWidth, Display.getHeight)

      Display.update()
    }

    shutdown()
  }

  override def shutdown(): Unit = {
    DC.log("Shutting down")
    //TODO: stop thread clean up and end
    Display.destroy()
    DC.log("Program Ended")
    System.exit(0)
  }


  override def receive: Receive = {
    case ComponentAdded(entity) => componentAdded(entity)
    case ComponentRemoved(entity) => //TODO
  }


  //TODO: to be done in the physics system
  def simulate(elapsed: Float): Unit = {

    // INPUT
    // update user input
    //context.updateInput()
    GameConsole.updateInput(elapsed)


    // PHYSICS
    //simulate all entities
    //entities.simulateAll(context)


  }

  //todo: to be done in the gfx system
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

    simulationContext.setProjectionMatrix(Mat4f.projection(fieldOfView, aspect, nearPlane, farPlane))
    simulationContext.setViewMatrix(Mat4f.identity)
    val mat = Mat4f.translation(0,0,-0.8f).mult(Mat4f.rotation(0,1,0, 90f)).mult(Mat4f.scale(0.001f, 0.001f, 0.001f))
    simulationContext.setModelMatrix(mat)
    //Shader.setProjectionMatrix(projectionMatrix)

    // display objects

    //camera.activate

    //cube.display

    // GRAPHICS
    // render all entities
    // entities.renderAll(context)


  }



}

