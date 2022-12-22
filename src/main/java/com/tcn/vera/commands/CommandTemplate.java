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
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * This is the command template class. Any command executed via Vera should extend this class in some fashion.
 * TODO: ADD EXAMPLE CODE THAT SHOWS THE CONSTRUCTOR N STUFF
 */
public abstract class CommandTemplate {

   protected CommandTemplate(String about, String help){

   }

   protected CommandTemplate(){

   }

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
    * to owner or organization in the discord developer portal.
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

   private static final CommandCache<Long, TLongSet> MESSAGE_LINK_MAP = new CommandCache<>(20);


   /**
    * The text command version of {@link com.tcn.vera.commands.CommandTemplate CommandTelplate}.
    * <br>This method may be left blank only if the constructor has the following:
    * <code>this.allowChatCommand = false</code>
    * @param author
    *    The discord user who initiated the command.
    * @param channel
    *    The guild channel where the command was called.
    * @param message
    *    The whole message in which the command was called.
    * @param content
    *    The command message text with the command name and arguments stripped
    * @param event
    *    The message event in which this command was called.
    */
   protected abstract void executeTextCommand(User author, TextChannel channel, Message message, String content, MessageReceivedEvent event);

   /**
    * The slash command version of {@link com.tcn.vera.commands.CommandTemplate CommandTelplate}.
    * <br>This method may be left blank only if the constructor has the following:
    * <code>this.allowSlashCommand = false</code>
    * @param event
    *    The slash command event in which this command was called.
    */
   protected abstract void executeSlashCommand(SlashCommandInteractionEvent event);












}