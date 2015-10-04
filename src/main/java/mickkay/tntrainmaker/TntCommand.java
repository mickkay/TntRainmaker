package mickkay.tntrainmaker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

public class TntCommand implements ICommand {

  private List<String> aliases = new ArrayList<String>();

  public TntCommand() {
    aliases.add("tnt");
  }

  @Override
  public String getCommandName() {
    return "tnt";
  }

  @Override
  public String getCommandUsage(ICommandSender sender) {
    return "tnt [ on [player]| off [player]| set drops <number> | set area <number> | set chance <number>]";
  }

  @Override
  public List getCommandAliases() {
    return aliases;
  }

  boolean isCommandAllowed(ICommandSender sender) {
    boolean isOp = sender.canCommandSenderUseCommand(2, "");
    return isOp;
  }

  @Override
  public void processCommand(ICommandSender sender, String[] args) {
    MinecraftServer server = MinecraftServer.getServer();
    if (!isCommandAllowed(sender)) {
      throw new CommandException("commands.generic.permission", args[0]);
    }

    if (args.length == 0) {
      replyToSender(sender, "Missing tnt command argument!");
      return;
    }
    String command = args[0];
    if ("on".equals(command)) {
      performOn(sender, subarray(args, 1));
    } else if ("off".equals(command)) {
      performOff(sender, subarray(args, 1));
    } else if ("set".equals(command)) {
      performSet(sender, subarray(args, 1));
    } else {
      replyToSender(sender, "Unknown tnt command %s!", command);
    }
  }

  private void replyToSender(ICommandSender sender, String message, Object... args) {
    sender.addChatMessage(new ChatComponentText(String.format(message, args)));
  }

  private void performSet(ICommandSender sender, String[] args) {
    if (args.length == 0) {
      replyToSender(sender, "Missing property!");
      return;
    }
    String property = args[0];
    if ("drops".equals(property)) {
      performSetDrops(sender, subarray(args, 1));
    } else if ("area".equals(property)) {
      performSetArea(sender, subarray(args, 1));
    } else if ("chance".equals(property)) {
      performSetChance(sender, subarray(args, 1));
    } else {
      replyToSender(sender, "Unknown property %s!", property);
    }
  }

  private void performSetChance(ICommandSender sender, String[] args) {
    if (args.length == 0) {
      replyToSender(sender, "Missing number value!");
      return;
    }
    try {
      int number = Integer.parseInt(args[0]);
      if (number < 1) {
        throw new CommandException("commands.generic.num.tooSmall", number, 1);
      }
      if (number > 100) {
        throw new CommandException("commands.generic.num.tooBig", number, 100);
      }
      TntRainmaker.instance.getTntAirRaidEffect().setChance(number);
    } catch (NumberFormatException ex) {
      throw new CommandException("commands.generic.num.invalid", args[0]);
    }
  }

  private void performSetArea(ICommandSender sender, String[] args) {
    if (args.length == 0) {
      replyToSender(sender, "Missing number value!");
      return;
    }
    try {
      int number = Integer.parseInt(args[0]);
      if (number < 1) {
        throw new CommandException("commands.generic.num.tooSmall", number, 1);
      }
      if (number > 20) {
        throw new CommandException("commands.generic.num.tooBig", number, 20);
      }
      TntRainmaker.instance.getTntAirRaidEffect().setSize(number);
    } catch (NumberFormatException ex) {
      throw new CommandException("commands.generic.num.invalid", args[0]);
    }
  }

  private void performSetDrops(ICommandSender sender, String[] args) {
    if (args.length == 0) {
      replyToSender(sender, "Missing number value!");
      return;
    }
    try {
      int number = Integer.parseInt(args[0]);
      if (number < 1) {
        throw new CommandException("commands.generic.num.tooSmall", number, 1);
      }
      if (number > 100) {
        throw new CommandException("commands.generic.num.tooBig", number, 100);
      }
      TntRainmaker.instance.getTntAirRaidEffect().setDropsPerDrop(number);
      replyToSender(sender, "tnt drops is set to %s", number);
    } catch (NumberFormatException ex) {
      throw new CommandException("commands.generic.num.invalid", args[0]);
    }
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


  private void performOff(ICommandSender sender, String[] args) {
    if (args.length == 0) {
      TntRainmaker.instance.getTntAirRaidEffect().setEnabled(false);
      replyToSender(sender, "tnt is off");
      return;
    }
    for (String name : args) {
      for (EntityPlayer p : (List<EntityPlayer>) sender.getEntityWorld().playerEntities) {
        if (name.equalsIgnoreCase(p.getDisplayName())) {
          TntRainmaker.instance.getTntAirRaidEffect().setEnabled(p, false);
          replyToSender(sender, "tnt is off for player " + name);
          return;
        }
      }
      replyToSender(sender, "player " + name + " not found");
    }
  }

  private void performOn(ICommandSender sender, String[] args) {
    if (args.length == 0) {
      TntRainmaker.instance.getTntAirRaidEffect().setEnabled(true);
      replyToSender(sender, "tnt is on");
      return;
    }
    for (String name : args) {
      for (EntityPlayer p : (List<EntityPlayer>) sender.getEntityWorld().playerEntities) {
        if (name.equalsIgnoreCase(p.getDisplayName())) {
          TntRainmaker.instance.getTntAirRaidEffect().setEnabled(p, true);
          replyToSender(sender, "tnt is on for player " + name);
          return;
        }
      }
      replyToSender(sender, "player " + name + " not found");
    }
  }

  @Override
  public boolean canCommandSenderUseCommand(ICommandSender sender) {
    return true;
  }

  @Override
  public List addTabCompletionOptions(ICommandSender sender, String[] args) {
    if (args.length == 0 || "".equals(args[0])) {
      return asList("set", "on", "off");
    }
    String command = args[0];
    if ("set".equals(command)) {
      if (args.length == 1 || "".equals(args[1])) {
        return asList("drops", "area", "chance");
      }
      String arg = args[1];
      if ("drops".equals(arg)) {
        return asList("1", "8", "20", "40");
      }
      if ("area".equals(arg)) {
        return asList("1", "4", "8", "20");
      }
      if ("chance".equals(arg)) {
        return asList("1", "10", "25", "50", "100");
      }
      if (arg.startsWith("d")) {
        return asList("drops");
      }
      if (arg.startsWith("a")) {
        return asList("area");
      }
      if (arg.startsWith("c")) {
        return asList("chance");
      }
      return null;
    }
    if (command.startsWith("s")) {
      return asList("set");
    }
    if (command.startsWith("o")) {
      return asList("on", "off");
    }
    if (command.startsWith("of")) {
      return asList("off");
    }
    return null;
  }

  private List asList(String... elems) {
    return Arrays.asList(elems);
  }


  @Override
  public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_) {
    return false;
  }

  @Override
  public int compareTo(Object arg0) {
    return 0;
  }

}
