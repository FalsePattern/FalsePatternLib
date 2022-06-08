package com.falsepattern.lib.mixin;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.val;

public interface IMixin {

    String getMixin();

    default boolean shouldLoad(List<ITargetedMod> loadedMods) {
        val side = getSide();
        return (side == Side.COMMON || side == Side.SERVER && FMLLaunchHandler.side().isServer() ||
                side == Side.CLIENT && FMLLaunchHandler.side().isClient()) && getFilter().test(loadedMods);
    }

    Side getSide();

    Predicate<List<ITargetedMod>> getFilter();

    enum Side {
        COMMON,
        CLIENT,
        SERVER
    }

    final class PredicateHelpers {
        public static Predicate<List<ITargetedMod>> never() {
            return (list) -> false;
        }

        public static Predicate<List<ITargetedMod>> condition(Supplier<Boolean> condition) {
            return (list) -> condition.get();
        }

        public static Predicate<List<ITargetedMod>> always() {
            return (list) -> true;
        }

        public static Predicate<List<ITargetedMod>> require(ITargetedMod mod) {
            return (list) -> list.contains(mod);
        }

        public static Predicate<List<ITargetedMod>> avoid(ITargetedMod mod) {
            return (list) -> !list.contains(mod);
        }
    }
}
