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
package com.tcn.vera.eventHandlers;

import com.tcn.vera.utils.CommandCache;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.EventListener;
import java.util.function.Consumer;

/**
 * Vera Button Handler. This class is used to handle all button interactions. Please do not use this class directly.
 */
public class ButtonHandler implements EventListener {
    int cacheSize = 100;

    /**
     * Creates a new ButtonHandler with a cache size of 100. If you need to support more buttons at a time, please use
     * {@link #ButtonHandler(int cacheSize)}.
     */
    public ButtonHandler() {
    }

    /**
     * Creates a new ButtonHandler with a desired cache size.
     */
    public ButtonHandler(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    private final CommandCache<String, Consumer<? super ButtonInteractionEvent>> listeners = new CommandCache<>(cacheSize);

    public void onEvent(ButtonInteractionEvent event) {
        Consumer<? super ButtonInteractionEvent> callback = listeners.find(prefix -> event.getComponentId().startsWith(prefix));

        if (callback != null) {
            callback.accept(event);
        }else{
            event.reply("This button is not valid").setEphemeral(true).queue();
        }
    }

    public void registerButtonSet(String prefix, Consumer<? super ButtonInteractionEvent> callback) {
        listeners.add(prefix, callback);
    }
}
