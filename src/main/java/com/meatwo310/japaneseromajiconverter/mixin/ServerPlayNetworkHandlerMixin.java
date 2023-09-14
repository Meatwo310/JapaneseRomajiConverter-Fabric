package com.meatwo310.japaneseromajiconverter.mixin;


import com.meatwo310.japaneseromajiconverter.JapaneseRomajiConverter;
import com.meatwo310.japaneseromajiconverter.util.RomajiToHiragana;
import net.minecraft.server.filter.TextStream;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @ModifyVariable(method = "handleMessage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private TextStream.Message modifyMessage(TextStream.Message message) {
        String raw = message.getRaw();
        String filtered = message.getFiltered();
        String rawConverted;
        String filteredConverted;

        try {
            rawConverted = RomajiToHiragana.convertMessageStringToHiragana(raw);
            if (raw.equals(filtered)) {
                filteredConverted = rawConverted;
            } else {
                filteredConverted = RomajiToHiragana.convertMessageStringToHiragana(filtered);
            }

            return new TextStream.Message(rawConverted, filteredConverted);

        } catch (Exception e) {
            JapaneseRomajiConverter.LOGGER.error(e.toString());
        }
        return message;
    }
}
