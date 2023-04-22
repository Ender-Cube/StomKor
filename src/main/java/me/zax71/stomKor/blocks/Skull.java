package me.zax71.stomKor.blocks;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class Skull implements BlockHandler {

    @Override
    public @NotNull NamespaceID getNamespaceId() {
        return NamespaceID.from(Key.key("minecraft:skull"));
    }

    @Override
    public @NotNull Collection<Tag<?>> getBlockEntityTags() {
        return List.of(
                Tag.String("ExtraType"),
                Tag.NBT("SkullOwner")
        );
    }

}
