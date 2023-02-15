package com.tcn.vera.commands.interactions;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public interface ButtonInterface {
    void executeButton(ButtonInteractionEvent event);

    String getButtonClassID();

}
