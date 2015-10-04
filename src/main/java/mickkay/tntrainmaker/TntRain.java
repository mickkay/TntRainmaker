package mickkay.tntrainmaker;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class TntRain {
  private final Map<Integer, TntCloud> map = new HashMap<Integer, TntCloud>();
  private int drops = 4;
  private int size = 8;
  private int chance = 20;
  private boolean enabled = false;

  public void setDrops(int drops) {
    this.drops = drops;
  }

  public int getDrops() {
    return drops;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public int getSize() {
    return size;
  }

  public int getChance() {
    return chance;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setChance(int chance) {
    this.chance = chance;
  }

  public void setEnabled(boolean b) {
    this.enabled = b;
    for (TntCloud cloud : map.values()) {
      cloud.setEnabled(b);
    }
  }

  public void setEnabled(EntityPlayer player, boolean enabled) {
    TntCloud cloud = getCloud(player);
    cloud.setEnabled(enabled);
  }

  public void onTick(World world, EntityPlayer player) {
    TntCloud cloud = getCloud(player);
    cloud.handle(world, player);
  }

  private TntCloud getCloud(EntityPlayer player) {
    TntCloud result = map.get(player.getEntityId());
    if (result == null) {
      result = new TntCloud(enabled);
      map.put(player.getEntityId(), result);
    }
    return result;
  }

  private class TntCloud {

    private static final double WIDTH = 16.0;
    private static final double LENGTH = 16.0;

    private long lastTry = 0;
    private long minPause = 10;
    private boolean enabled;

    public TntCloud(boolean enabled) {
      this.enabled = enabled;
    }

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public void handle(World world, EntityPlayer player) {
      if (!enabled || player.isDead) {
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

        // int y = world.getHeightValue(x, z) + 72;
        BlockPos pos = new BlockPos(x, 0, z);
        int y = world.getTopSolidOrLiquidBlock(pos).getY() + 72;


        EntityTNTPrimed tnt = new EntityTNTPrimed(world, x, y, z, null);
        world.spawnEntityInWorld(tnt);
      }
    }
  }


}
