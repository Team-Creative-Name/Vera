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

import com.tcn.vera.commands.templates.SlashCommandTemplate;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;

/**
 * This interface allows the addition of autocomplete interactions to a slash command. Because only the slash command
 * can use autocomplete, implementing this in any class other than ones extending {@link SlashCommandTemplate}
 * will have no effect.
 */
public interface AutoCompleteInterface {

    /**
     * The code executed when the user attempts to autofill a slash command.
     *
     * @param event The {@link CommandAutoCompleteInteractionEvent} sent by discord
     */
    void executeAutocomplete(CommandAutoCompleteInteractionEvent event);

}
