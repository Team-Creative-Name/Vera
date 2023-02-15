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
package com.tcn.vera.commands.templates;

import javax.annotation.Nonnull;

/**
 * A list of all the command types that Vera currently supports. An incorrectly marked command type could cause issues
 * when {@link com.tcn.vera.eventHandlers.CommandHandler} attempts to handle it. Please note that any class using these types must also
 * implement any methods that the default command template for that type does. For example, a class marked with the {@link CommandType#CHAT_COMMAND}
 * type should either extend or reimplement everything found in {@link ChatCommandTemplate}.
 *
 * @author Thomas Wessel
 * @since 1.0
 */
public enum CommandType {

    /**
     * If a command is of a type that we do not yet handle, it should be marked as UNKNOWN.
     */
    UNKNOWN(-1),

    /**
     * Used when the command is designed to handle a {@link net.dv8tion.jda.api.events.message.MessageReceivedEvent}
     */
    CHAT_COMMAND(1),

    /**
     * Used when the command is designed to handle a {@link net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent}
     */
    SLASH_COMMAND(2),

    /**
     * Used when the command is designed to handle a {@link net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent}
     */
    USER_CONTEXT_COMMAND(3),

    /**
     * Used when the command is designed to handle a {@link net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent}
     */
    CONTEXT_MESSAGE_COMMAND(4),
    ;

    private final int thisCommandType;

    CommandType(int thisCommandType) {
        this.thisCommandType = thisCommandType;
    }

    /**
     * gets the type of command based upon its integer value.
     *
     * @param key The integer value that the command type represents.
     * @return The CommandType that your key represents. If there is no match, then it returns the {@link CommandType#UNKNOWN} type
     * which is mapped to the key of value -1.
     */
    @Nonnull
    public static CommandType fromKey(int key) {
        for (CommandType type : values()) {
            if (type.thisCommandType == key)
                return type;
        }
        return UNKNOWN;
    }
}
