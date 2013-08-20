package co.deepthought;

import co.deepthought.imagine.image.Fingerprinter;
import co.deepthought.imagine.store.Size;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;

public class Tester {

    public static void main(String[] args) throws IOException {

        final String[] imageFiles = {
            "fireboat-300x200-100.jpg",
            "fireboat-1200x800-60.jpg",
            "liberty+autocolor-900x600-60.jpg",
            "liberty-600x400-40.jpg",
            "liberty-1200x800-80.jpg",
            "pathological-cropped-1.jpeg",
            "pathological-cropped-2.jpeg",
            "pathological-skyline-1.jpeg",
            "pathological-skyline-2.jpeg",
            "hat-small.jpeg",
            "hat-large.jpeg"
        };
        final String[] fingerprints = new String[imageFiles.length];
        for(int i = 0; i < imageFiles.length; i++) {
            final BufferedImage image = ImageIO.read(
                new File("/Users/kevindolan/rentenna/imagine/resources/test/"+imageFiles[i]));
            final Fingerprinter fingerprinter = new Fingerprinter(image);
            fingerprints[i] = fingerprinter.getFingerprint(new Size(36, 24));
        }
        final HashSet<String> seen = new HashSet<>();
        for(int i = 0; i < imageFiles.length; i++) {
            if(!seen.contains(imageFiles[i])) {
                seen.add(imageFiles[i]);
                System.out.println(imageFiles[i]);
                for(int j = 0; j < imageFiles.length; j++) {
                    if(!seen.contains(imageFiles[j])) {
                        final int difference = Fingerprinter.difference(fingerprints[i], fingerprints[j]);
                        System.out.println(difference);
                        if(difference < 256) {
                            System.out.println("\t" + imageFiles[j]);
                            seen.add(imageFiles[j]);
                        }
                    }
                }
            }
        }

    }

}
