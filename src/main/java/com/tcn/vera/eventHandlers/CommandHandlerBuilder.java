/*
 * Vera - a common library for all of TCN's discord bots.
 *
 * Copyright (C) 2022-23 Thomas Wessel and the rest of Team Creative Name
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
package com.tcn.vera.eventHandlers;

import com.tcn.vera.commands.templates.ChatCommandTemplate;
import com.tcn.vera.commands.templates.CommandTemplateBase;
import com.tcn.vera.commands.templates.CommandType;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;

public class CommandHandlerBuilder {
    private final ArrayList<CommandTemplateBase> commandList = new ArrayList<>();
    private final ArrayList<String> botOwner = new ArrayList<>();
    private String prefix = "!";

    private ButtonHandler buttonHandler = null;

    public CommandHandler build() {
        runChecks();
        return new CommandHandler(commandList, botOwner, prefix, buttonHandler);
    }

    private void runChecks() {

        if (commandList.isEmpty()) {
            throw new IllegalArgumentException("Cannot build CommandHandler without any commands! Please ensure that there " +
                    "is at least one command added via the CommandHandlerBuilder.addCommand method!");
        }

        if (prefix.length() > 1 || prefix.equalsIgnoreCase(" ")) {
            throw new IllegalArgumentException("The prefix cannot be longer than one character and cannot be a blank space." +
                    "Please modify the prefix given to the changePrefix method. This commandhandler will not be built");
        }

        for (CommandTemplateBase command : commandList) {
            if (command.getCommandType() == CommandType.CHAT_COMMAND) {
                ChatCommandTemplate tempCommand = (ChatCommandTemplate) command;
                if (tempCommand.getAllCommandNames().stream().anyMatch(c -> c.contains(" "))) {
                    throw new IllegalArgumentException("Sorry, chat commands cannot have a space in their name or aliases");
                }
            }
        }

        if(buttonHandler == null){
            LoggerFactory.getLogger("Vera: Command Handler").warn("No button handler was provided to the command handler. " +
                    "If you want to use buttons, please provide a button handler to the command handler builder via the addButtonHandler method.");
            buttonHandler = new ButtonHandler();
        }
    }

    /**
     * Adds a command to be registered to the command handler. Can be called multiple times to add more commands.
     *
     * @param newCommand The command object that you wish to be handled by the command handler.
     * @return This builder.
     */
    public CommandHandlerBuilder addCommand(CommandTemplateBase newCommand) {
        commandList.add(newCommand);
        return this;
    }

    /**
     * Adds a user ID that is allowed to access commands marked as owner only. This command can be called multiple
     * times to add additional users.
     *
     * @param owner A string representing the discord ID of the user you would like to add.
     * @return This builder
     */
    public CommandHandlerBuilder addOwner(String owner) {
        botOwner.add(owner);
        return this;
    }

    /**
     * Adds multiple user IDs that are allowed to access commands marked as owner only. This command can be called multiple
     * times to add additional users.
     *
     * @param owner A string array of discord user IDs.
     * @return This builder
     */
    public CommandHandlerBuilder addOwner(String[] owner) {
        botOwner.addAll(Arrays.stream(owner).toList());
        return this;
    }

    /**
     * Changes the bot prefix from the default '!' prefix.
     *
     * @param newPrefix The prefix that you want to use. It cannot be longer than one character.
     * @return This builder
     */
    public CommandHandlerBuilder changePrefix(String newPrefix) {
        prefix = newPrefix;
        return this;
    }

    /**
     * Adds a button handler to the command handler. This is required if you want to use buttons.
     * <p>
     * Only the last button handler added will be used.
     * @param buttonHandler The button handler you want to use for all of your buttons.
     * @return This Builder
     */
    public CommandHandlerBuilder addButtonHandler(ButtonHandler buttonHandler) {
        this.buttonHandler = buttonHandler;
        return this;
    }

}