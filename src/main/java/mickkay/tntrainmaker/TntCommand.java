package mickkay.tntrainmaker;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class TntCommand implements ICommand {

  private static final String ON = "on";
  private static final String OFF = "off";
  private static final String SET = "set";
  private static final String CHANCE = "chance";
  private static final String AREA = "area";
  private static final String DROPS = "drops";

  private List<String> aliases = new ArrayList<String>();

  public TntCommand() {
    aliases.add("tnt");
  }

  @Override
  public String getName() {
    return "tnt";
  }

  @Override
  public String getCommandUsage(ICommandSender sender) {
    return "tnt [ on [player]| off [player]| set drops <number> | set area <number> | set chance <number>]";
  }

  @Override
  public List getAliases() {
    return aliases;
  }

  @Override
  public boolean canCommandSenderUse(ICommandSender sender) {
    // TODO check is this is sufficient and we can remove the check in execute
    return isCommandAllowed(sender);
  }

  @Override
  public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_) {
    return false;
  }

  @Override
  public int compareTo(Object arg0) {
    return 0;
  }

  boolean isCommandAllowed(ICommandSender sender) {
    boolean isOp = sender.canUseCommand(2, "");
    return isOp;
  }

  @Override
  public void execute(ICommandSender sender, String[] args) throws CommandException {
    MinecraftServer server = MinecraftServer.getServer();
    // check if we can remove this check because we implemented canCommandSenderUse correctly
    if (!isCommandAllowed(sender)) {
      throw new CommandException("commands.generic.permission", args[0]);
    }

    if (args.length == 0) {
      replyToSender(sender, "Missing tnt command argument!");
      return;
    }
    String command = args[0];
    if (ON.equals(command)) {
      performOn(sender, subarray(args, 1));
    } else if (OFF.equals(command)) {
      performOff(sender, subarray(args, 1));
    } else if (SET.equals(command)) {
      performSet(sender, subarray(args, 1));
    } else {
      replyToSender(sender, "Unknown tnt argument %s!", command);
    }
  }

  private void replyToSender(ICommandSender sender, String message, Object... args) {
    sender.addChatMessage(new ChatComponentText(String.format(message, args)));
  }

  private void performSet(ICommandSender sender, String[] args) throws CommandException {
    if (args.length == 0) {
      performShowSetting(sender);
      return;
    }
    String property = args[0];
    if (DROPS.equals(property)) {
      performSetDrops(sender, subarray(args, 1));
    } else if (AREA.equals(property)) {
      performSetArea(sender, subarray(args, 1));
    } else if (CHANCE.equals(property)) {
      performSetChance(sender, subarray(args, 1));
    } else {
      replyToSender(sender, "Unknown property %s!", property);
    }
  }

  private void performShowSetting(ICommandSender sender) {
    TntRain rain = TntRainmaker.instance.getTntRain();
    int drops = rain.getDrops();
    int area = rain.getSize();
    int chance = rain.getChance();
    replyToSender(sender, String.format("%s settings: drops=%d, area=%d, chance=%d",
        TntRainmaker.NAME, drops, area, chance));
  }

  private void performSetChance(ICommandSender sender, String[] args) throws CommandException {
    if (args.length == 0) {
      throw new CommandException("commands.generic.usage", "/tnt set chance <number>");
    }
    try {
      int number = Integer.parseInt(args[0]);
      if (number < 1) {
        throw new CommandException("commands.generic.num.tooSmall", number, 1);
      }
      if (number > 100) {
        throw new CommandException("commands.generic.num.tooBig", number, 100);
      }
      TntRainmaker.instance.getTntRain().setChance(number);
    } catch (NumberFormatException ex) {
      throw new CommandException("commands.generic.num.invalid", args[0]);
    }
  }

  private void performSetArea(ICommandSender sender, String[] args) throws CommandException {
    if (args.length == 0) {
      throw new CommandException("commands.generic.usage", "/tnt set area <number>");
    }
    try {
      int number = Integer.parseInt(args[0]);
      if (number < 1) {
        throw new CommandException("commands.generic.num.tooSmall", number, 1);
      }
      if (number > 20) {
        throw new CommandException("commands.generic.num.tooBig", number, 20);
      }
      TntRainmaker.instance.getTntRain().setSize(number);
    } catch (NumberFormatException ex) {
      throw new CommandException("commands.generic.num.invalid", args[0]);
    }
  }

  private void performSetDrops(ICommandSender sender, String[] args) throws CommandException {
    if (args.length == 0) {
      throw new CommandException("commands.generic.usage", "/tnt set drops <number>");
    }
    try {
      int number = Integer.parseInt(args[0]);
      if (number < 1) {
        throw new CommandException("commands.generic.num.tooSmall", number, 1);
      }
      if (number > 100) {
        throw new CommandException("commands.generic.num.tooBig", number, 100);
      }
      TntRainmaker.instance.getTntRain().setDrops(number);
      replyToSender(sender, "tnt drops is set to %s", number);
    } catch (NumberFormatException ex) {
      throw new CommandException("commands.generic.num.invalid", args[0]);
    }
  }

  private void performOff(ICommandSender sender, String[] args) throws CommandException {
    if (args.length == 0) {
      TntRainmaker.instance.getTntRain().setEnabled(false);
      replyToSender(sender, "tnt is off");
      return;
    }
    List<EntityPlayer> players = findPlayersByName(args, sender.getEntityWorld());
    for (EntityPlayer p : players) {
      TntRainmaker.instance.getTntRain().setEnabled(p, false);
      replyToSender(sender, "tnt is off for " + p.getDisplayNameString());
    }
  }

  private void performOn(ICommandSender sender, String[] args) throws CommandException {
    if (args.length == 0) {
      TntRainmaker.instance.getTntRain().setEnabled(true);
      replyToSender(sender, "tnt is on");
      return;
    }
    List<EntityPlayer> players = findPlayersByName(args, sender.getEntityWorld());
    for (EntityPlayer p : players) {
      TntRainmaker.instance.getTntRain().setEnabled(p, true);
      replyToSender(sender, "tnt is on for " + p.getDisplayNameString());
    }
  }

  @Override
  public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
    if (args.length == 0 || "".equals(args[0])) {
      return asList(SET, ON, OFF);
    }
    String command = args[0];
    if (SET.equals(command)) {
      if (args.length == 1 || "".equals(args[1])) {
        return asList(DROPS, AREA, CHANCE);
      }
      String arg = args[1];
      if (DROPS.equals(arg)) {
        return asList("1", "8", "20", "40");
      }
      if (AREA.equals(arg)) {
        return asList("1", "4", "8", "20");
      }
      if (CHANCE.equals(arg)) {
        return asList("1", "10", "25", "50", "100");
      }
      if (arg.startsWith("d")) {
        return asList(DROPS);
      }
      if (arg.startsWith("a")) {
        return asList(AREA);
      }
      if (arg.startsWith("c")) {
        return asList(CHANCE);
      }
      return null;
    }
    if (command.equals(ON) || command.equals(OFF)) {
      World world = sender.getCommandSenderEntity().worldObj;
      String name = getLast(subarray(args, 1));
      List<String> names = findPlayerNamesStartWith(name, world);
      return names;
    }
    if (command.startsWith("s")) {
      return asList(SET);
    }
    if (command.startsWith("o")) {
      return asList(ON, OFF);
    }
    if (command.startsWith("of")) {
      return asList(OFF);
    }
    return null;
  }

  private List<String> findPlayerNamesStartWith(String name, World world) {
    List<String> result = newArrayList();
    for (EntityPlayer p : (List<EntityPlayer>) world.playerEntities) {
      if (p != null && p.getDisplayNameString().toLowerCase().startsWith(name.toLowerCase())) {
        result.add(p.getDisplayNameString());
      }
    }
    return result;
  }

  private List<EntityPlayer> findPlayersByName(String[] names, World world) throws CommandException {
    List<EntityPlayer> result = newArrayList();
    nameloop: for (String name : names) {
      for (EntityPlayer p : (List<EntityPlayer>) world.playerEntities) {
        if (p != null && name.equalsIgnoreCase(p.getDisplayNameString())) {
          result.add(p);
          continue nameloop;
        }
      }
      throw new CommandException("Player " + name + " cannot be found", name);
    }
    return result;
  }

  private String[] subarray(String[] source, int start) {
    int len = source.length - start;
    if (len < 0) {
      return new String[0];
    }
    String[] result = new String[len];
    System.arraycopy(source, start, result, 0, len);
    return result;
  }

  private String getLast(String[] array) {
    if (array.length == 0) {
      return null;
    }
    return array[array.length - 1];
  }

  private List asList(String... elems) {
    return Arrays.asList(elems);
  }

}
