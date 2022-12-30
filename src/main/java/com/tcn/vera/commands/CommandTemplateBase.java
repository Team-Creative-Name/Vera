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

/**
 * A common parent for every command template class. This class defines features that every command template must support
 * in order for the {@link com.tcn.vera.eventHandlers.CommandHandler} to be able to use it.
 * <p>
 * Unless the user is attempting to define a custom command type, there should be no reason for them to extend this class.
 *
 * @author Thomas Wessel
 * @since 1.0
 */
public abstract class CommandTemplateBase {

    /**
     * Defines the type of command that the template represents using the command types in {@link CommandType}.
     * Every template that extends this one must set its type. Because it is final, it can be trusted to determine the features
     * that the command supports.
     */
    protected final CommandType commandType;
    /**
     * The name of the command. Unless overridden, this is what the command template will use as the invocation method for
     * your command.
     * <p>
     * Depending on the type of command, the name has a few requirements:
     * <ul>
     * <li>Chat, User Context, and Message context Commands: Names can contain spaces and can be mixed case.
     * <li>Slash Commands: Names cannot contain spaces and must be lowercase.
     * </ul>
     * <p>
     * This is only the most important parts of the name requirements. To see the full list of name requirements, check out
     * the <a href="https://discord.com/developers/docs/interactions/application-commands#application-command-object">Discord docs on Application Command Naming</a>
     */
    protected String commandName = "command";
    /**
     * This string is used as the general description of the command. If this command is something that discord shows a description
     * near such as a context or slash command, it will appear there if you use the default command builder. This string is also presented
     * by Vera's builtin help command if enabled.
     */
    protected String help = "No help provided for this command!";

    protected CommandTemplateBase(CommandType type) {
        this.commandType = type;
    }

    public String getCommandName() {
        return commandName;
    }

    public String getCommandHelp() {
        return help;
    }

    public abstract CommandType getCommandType();

}
