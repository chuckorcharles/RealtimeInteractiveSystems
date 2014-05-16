package main.scala.engine

import main.scala.architecture.Engine
import ogl.app.StopWatch
import main.scala.tools.{DisplayManager, DC}
import main.scala.systems.input._
import org.lwjgl.opengl.{PixelFormat, GL11, Display}
import main.scala.io.EntityTemplateLoader
import main.scala.math.Vec3f
import main.scala.systems.gfx.{CameraSystem, RenderingSystem, Shader, Mesh}
import main.scala.entities.Entity
import main.scala.components.{Motion, Placement}
import main.scala.event._
import main.scala.components.CamControl
import main.scala.event.EntityRemoved
import main.scala.systems.input.Triggers
import main.scala.components.Camera
import main.scala.event.EntityCreated
import main.scala.systems.physics.{PhysicsSystem, CollisionSystem}

/**
 * Created by Christian Treffs
 * Date: 14.03.14 18:34
 */
object GameEngine extends Engine with EventReceiver{

  EventDispatcher.subscribe(classOf[Event])(this)
  // set debug level
  DC.debugLevel = 3

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
  private var preferredFPS: Int = -1
  private var fieldOfView: Float = -1
  private var nearPlane: Float = -1
  private var farPlane: Float = -1
  private var fps: Float = 0.0f /** frames per second */
  private var lastFPS: Long = -1 /** last fps time */


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
    DC.logT('engineStartup,"Engine", "starting up", 3)
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
    /*val dm = new DisplayMode(width, height)

    Display.setDisplayMode(dm)*/
    DisplayManager.setDisplayMode(width,height,fullscreen = false)
    Display.setResizable(true)

    setGameTitle()

    if (multiSampling) Display.create(new PixelFormat().withSamples(8))
    else Display.create()


    Display.setVSyncEnabled(vSync)
    preferredFPS = fps
    Display.setSwapInterval(1)
    fieldOfView = fov
    nearPlane = nP
    farPlane = fP

    DC.log("Display", "initialized@"+width+"x"+height, 3)
  }

  def initGL() = {

    /*
    GL11.glMatrixMode(GL11.GL_PROJECTION_MATRIX)
    GL11.glLoadIdentity()

    val topClippingPlane    = 0
    val leftClippingPlane   = 0
    val rightClippingPlane  = Display.getWidth
    val bottomClippingPlane = Display.getHeight

    GL11.glOrtho(leftClippingPlane,rightClippingPlane, bottomClippingPlane, topClippingPlane, nearPlane, farPlane)

    GL11.glMatrixMode(GL11.GL_MODELVIEW)
    GL11.glLoadIdentity()

    GL11.glViewport(0, 0, Display.getWidth, Display.getHeight) //TODO: necessary?*/
    GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)  // Black

    DC.log("OpenGL", "initialized", 3)
  }

//  private val _entities: mutable.HashMap[Class[_ <: architecture.Entity], architecture.Entity] = _

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
    val shaders = Shader.load(shaderDir) //TODO: remove?

    EntityTemplateLoader.load(entitiesDir)

    //creating SkyBox
    Entity.newInstanceOf('SkyBox)

    // creating Tank
    //Entity.newInstanceOf('Tank)

    // creating Camera
    val camEntity = Entity.newInstanceOf('Camera)

    /*val camEntity = Entity.create("Camera")
    val cam = new Camera(90)
    val camPos = new Placement(Vec3f(0,0,0),Vec3f(0,0,0))
    */

    val camCon = new CamControl(Triggers(Key._W),Triggers(Key._S),Triggers(Key._A),Triggers(Key._D),
      Triggers(Key.ArrowUp,null,MouseMovement.MovementY), Triggers(Key.ArrowDown,null,MouseMovement.MovementY),
      Triggers(Key.ArrowLeft,null, MouseMovement.MovementX), Triggers(Key.ArrowRight,null,MouseMovement.MovementX), Triggers(Key.Space, null, null), Triggers(Key.CtrlLeft,null,null))

    val motion = new Motion()
    camEntity.add(camCon)
    camEntity.add(motion)
    /*camEntity.add(cam)
    camEntity.add(camPos)*/

    //register systems with engine
    add(new CameraSystem)
    add(new CamControlSystem)
    add(new RenderingSystem)
    add(new CollisionSystem)
    add(new PhysicsSystem)

    Input.init()

    time = new StopWatch()


    // CREATE SIMULATION CONTEXT
    simulationContext = new SimulationContext()

    //FPS
    lastFPS = System.currentTimeMillis()

    // set initial deltaT
    simulationContext.updateDeltaT()


    DC.log("Game","initialized",3)
  }




  override protected def gameLoop(): Unit = {
    DC.logT('engineStartup,"Engine", "initialized", 3)
    while (!Display.isCloseRequested) {
      Display.sync(preferredFPS) //needs to be first

      Input.update() // needs to be before the context update because context depends on fresh key/mouse input?!

      updateContext() // update the context

      //update all systems with sim-context
      updateSystems(simulationContext)

      updateFPS() // update FPS Counter

      Display.update() // show changes
    }

    shutdown()
  }

  override def shutdown(): Unit = {
    DC.logT('engineShutdown,"Engine","shutting down",3)
    //TODO: stop thread clean up and end

    //systems.values.foreach(system => system.deinit()) // shut down all systems

    Display.destroy()
    DC.logT('engineShutdown,"Engine","ended",3)
    System.exit(0)
  }


  /*override def receive: Receive = {
    case ComponentAdded(entity) => componentAdded(entity)
    case ComponentRemoved(entity) => //TODO
  }
*/

  //TODO: to be done in the physics system
  def simulate(elapsed: Float): Unit = {

    // INPUT
    // update user input
    //simulationContext.updateInput()

    // PHYSICS
    //simulate all entities
    //entities.simulateAll(context)


  }


  //todo: move variables to context
  def updateContext(): Unit = {

    simulationContext.displayHeight = Display.getHeight
    simulationContext.displayWidth = Display.getWidth
    simulationContext.preferredFPS = preferredFPS
    simulationContext.fieldOfView = fieldOfView
    simulationContext.nearPlane = nearPlane
    simulationContext.farPlane = farPlane
    simulationContext.updateDeltaT(time.elapsed())
   simulationContext.updateInput()
  }


  def setGameTitle(fps: Float = 0, name: String = gameTitle, w: Int = Display.getWidth, h: Int = Display.getHeight) {
    Display.setTitle(name +" @ "+w+"x"+h+" "+ fps +" fps")
  }


  /**
   * Calculate the FPS and set it in the title bar
   *
   * http://www.lwjgl.org/wiki/index.php?title=LWJGL_Basics_4_(Timing)#Calculating_FPS
   */
  def updateFPS() {
    if (System.currentTimeMillis() - lastFPS > 1000) {
      setGameTitle(fps)
      fps = 0 //reset the FPS counter
      lastFPS += 1000 //add one second
    }
    fps = fps +1
  }

  override def receive(event: Event): Unit = {
    event match {
      case ec: EntityCreated => add(ec.ent)
      case er: EntityRemoved => remove(er.ent)
      case _ =>

    }
  }
}

