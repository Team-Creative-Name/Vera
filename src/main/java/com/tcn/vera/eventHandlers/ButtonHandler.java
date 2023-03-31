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

import com.tcn.vera.commands.interactions.ButtonInterface;
import com.tcn.vera.utils.CommandCache;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.EventListener;
import java.util.function.Consumer;

/**
 * The Button handler for Vera. It is recommended that you only use one instance of this class per bot. If you have ANY commands that
 * use buttons, you MUST set an instance of this class in {@link CommandHandlerBuilder#addButtonHandler(ButtonHandler)} so it can
 * properly register your buttons and handle any presses.
 * <p>
 * If you are using any of Vera's paginator classes, you must pass the same instance of this class to the paginator, so it can properly register itself.
 *
 * @implNote By default, this class has a cache size of 100. If you need to support more buttons at a time, please use {@link #ButtonHandler(int cacheSize)}.
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
     * Creates a new ButtonHandler with a custom cache size.
     * @param cacheSize The maximum number of buttons that can be registered at a time. If more buttons are registered, the oldest button will be removed.
     */
    public ButtonHandler(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    private final CommandCache<String, Consumer<? super ButtonInteractionEvent>> listeners = new CommandCache<>(cacheSize);

    /**
     * This method is called when a button is pressed. It will check if the button is registered, and if it is, it will call the callback.
     * @param event The {@link ButtonInteractionEvent} that was fired.
     */
    public void onEvent(ButtonInteractionEvent event) {
        Consumer<? super ButtonInteractionEvent> callback = listeners.find(prefix -> event.getComponentId().startsWith(prefix));

        if (callback != null) {
            callback.accept(event);
        }else{
            event.reply("This button is not valid").setEphemeral(true).queue();
        }
    }

    /**
     * Registers a button with a callback. The callback will be called when the button is pressed.
     * @param prefix The prefix of the button. This is used to identify the button. The prefix should be the value set in {@link ButtonInterface#getButtonClassID()}.
     * @param callback The callback to call when the button is pressed.
     */
    public void registerButtonSet(String prefix, Consumer<? super ButtonInteractionEvent> callback) {
        listeners.add(prefix, callback);
    }
}
