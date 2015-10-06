package mickkay.tntrainmaker;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = TntRainmaker.ID, name = TntRainmaker.NAME, version = TntRainmaker.VERSION,
    acceptableRemoteVersions = "*")
public class TntRainmaker {

  public static final String NAME = "TNT-Rainmaker";
  public static final String ID = "TntRainmaker";
  public static final String VERSION = "1.8-2.1.0";

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
