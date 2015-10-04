package mickkay.tntrainmaker;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class TntRainEffect {
  private final Map<Integer, Dropper> map = new HashMap<Integer, Dropper>();
  private int drops = 4;
  private int size = 8;
  private int chance = 20;
  private boolean enabled = false;

  public void setDropsPerDrop(int drops) {
    this.drops = drops;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public void setChance(int chance) {
    this.chance = chance;
  }

  public void setEnabled(boolean b) {
    this.enabled = b;
    for (Dropper dropper : map.values()) {
      dropper.setEnabled(b);
    }
  }

  public void setEnabled(EntityPlayer player, boolean enabled) {
    Dropper dropper = getDropper(player);
    dropper.setEnabled(enabled);
  }

  public void onTick(World world, EntityPlayer player) {
    Dropper dropper = getDropper(player);
    dropper.handle(world, player);
  }

  private Dropper getDropper(EntityPlayer player) {
    Dropper result = map.get(player.getEntityId());
    if (result == null) {
      result = new Dropper(enabled);
      map.put(player.getEntityId(), result);
    }
    return result;
  }

  private class Dropper {

    private static final double WIDTH = 16.0;
    private static final double LENGTH = 16.0;

    private long lastTry = 0;
    private long minPause = 10;
    private boolean enabled;

    public Dropper(boolean enabled) {
      this.enabled = enabled;
    }

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public void handle(World world, EntityPlayer player) {
      if (!enabled) {
        return;
      }
      if (lastTry + minPause > world.getTotalWorldTime()) {
        return;
      }
      if (world.rand.nextInt(100) < chance) {
        drop(world, player);
      }
      lastTry = world.getTotalWorldTime();
    }

    private void drop(World world, EntityPlayer player) {
      for (int i = 0; i < drops; ++i) {
        int x = (int) (player.posX + world.rand.nextDouble() * WIDTH * size - WIDTH * size / 2.0);
        int z = (int) (player.posZ + world.rand.nextDouble() * LENGTH * size - LENGTH * size / 2.0);
        int y = world.getHeightValue(x, z) + 72;

        EntityTNTPrimed tnt = new EntityTNTPrimed(world, x, y, z, null);
        world.spawnEntityInWorld(tnt);
      }
    }
  }

}
