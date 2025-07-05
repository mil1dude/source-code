package net.minecraft.util.debugchart;

public interface SampleStorage {
    int capacity();

    int size();

    long get(int p_327727_);

    long get(int p_334951_, int p_335677_);

    void reset();
}