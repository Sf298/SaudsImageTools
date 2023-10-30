package maintest;

import sauds.image.tools2.*;

import javax.swing.Timer;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class ImageDedupe2 {

    public static Map<String, ArrayList<File>> hashes = new HashMap<>();
    private static int fffff = 0;
/*
    public static void main(String... args) throws IOException {
        segment();
    }

    public static void segment() {
        File destPath = new File("/home/saud/Downloads/just delete/Multimedia2/");
        File root = new File("/home/saud/Downloads/just delete/Multimedia/");
        File[] files = root.listFiles();
        if (isNull(files)) return;

        Timer t = new Timer(3000, e -> printProgress());
        t.start();

        List<FileFingerprint> fingerprints = Arrays.stream(files)
                .filter(File::isFile)
                .filter(f -> !f.getName().toLowerCase().endsWith(".mp4"))
                .skip(250)
                //.limit(500)
                //.parallel()
                .map(f -> {
                    ImageRaster imgRaster = readImg(f);
                    if (isNull(imgRaster)) return null;
                    return FileFingerprint.of(imgRaster, f);
                })
                .filter(Objects::nonNull)
                .map(p -> {
                    Image image = p.getKey();
                    long start = System.currentTimeMillis();
                    Image resized = Operation.resize(image, 16,16);
                    Image sobelX = Operation.convolve(resized, BorderHandling.IGNORE, Kernel.sobelX(), Aggregator.MEAN, 1, 1);
                    Image sobelY = Operation.convolve(resized, BorderHandling.IGNORE, Kernel.sobelY(), Aggregator.MEAN, 1, 1);
                    Image sobelXGrey = Operation.toGreyscale(SubpixelOperation.abs(sobelX));
                    Image sobelYGrey = Operation.toGreyscale(SubpixelOperation.abs(sobelY));
                    Image sum = SubpixelOperation.add(sobelXGrey, sobelYGrey);
                    System.out.println("IMG "+(System.currentTimeMillis()-start)+"\t"+p.getValue());
                    fffff++;

                    //Img rescaled = (Img) img.resize(16, 16);
                    //ImgInterface sobelX = rescaled.convolve(Kernel.sobelX(), Img.BORDER_IGNORE, 1, Img.CONV_MEAN, true, 0);
                    //ImgInterface sobelY = rescaled.convolve(Kernel.sobelY(), Img.BORDER_IGNORE, 1, Img.CONV_MEAN, true, 0);
                    //ImgInterface greySobelX = sobelX.toGrey();
                    //ImgInterface greySobelY = sobelY.toGrey();
                    //ImgInterface sum = greySobelX.add(greySobelY);
                    //ImgInterface div = sum.div(16);
                    if (sum.getWidth() != 16 || sum.getHeight() != 16) {
                        System.out.println();
                        Image s = Operation.resize(image, 16,16);
                    }
                    return FileFingerprint.of(sum, p.getValue());
                })
                .collect(Collectors.toList());

        System.out.println("done reads");
        Map<Set<FileFingerprint>, Long> distances = comparePairs(
                fingerprints,
                (p1, p2) -> {
                    Image diff = SubpixelOperation.abs(SubpixelOperation.sub(p1.getKey(), p2.getKey()));
                    return diff.sumAllValues() / diff.getSubpixelCount();
                },
                d -> d<5
        );


        System.out.println();
    }
    public static synchronized ImageRaster readImg(File f) {
        ImageRaster imgRaster;
        try {
            imgRaster = ImageRaster.create(f);
        } catch (Exception e) {
            System.err.println(f+" "+e.getLocalizedMessage());
            return null;
        }
        return imgRaster;
    }

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

            if (result.length() > 1)
                System.out.println(result);
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
        }
    }

    public static <A,R> Map<Set<A>, R> comparePairs(List<A> pairs, BiFunction<A,A,R> distanceCalculator, Predicate<R> distanceFilter) {
        Map<Set<A>, R> out = new HashMap<>();

        for (int i = 0; i < pairs.size(); i++) {
            for (int j = i+1; j < pairs.size(); j++) {
                R result = distanceCalculator.apply(pairs.get(i), pairs.get(j));
                if (nonNull(result) && distanceFilter.test(result)) {
                    Set<A> key = new HashSet<>();
                    key.add(pairs.get(i));
                    key.add(pairs.get(j));
                    out.put(key, result);
                }
            }
        }

        return out;
    }

    private static class FileFingerprint {
        Image image;
        File file;

        public FileFingerprint(Image image, File file) {
            this.image = image;
            this.file = file;
        }

        public Image getKey() {
            return getLeft();
        }
        public Image getLeft() {
            return image;
        }
        public File getRight() {
            return file;
        }
        public File getValue() {
            return getRight();
        }

        public static FileFingerprint of(Image image, File file) {
            return new FileFingerprint(image, file);
        }
    }*/
}