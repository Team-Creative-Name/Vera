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

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is the chat command template class. Any command executed by typing the bot prefix followed by a command name should
 * be extended from this class. Please note that there is a limit imposed by discord where any bots that wish to use this
 * type of command *must* have the message content intent. To learn more about this, please see
 * <a href="https://jda.wiki/using-jda/gateway-intents-and-member-cache-policy/#gateway-intents">JDA's excellent writeup</a>
 * for more information about these gateway intents.
 * <p>
 * Once you have built a command based upon this class, you simply need to pass it to an instance of the Vera {@link com.tcn.vera.eventHandlers.CommandHandlerBuilder}
 * and then add that as an event listener in the {@link net.dv8tion.jda.api.JDABuilder}. Unlike other command types,
 * there are no imposed limits on the number of chat commands that can be registered at a time. For example:
 * <blockquote><pre>
 *     CommandHandler veraHandler = new CommandHandlerBuilder()
 *                 .addCommand(new exampleChatCommand())  //<--- the command that we want to register
 *                 .build();
 *
 *     JDA exampleBot = JDABuilder
 *                 .createDefault("DISCORD_TOKEN")
 *                 .enableIntents(GatewayIntent.MESSAGE_CONTENT)
 *                 .addEventListeners(veraHandler)
 *                 .build();
 * </pre></blockquote>
 * This template has a number of fields that you may set to customize your command. While none of these values are required,
 * it is important to note that the commandName field must be unique amongst any other command names. If any fields are ignored,
 * they will be set to their default values. Please see the individual javadocs for more information about what each field does.
 * Here is an example of a constructor with every field set.
 * <blockquote><pre>
 *     public ExampleChatCommand(){
 *         this.commandName = "ExampleChatCommand";
 *         this.aliases = new String[] {"one", "two", "three"};
 *         this.help = "This command does a thing!";
 *         this.isOwnerCommand = false;
 *     }
 * </pre></blockquote>
 *
 * @author Thomas Wessel
 * @since 1.0
 */
public abstract class ChatCommandTemplate extends CommandTemplateBase {

    /**
     * Any additional names that you might want a command to go by. For example, a help command might also be invoked by
     * the initial "h" or common misspellings like "hep" or "hlp".
     * <p>
     * Because these are just alternate names for the command, they follow the same restrictions as the commandName field.
     * Each name and alias must be unique amongst every registered chat command. This prevents ambiguities when users attempt
     * to invoke commands.
     * <p>
     * If no value is set for this field, then only the command name can be used to invoke the command.
     * The default value is: <pre>new String[0]</pre>
     */
    protected String[] aliases = new String[0];

    /**
     * sets if the command should only be invokable by the owner of the bot. The owner(s) of the bot can be set via the
     * {@link com.tcn.vera.eventHandlers.CommandHandlerBuilder} when creating the command handler. If none are set, the handler
     * will automatically retrieve the user who created the application in the discord developer portal.
     */
    protected boolean isOwnerCommand = false;


    protected ChatCommandTemplate() {
        super(CommandType.CHAT_COMMAND);
    }

    /**
     * The entrypoint into your command.
     *
     * @param event          The {@link MessageReceivedEvent} which caused the command to be called.
     * @param message        The {@link Message} that fired the event.
     * @param messageContent The message content with the prefix and command name stripped out.
     */
    public abstract void executeChatCommand(MessageReceivedEvent event, Message message, String messageContent);

    /**
     * Gets the type of command. This value is hardcoded into the commandTemplate and cannot be changed. This value can
     * be used to determine the type of command.
     *
     * @return A {@link CommandType} that identifies the type of command that this class represents.
     */
    @Override
    public CommandType getCommandType() {
        return commandType;
    }

    public boolean isOwnerCommand() {
        return isOwnerCommand;
    }

    public String[] getAliases() {
        return aliases;
    }

    public List<String> getAllCommandNames() {
        ArrayList<String> array = new ArrayList<>(Arrays.asList(getAliases()));
        array.add(getCommandName());
        return array;
    }


}
