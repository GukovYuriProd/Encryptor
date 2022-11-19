import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public class CheckImage {
    public static void main (String[] args) throws IOException {
        File input_file = new File("src/main/resources/picture.bmp");
        System.out.println(Arrays.toString(Files.readAllBytes(input_file.toPath())));
    }
}