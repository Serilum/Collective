package com.natamus.collective.forge.mixin.plugin;

import com.natamus.collective.bundle.BundleConfigCheck;
import com.natamus.collective.forge.bundle.ForgeBundleJarJarCheck;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class ForgeMixinConfigPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (((mixinClassName.contains(".neoforge.") || mixinClassName.contains("_neoforge.")) && !isNeoForge() ||
                (mixinClassName.contains(".forge.") || mixinClassName.contains("_forge.")) && !isForge() ||
                (mixinClassName.contains(".fabric.") || mixinClassName.contains("_fabric.")) && !isFabric())) {
            return false;
        }

		String[] pSpl = mixinClassName.split("\\.");
		if (pSpl.length < 3) {
			return true;
		}

		String modId = pSpl[2].split("_")[0];

        if (ForgeBundleJarJarCheck.isModJarJard(modId)) {
            return BundleConfigCheck.isBundleModEnabled(modId);
        }

        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    // Because NeoForge attempts to load Forge mixins, and can't have Forge code. Yep, it's hack-ish:
    private static boolean isFabric() {
        try { Class.forName("net.fabricmc.api.EnvType"); return true;
        } catch(ClassNotFoundException e) { return false; }
    }

    private static boolean isForge() {
        try { Class.forName("net.minecraftforge.fml.loading.FMLEnvironment"); return true;
        } catch(ClassNotFoundException e) { return false; }
    }

    private static boolean isNeoForge() {
        try { Class.forName("net.neoforged.fml.loading.FMLEnvironment"); return true;
        } catch(ClassNotFoundException e) { return false; }
    }
}