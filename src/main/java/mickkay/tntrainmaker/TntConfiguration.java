package mickkay.tntrainmaker;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.io.File;
import java.util.List;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import com.google.common.collect.Lists;

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
    config
        .getCategory("dimensions")
        .setComment(
            "For instructions see http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2535044");
    loadDimensionsRestrictions();
    config.save();
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

  private void loadDimensionsRestrictions() {
    Property property =
        config.get("dimensions", "restrictions", new String[] {"ALLOW *", "DENY -1"});
    property.comment = "TNT rain is restricted to only work for the following dimensions";
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
    throw new IllegalArgumentException(
        String
            .format(
                "Bad dimensions configuration in file %s. ALLOW * and DENY * are only allowed on first line!",
                file.getAbsolutePath()));
  }

  private void throwCantParseLine(String line) {
    throw new IllegalArgumentException(String.format(
        "Bad dimensions configuration. Can't parse '%s'", line));
  }

  private List<Integer> parseIntArray(String value) {
    String[] groups = value.split(",");
    List<Integer> result = Lists.newArrayList();
    for (String g : groups) {
      result.add(Integer.parseInt(g));
    }
    return result;
  }

}
