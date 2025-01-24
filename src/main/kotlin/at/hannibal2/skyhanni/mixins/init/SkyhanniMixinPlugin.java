package at.hannibal2.skyhanni.mixins.init;

import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.InjectionPoint;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SkyhanniMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {
        InjectionPoint.register(BeforeForLoopInjectionPoint.class);
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return false;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    public URL baseUrl(URL classUrl) {
        String string = classUrl.toString();
        if (classUrl.getProtocol().equals("jar")) {
            try {
                return new URL(string.substring(4, string.lastIndexOf('!')));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        if (string.endsWith(".class")) {
            try {
                return new URL(string.replace("\\", "/")
                    .replace(getClass().getCanonicalName().replace(".", "/") + ".class", ""));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return classUrl;
    }

    String mixinBasePackage = "at.hannibal2.skyhanni.mixins.transformers.";
    String mixinBaseDir = mixinBasePackage.replace(".", "/");

    List<String> mixins = null;

    public void tryAddMixinClass(String className) {
        String norm = (className.endsWith(".class") ? className.substring(0, className.length() - ".class".length()) : className)
            .replace("\\", "/")
            .replace("/", ".");
        if (norm.startsWith(mixinBasePackage) && !norm.endsWith(".")) {
            mixins.add(norm.substring(mixinBasePackage.length()));
        }
    }

    public void walkDir(Path file) {
        System.out.println("Trying to find mixins from directory");
        try (Stream<Path> classes = Files.walk(file.resolve(mixinBaseDir))) {
            classes.filter(Files::isRegularFile)
                .map(it -> file.relativize(it).toString())
                .forEach(this::tryAddMixinClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getMixins() {
        if (mixins != null) return mixins;
        System.out.println("Trying to discover mixins");
        mixins = new ArrayList<>();
        URL classUrl = getClass().getProtectionDomain().getCodeSource().getLocation();
        System.out.println("Found classes at " + classUrl);
        Path file;
        try {
            file = Paths.get(baseUrl(classUrl).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Base directory found at " + file);
        if (Files.isDirectory(file)) {
            walkDir(file);
        } else {
            walkJar(file);
        }
        System.out.println("Found mixins: " + mixins);

        return mixins;
    }

    private void walkJar(Path file) {
        System.out.println("Trying to find mixins from jar file");
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(file))) {
            ZipEntry next;
            while ((next = zis.getNextEntry()) != null) {
                tryAddMixinClass(next.getName());
                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
