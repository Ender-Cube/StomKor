package me.zax71.stomKor.blocks;

import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class Sign implements BlockHandler {
    @Override
    public @NotNull NamespaceID getNamespaceId() {
        return NamespaceID.from("minecraft", "sign");
    }

    @Override
    public @NotNull Collection<Tag<?>> getBlockEntityTags() {
        return List.of(
                Tag.Byte("GlowingText"),
                Tag.String("Color"),
                Tag.String("Text1"),
                Tag.String("Text2"),
                Tag.String("Text3"),
                Tag.String("Text4")
        );
    }


}
