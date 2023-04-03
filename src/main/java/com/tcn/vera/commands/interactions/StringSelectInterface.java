/*
 * Vera - a common library for all of TCN's discord bots.
 *
 * Copyright (C) 2023 Thomas Wessel and the rest of Team Creative Name
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
package com.tcn.vera.commands.interactions;

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

/**
 * This interface allows for your command to handle string select interactions. This interface is only useful on
 * {@link com.tcn.vera.commands.templates.SlashCommandTemplate Slash Command},
 * {@link com.tcn.vera.commands.templates.MessageContextTemplate Message Context} and
 * {@link com.tcn.vera.commands.templates.UserContextTemplate User Context} command templates.
 * <p>
 * To use this interface, send a {@link StringSelectMenu} to discord as a reply to the original command's interaction event. Then, ensure that the
 * {@link StringSelectInterface#getMenu()} method returns the same menu that you sent to discord. You can then use the
 * {@link StringSelectInterface#executeStringSelectInteraction(StringSelectInteractionEvent)}
 * to execute code when the user selects a string.
 */
public interface StringSelectInterface {

    /**
     * The code executed when discord fires the {@link StringSelectInteractionEvent} event related to this command.
     *
     * @param event The event that contains the user's selection.
     */
    void executeStringSelectInteraction(StringSelectInteractionEvent event);

    /**
     * In order for the {@link com.tcn.vera.eventHandlers.CommandHandler} to match a string select event to this command, it must
     * be able to see it. Please ensure that the menu returned by this method is the same as the one that you used as a
     * response to the Discord interaction event.
     *
     * @return The menu sent to discord as a reply
     */
    StringSelectMenu getMenu();
}
