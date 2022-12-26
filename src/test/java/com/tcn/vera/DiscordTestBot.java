package com.tcn.vera;

import com.tcn.vera.eventHandlers.CommandHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class DiscordTestBot {


    public static void main(String[] args) {
        JDA discordBot = JDABuilder.createDefault(Secrets.discordToken).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();

        CommandHandler veraHandler = new CommandHandler.CommandHandlerBuilder()
                .addOwner(Secrets.ownerID)
                .addCommand(new CommandTest())
                .changePrefix("!")
                .build();

        discordBot.addEventListener(veraHandler);

        System.out.println("All ready");
    }



}
