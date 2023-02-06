package com.tcn.vera.interactions;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public interface ButtonInterface {
    void executeButton(ButtonInteractionEvent event);

    String getButtonClassID();

}
