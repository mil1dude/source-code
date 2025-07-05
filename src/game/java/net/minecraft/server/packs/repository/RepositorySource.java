package net.minecraft.server.packs.repository;

import java.util.function.Consumer;

@FunctionalInterface
public interface RepositorySource {
    void loadPacks(Consumer<Pack> p_10542_);
}