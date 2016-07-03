package mickkay.tntrainmaker;

import java.io.File;
import java.util.List;

import com.google.common.collect.Lists;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public final class TntConfiguration {

  private enum DefaultRule {
    ALLOW, DENY
  };

  private final File file;
  private final Configuration config;
  private DefaultRule defaultRule = null;
  private TIntSet allowedDimensionsSet = new TIntHashSet();
  private TIntSet deniedDimensionsSet = new TIntHashSet();

  public TntConfiguration(File file) {
    this.file = file;
    config = new Configuration(file, TntRainmaker.VERSION);
    config.getCategory("defaults")
        .setComment("This configures the default values of in-game settings");
    loadDefaults();
    config.getCategory("dimensions").setComment(
        "For instructions see http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2535044");
    loadDimensions();
    config.save();
  }

  private void loadDefaults() {
    getDefaultAreaProperty();
    getDefaultChanceProperty();
    getDefaultDropsProperty();
  }

  public boolean isAllowedDimension(int dimId) {
    if (defaultRule == DefaultRule.ALLOW) {
      return !deniedDimensionsSet.contains(dimId);
    }
    if (defaultRule == DefaultRule.DENY) {
      return allowedDimensionsSet.contains(dimId);
    }
    if (!allowedDimensionsSet.contains(dimId)) {
      return false;
    }
    return !deniedDimensionsSet.contains(dimId);
  }

  public int getDefaultArea() {
    return getDefaultAreaProperty().getInt();
  }

  public void setDefaultArea(int defaultArea) {
    getDefaultAreaProperty().set(defaultArea);
    config.save();
  }

  public int getDefaultDrops() {
    return getDefaultDropsProperty().getInt();
  }

  public void setDefaultDrops(int defaultDrops) {
    getDefaultDropsProperty().set(defaultDrops);
    config.save();
  }

  public int getDefaultChance() {
    return getDefaultChanceProperty().getInt();
  }

  public void setDefaultChance(int defaultChance) {
    getDefaultChanceProperty().set(defaultChance);
    config.save();
  }

  private void loadDimensions() {
    Property property =
        config.get("dimensions", "restrictions", new String[] {"DENY *", "ALLOW 0"});
    property.setComment("TNT rain is restricted to only work for the following dimensions");
    String[] lines = property.getStringList();

    for (int i = 0; i < lines.length; ++i) {
      String line = lines[i];
      if (line.equalsIgnoreCase("allow *")) {
        if (i > 0) {
          throwAllowAndDenyWildcardAreOnlyAllowdOnFirstLine();
        }
        defaultRule = DefaultRule.ALLOW;
      } else if (line.equalsIgnoreCase("deny *")) {
        if (i > 0) {
          throwAllowAndDenyWildcardAreOnlyAllowdOnFirstLine();
        }
        defaultRule = DefaultRule.DENY;
      } else if (line.toLowerCase().startsWith("allow ")) {
        List<Integer> ints = parseIntArray(line.substring("allow ".length()));
        allowedDimensionsSet.addAll(ints);
        deniedDimensionsSet.removeAll(ints);
      } else if (line.toLowerCase().startsWith("deny ")) {
        List<Integer> ints = parseIntArray(line.substring("deny ".length()));
        deniedDimensionsSet.addAll(ints);
        allowedDimensionsSet.removeAll(ints);
      } else if (line.trim().isEmpty()) {
        // just ignoring empty lines
      } else if (line.startsWith("#")) {
        // just ignoring comments
      } else {
        throwCantParseLine(line);
      }
    }
  }

  private void throwAllowAndDenyWildcardAreOnlyAllowdOnFirstLine() {
    throw new IllegalArgumentException(String.format(
        "Bad dimensions configuration in file %s. ALLOW * and DENY * are only allowed on first line!",
        file.getAbsolutePath()));
  }

  private void throwCantParseLine(String line) {
    throw new IllegalArgumentException(
        String.format("Bad dimensions configuration. Can't parse '%s'", line));
  }

  private List<Integer> parseIntArray(String value) {
    String[] groups = value.split(",");
    List<Integer> result = Lists.newArrayList();
    for (String g : groups) {
      result.add(Integer.parseInt(g));
    }
    return result;
  }

  private Property getDefaultAreaProperty() {
    Property property = config.get("defaults", "area", 8);
    property.setComment(
        "The size of the TNT rain cloud measured in chunks. Default is 8 (which means 8x8 chunks)");
    property.setMinValue(TntRain.MIN_AREA);
    property.setMaxValue(TntRain.MAX_AREA);
    int number = property.getInt();
    return property;
  }

  private Property getDefaultDropsProperty() {
    Property property = config.get("defaults", "drops", 4);
    property.setComment(
        "The number of drops that are spawned together in a single shower. Default is 4");
    property.setMinValue(TntRain.MIN_DROPS);
    property.setMaxValue(TntRain.MAX_DROPS);
    int number = property.getInt();
    return property;
  }

  private Property getDefaultChanceProperty() {
    Property property = config.get("defaults", "chance", 20);
    property.setComment(
        "The probability of a shower, calculated every world tick. Default is 20 (which means 20 percent)");
    property.setMinValue(TntRain.MIN_CHANCE);
    property.setMaxValue(TntRain.MAX_CHANCE);
    int number = property.getInt();
    return property;
  }

}
