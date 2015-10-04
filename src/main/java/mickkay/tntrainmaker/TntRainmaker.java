package mickkay.tntrainmaker;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

@Mod(modid = TntRainmaker.ID, name = TntRainmaker.NAME, version = TntRainmaker.VERSION,
    acceptableRemoteVersions = "*")
public class TntRainmaker {

  public static final String NAME = "TNT-Rainmaker";
  public static final String ID = "TntRainmaker";
  public static final String VERSION = "1.7.10-1.2.0";

  public final Logger logger = LogManager.getLogger(TntRainmaker.class.getName());
  private final TntRain rain = new TntRain();

  @Instance(ID)
  public static TntRainmaker instance;

  public String getVersion() {
    return VERSION;
  }

  public TntRain getTntRain() {
    return rain;
  }

  @EventHandler
  public void preInit(FMLPreInitializationEvent event) throws Exception {}

  @EventHandler
  public void init(FMLInitializationEvent event) {
    logger.info("Initializing " + NAME + " mod");
    FMLCommonHandler.instance().bus().register(this);
  }

  @EventHandler
  public void serverLoad(FMLServerStartingEvent event) {
    logger.info("Registering tnt command");
    event.registerServerCommand(new TntCommand());
  }

  @SubscribeEvent
  public void tickEnd(TickEvent.WorldTickEvent evt) {
    if (evt.world.isRemote) {
      return;
    }
    if (evt.type == TickEvent.Type.WORLD && evt.phase == TickEvent.Phase.END) {
      onTick(evt.world);
    }
  }

  private void onTick(World world) {
    List<EntityPlayer> players = world.playerEntities;
    for (EntityPlayer player : players) {
      getTntRain().onTick(world, player);
    }
  }

}
