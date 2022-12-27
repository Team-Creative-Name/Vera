/*
 * Vera - a common library for all of TCN's discord bots.
 *
 * Copyright (C) 2022 Thomas Wessel and the rest of Team Creative Name
 *
 *
 * This library is licensed under the GNU Lesser General Public License v2.1
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 *
 *
 * For more information, please check out the original repository of this project on github
 * https://github.com/Team-Creative-Name/Vera
 */
package com.tcn.vera.commands;

import com.tcn.vera.utils.CommandCache;
import gnu.trove.set.TLongSet;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * This is the command template class. Any command executed via Vera should extend this class in some fashion. This command
 * template is designed to be passed to the {@link com.tcn.vera.eventHandlers.CommandHandlerBuilder} and executed
 * via the {@link com.tcn.vera.eventHandlers.CommandHandler} object build by said builder.
 * <br><br>
 * The constructor of this class is responsible for setting many of the command's properties. An example is as follows:
 * <pre>{@code
 *    public CommandTest(){
 *         this.aliases = new String[]{"one", "two", "three"};
 *         this.allowChatCommand = true;
 *         this.allowSlashCommand = true;
 *         this.commandName = "CommandTest";
 *         this.isOwnerCommand = true;
 *         this.chatHelp = "This command does a thing";
 *         this.slashHelp = "This command does something slightly differently";
 *         this.slashCommand = Commands.slash(getCommandName(), "a slash command example")
 *                 .addOption(OptionType.INTEGER, "number", "a random numer", true);
 *     }
 * }</pre>
 *
 * Please see the javadoc for each individual variable above to see what exactly they do.
 *
 */
public abstract class CommandTemplate {

   /**
    * The name of the command. This is what is used to call both the text and slash version of the command. It is also
    * used as the name of the command in the builtin help command.
    */
   protected String commandName = "null";

   /**
    * The string that the builtin help command will display when called as a chat command.
    */
   protected String chatHelp = "No help provided for this command!";

   /**
    * The string that the builtin help command will display when called as a slash command.
    */
   protected String slashHelp = "No help provided for this command!";

   /**
    * A string array of aliases that can be used instead of the official name when invoking the command. Please note that
    * these aliases only work for the chat version of the command.
    */
   protected String[] aliases = new String[0];

   /**
    * This boolean sets if the command should be usable to only the owner(s) of the bot. If no owner is set, it will default
    * to owner or organization in the discord developer portal. <br><br>
    * Please note that this only works with chat commands. If you wish to set permissions on a slash command, you must use
    * the {@link net.dv8tion.jda.api.interactions.commands.build.SlashCommandData#setDefaultPermissions(DefaultMemberPermissions)}
    * method when building your slash command.
    */
   protected boolean isOwnerCommand = false;

   /**
    * This boolean sets if the command should be invokable via a slash command. A slash command is Discord's new method of
    * interfacing with bots via typing a forward slash into the chat box.
    */
   protected boolean allowSlashCommand = true;

   /**
    * This boolean sets if the command should be invokable via chat. This means typing something like "!gif reverse" into
    * discord to invoke the command.
    */
   protected boolean allowChatCommand = true;

   /**
    * If <code>allowSlashCommand == true</code>, this is what is passed to discord to show in the slash command GUI. If you
    * do not require any special arguments, Vera will automatically generate a slash command for you and this can be left
    * as null.
    */
   protected CommandData slashCommand = null;

   private static final CommandCache<Long, TLongSet> MESSAGE_LINK_MAP = new CommandCache<>(20);

   /**
    * The actual body of the command to be executed when invoked via typing the prefix and command name into discord.
    * If no functionality is required, it can be left empty.
    * @param event
    *    The {@link MessageReceivedEvent} generated by JDA when the command is invoked.
    * @param message
    *    The {@link Message} object that contains all information about the message in which the command was invoked.
    * @param content
    *    The message send to discord stripped of the prefix and the command name. This *should* be safe to use directly as your
    *    command arguments.
    */
   public abstract void executeChatCommand(MessageReceivedEvent event, Message message, String content);

   /**
    * The method executed when this command is invoked via the builtin slash command feature in discord. If no functionality
    * for this type of command is desired, ensure the constructor sets <code>allowSlashCommand = false</code> and leave this method
    * blank.
    * @param event
    *    The {@link SlashCommandInteractionEvent} generated via JDA when this command is invoked.
    */
   public abstract void executeSlashCommand(SlashCommandInteractionEvent event);

   public boolean isAllowChatCommand() {
      return allowChatCommand;
   }

   public boolean isAllowSlashCommand() {
      return allowSlashCommand;
   }

   public String getCommandName(){
      return commandName;
   }

   public String[] getAliases(){
      return aliases;
   }

   /**
    * Gathers the command name and aliases and returns it as an array list.
    * @return
    *    An arraylist containing all names the command can be invoked with.
    */
   public ArrayList<String> getAllCommandNames(){
      ArrayList<String> array = new ArrayList<>(Arrays.asList(getAliases()));
      array.add(getCommandName());
      return array;
   }

   /**
    * If <code>allowSlashCommand == true</code>, this returns the slashCommand field. If it was not created, it will generate
    * a slash command via the commandName and slashHelp fields. This returns null if <code>allowSlashCommand == false</code>
    * @return
    *    A commandData object if enabled or null if not.
    */
   public CommandData getSlashCommand(){
      if(isAllowSlashCommand()){
         return Objects.requireNonNullElseGet(slashCommand, () -> Commands.slash(getCommandName(), slashHelp));
      }
      return null;
   }

   public boolean isOwnerCommand(){
      return isOwnerCommand;
   }
}