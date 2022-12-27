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
package com.tcn.vera.eventHandlers;

import com.tcn.vera.commands.CommandTemplate;

import java.util.ArrayList;

public class CommandHandlerBuilder{
    private ArrayList<CommandTemplate> commandList = new ArrayList<>();
    private String[] botOwner = new String[0];
    private String prefix = "!";

    public CommandHandler build(){
        runChecks();
        return new CommandHandler(commandList, botOwner, prefix);
    }

    private void runChecks(){

        if(commandList.isEmpty()){
            throw new IllegalArgumentException("Cannot build CommandHandler without any commands! Please ensure that there " +
                    "is at least one command added via the CommandHandlerBuilder.addCommand method!");
        }

        if(prefix.length() > 1 || prefix.equalsIgnoreCase(" ")){
            throw new IllegalArgumentException("The prefix cannot be longer than one character and cannot be a black space." +
                    "Please modify the prefix given to the changePrefix method.");
        }

        for (CommandTemplate command : commandList){

            //ensure that there is at least one type of command enabled
            if(!command.isAllowChatCommand() && !command.isAllowSlashCommand()){
                throw new IllegalArgumentException("The command \"" + command.getCommandName() + "\" must have either " +
                        "a slash command or a chat command enabled in order to be registered. Please set one of the " +
                        "booleans to \"true\" in the command's constructor.");
            }

            //ensure that there is not a space in the command name
            if(command.getCommandName().contains(" ")){
                throw new IllegalArgumentException("The command \"" + command.getCommandName() + "\" must not contain " +
                        "any spaces in its name. Please change the commandName string in the command's constructor.");
            }
        }

    }

    /**
     * Adds a command to be registered to the command handler. Can be called multiple times to add more commands.
     * @param newCommand
     *  The command object that you wish to be handled by the command handler.
     * @return
     *  This builder.
     */
    public CommandHandlerBuilder addCommand(CommandTemplate newCommand){
        commandList.add(newCommand);
        return this;
    }

    /**
     * Adds a user ID that is allowed to access commands marked as owner only. This command can be called multiple
     * times to add additional users.
     * @param owner
     *  The discord ID of the user you would like to add.
     * @return
     *  This builder
     */
    public CommandHandlerBuilder addOwner(String owner){
        String[] temp = new String[botOwner.length + 1];
        System.arraycopy(botOwner, 0, temp, 0, botOwner.length);
        temp[botOwner.length] = owner;
        botOwner = temp;
        return this;
    }

    /**
     * Adds multiple user IDs that are allowed to access commands marked as owner only. This command can be called multiple
     * times to add additional users.
     * @param owner
     *  A string array of discord user IDs.
     * @return
     *  This builder
     */
    public CommandHandlerBuilder addOwner(String[] owner){
        String[] temp = new String[botOwner.length + owner.length];
        System.arraycopy(botOwner, 0, temp, 0, botOwner.length);
        System.arraycopy(owner, 0, temp, botOwner.length, owner.length);
        botOwner = temp;
        return this;
    }

    /**
     * Changes the bot prefix from the default '!' prefix.
     * @param newPrefix
     *  The prefix that you want to use. It cannot be longer than one character.
     * @return
     *  This builder
     */
    public CommandHandlerBuilder changePrefix(String newPrefix){
        prefix = newPrefix;
        return this;
    }

}