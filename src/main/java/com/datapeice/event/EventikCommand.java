package com.datapeice.event;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class EventikCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("eventik")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("status", StringArgumentType.string())
                                .executes(context -> savest(context, StringArgumentType.getString(context, "status")))
                        )
        );
    }

    private static int savest(CommandContext<ServerCommandSource> context, String status){
        if (status == "on") ;
        if (status == "off") ;
        return 1;
    }

}
