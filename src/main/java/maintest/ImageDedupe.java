package maintest;

import org.apache.commons.lang3.tuple.Pair;

import javax.swing.Timer;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class ImageDedupe {

    public static Map<String, ArrayList<File>> hashes = new HashMap<>();

    public static void main(String... args) throws IOException {
        segment();
    }

    public static void moveSingletonsToParent() {
        File root = new File("/home/saud/Downloads/just delete/Multimedia2/");
        File[] files = root.listFiles();
        if (isNull(files)) return;

        for (File dir : files) {
            if (!dir.isDirectory()) continue;

            File[] children = dir.listFiles();
            if (isNull(children) || children.length > 1) continue;

            if (children.length == 1) {
                File child = children[0];
                File parent = child.getParentFile().getParentFile();
                File newFile = new File(parent, child.getName());
                child.renameTo(newFile);
            }
            dir.delete();
        }
    }

    public static void removeIdentical() throws IOException {
        File root = new File("/home/saud/Downloads/just delete/Multimedia2/");
        File[] files = root.listFiles();
        if (isNull(files)) return;

        for (File dir : files) {
            if (!dir.isDirectory()) continue;

            outerLoop: while (true) {
                File[] children = dir.listFiles();
                if (isNull(children)) continue;
                for (int i = 0; i < children.length; i++) {
                    for (int j = i + 1; j < children.length; j++) {
                        if (filesCompareByByte(children[i].toPath(), children[j].toPath()) == -1) {
                            children[j].delete();
                            System.out.println("Deleted "+children[j]);
                            continue outerLoop;
                        }
                    }
                }
                break;
            }
        }
    }
    public static long filesCompareByByte(Path path1, Path path2) throws IOException {
        try (BufferedInputStream fis1 = new BufferedInputStream(Files.newInputStream(path1.toFile().toPath()));
             BufferedInputStream fis2 = new BufferedInputStream(Files.newInputStream(path2.toFile().toPath()))) {

            int ch;
            long pos = 1;
            while ((ch = fis1.read()) != -1) {
                if (ch != fis2.read()) {
                    return pos;
                }
                pos++;
            }
            if (fis2.read() == -1) {
                return -1;
            } else {
                return pos;
            }
        }
    }


    public static void fixMetadata() throws IOException {
        File rootPath = new File("/home/saud/Downloads/just delete/Multimedia2/");
        Files.walk(rootPath.toPath())
                .filter(Files::isRegularFile) // Only process regular files, exclude directories
                .forEach(ImageDedupe::fixFileCreatedDate);

    }
    public static void fixFileCreatedDate(Path path) {
        try {
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
            FileTime createdTime = attributes.creationTime();
            FileTime modifiedTime = attributes.lastModifiedTime();

            if (createdTime.compareTo(modifiedTime) > 0) {
                FileTime newCreatedTime = FileTime.fromMillis(modifiedTime.toMillis());
                Files.setAttribute(path, "creationTime", newCreatedTime);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void segment() {
        File destPath = new File("/home/saud/Downloads/just delete/Multimedia2/");
        File root = new File("/home/saud/Downloads/just delete/Multimedia/");
        File[] files = root.listFiles();
        if (isNull(files)) return;

        Timer t = new Timer(3000, e -> printProgress());
        t.start();

        /*List<Pair<ImgInterface, File>> imgPairs = Arrays.stream(files)
                .filter(File::isFile)
                .filter(f -> !f.getName().toLowerCase().endsWith(".mp4"))
                /*.forEach(f -> {
                    String key = calculateImgHash(f);
                    if (isNull(key)) return;

                    ArrayList<File> values = hashes.getOrDefault(key, new ArrayList<>());
                    values.add(f);
                    hashes.put(key, values);
                });*
                .map(f -> {
                    ImgInterface img = calculateImgFingerprint(f);
                    if (isNull(img)) return null;
                    return Pair.of(calculateImgFingerprint(f), f);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        /*for (Map.Entry<String, ArrayList<File>> e : hashes.entrySet()) {
            if (e.getValue().size() > 1) {
                File subPath = new File(destPath, String.valueOf(e.getKey().hashCode()));
                subPath.mkdirs();
                e.getValue().forEach(f -> f.renameTo(new File(subPath, f.getName())));
            } else {
                File f = e.getValue().get(0);
                f.renameTo(new File(destPath, f.getName()));
            }
        }*/

        System.out.println();
    }
    /*public static ImgInterface calculateImgFingerprint(File f) {
        Img img;
        try {
            img = Img.createNew(f);
        } catch (Exception e) {
            System.err.println(f+" "+e.getLocalizedMessage());
            return null;
        }

        /*long timerA = System.currentTimeMillis();
        ImgInterface sobelXResize = img.convolve(Kernel.sobelX(), Img.BORDER_IGNORE, 1, Img.CONV_MEAN, true, 0).resize(32, 32);
        System.out.println(System.currentTimeMillis() - timerA);

        long timerB = System.currentTimeMillis();
        ImgInterface resizeSobelX = ((Img)img.resize(32, 32)).convolve(Kernel.sobelX(), Img.BORDER_IGNORE, 1, Img.CONV_MEAN, true, 0);
        System.out.println(System.currentTimeMillis() - timerB);

        sobelXResize.show("sobelXResize");
        resizeSobelX.show("resizeSobelX");*

        Img rescaled = (Img) img.resize(16, 16);
        ImgInterface sobelX = rescaled.convolve(Kernel.sobelX(), Img.BORDER_IGNORE, 1, Img.CONV_MEAN, true, 0);
        ImgInterface sobelY = rescaled.convolve(Kernel.sobelY(), Img.BORDER_IGNORE, 1, Img.CONV_MEAN, true, 0);
        ImgInterface greySobelX = sobelX.toGrey();
        ImgInterface greySobelY = sobelY.toGrey();
        ImgInterface sum = greySobelX.add(greySobelY);
        ImgInterface div = sum.div(16);
        /*byte[] bytes = ((Img)div).getValues();
        return Base64.getEncoder().encodeToString(bytes);*
        return div;
    }*/

    public static void printProgress() {
        try {
            Map<Integer, Integer> freqDist = new TreeMap<>();
            for (ArrayList<File> l : hashes.values()) {
                int f = freqDist.getOrDefault(l.size(), 0);
                freqDist.put(l.size(), f + 1);
            }
            String result = freqDist.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Integer>comparingByKey().reversed())
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining(", "));

            System.out.println(result);
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
        }
    }

    public static <A,R> Map<Set<A>, R> comparePairs(List<A> pairs, BiFunction<A,A,R> distanceCalculator) {
        Map<Set<A>, R> out = new HashMap<>();

        for (int i = 0; i < pairs.size(); i++) {
            for (int j = i+1; j < pairs.size(); j++) {
                R result = distanceCalculator.apply(pairs.get(i), pairs.get(j));
                if (nonNull(result)) {
                    Set<A> key = new HashSet<>();
                    key.add(pairs.get(i));
                    key.add(pairs.get(j));
                    out.put(key, result);
                }
            }
        }

        return out;
    }
}