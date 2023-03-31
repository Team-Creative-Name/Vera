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

import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.Objects;

/**
 * This is the User Context command template. Any command that can be executed via right-clicking on a user should
 * extend this class. Please note that discord imposes a limit of a total of five user context commands per bot. This
 * is not something that can be changed.
 * <p>
 * Once you have built a command based upon this class, you simply need to pass it to an instance of the Vera {@link com.tcn.vera.eventHandlers.CommandHandlerBuilder}
 * and then add that as an event listener in the {@link net.dv8tion.jda.api.JDABuilder}. For example:
 * <blockquote><pre>
 *     CommandHandler veraHandler = new CommandHandlerBuilder()
 *                 .addCommand(new exampleUserContextCommand())  //<--- the command that we want to register
 *                 .build();
 *
 *     JDA exampleBot = JDABuilder
 *                 .createDefault("DISCORD_TOKEN")
 *                 .addEventListeners(veraHandler)
 *                 .build();
 * </pre></blockquote>
 * This template has a number of fields that you may set to customize your command. While none of these values are required,
 * it is important to note that the commandName field must be unique amongst any other User Context commands. If any fields
 * are ignored, they will be set to their default values. Please see the individual javadoc entries for more information
 * about what each individual field represents.
 * <p>
 * Additionally, this class will automatically generate a {@link CommandData} object based upon the given command name string.
 * To change this, simply add a command data object in the constructor.
 * <br>
 * Here is an example of this class's constructor with every field set:
 * <blockquote><pre>
 *     public ExampleUserContextCommand(){
 *         this.commandName = "User Context Example";
 *         this.help = "This is an example user context command";
 *         this.messageContextCommand = Commands.context(Command.Type.USER, getCommandName());
 *     }
 * </pre></blockquote>
 */
public abstract class UserContextTemplate extends CommandTemplateBase {

    /**
     * The {@link CommandData} object that will be sent to discord. This will be automatically generated if one is not
     * specified in the constructor.
     */
    protected CommandData userContextCommand = null;

    protected UserContextTemplate() {
        super(CommandType.USER_CONTEXT_COMMAND);
    }

    /**
     * The entrypoint into your command. Place any code that you want to execute when the command is called here.
     *
     * @param event The {@link UserContextInteractionEvent} which caused the command to be executed.
     */
    public abstract void executeUserContextCommand(UserContextInteractionEvent event);

    /**
     * Gets a {@link CommandData} object that represents the data to be sent to Discord. If there was not one set in the constructor
     * this method generates one based upon the command name.
     *
     * @return A {@link CommandData} object representing the data to send to Discord.
     * @implNote Because this command is so simple, it is best to let the template generate a command data object for you unless
     * you need any specific features.
     */
    public CommandData getUserContextCommand() {
        return Objects.requireNonNullElseGet(userContextCommand, () -> Commands.context(Command.Type.USER, getCommandName()));
    }

    public CommandType getCommandType() {
        return commandType;
    }


}
