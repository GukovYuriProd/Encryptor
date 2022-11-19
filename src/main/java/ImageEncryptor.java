import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class ImageEncryptor {
    //BGR - порядок кодирования битов
    //формула вычисления интенсивности пикселя по стандарту BT-709 Y = 0.2125·R + 0.7154·G + 0.0721·B
    public static void main (String[] Args) throws IOException {
        EncryptImageBySPBlock("src/main/resources/picture.bmp", "src/main/resources/output.bmp");
    }
    public static void EncryptImageBySPBlock (String source, String destination) throws IOException{
        byte[] bitmap = ReadBytes(source); //чтение
        bitmap = encrypted_bitmap(bitmap); //шифрование
        WriteBytesToFile(destination, bitmap); //запись
    }
    public static void WriteBytesToFile (String source, byte[] bytes) throws IOException {
        File output_file = new File(source);
        BufferedOutputStream output_stream = new BufferedOutputStream(new FileOutputStream(output_file));
        output_stream.write(bytes);
        output_stream.flush();
        output_stream.close();
    }
    public static byte[] ReadBytes (String source) throws IOException {
        File input_file = new File(source);
        return Files.readAllBytes(input_file.toPath());
    }
    public static byte[] encrypted_bitmap (byte[] bitmap){
        int pixels_count = bitmap.length;
        byte[] output_bitmap = new byte[pixels_count];
        //заполняем шапку файла
        System.arraycopy(bitmap, 0, output_bitmap, 0, 54);
        //примем размерность нашего блока данных за 48 битов,
        //то есть это 6 байтов, кодирующие 2 последовательных пикселя
        int data_block = 30; //ДЛИНА БЛОКА ДАННЫХ
        for (int index = 54; index < pixels_count; index += data_block){
            //здесь на каждой итерации обрабатываем 30 последовательных байтов в значимой массе байтов
            //и заполняем их биты в общий битовый блок bits на 240 разрядов
            byte[] bits = new byte[data_block*8]; //блок данных
            for (int byte_index = 0; byte_index < data_block; byte_index++){ //читаю по порядку байты одного блока
                for (int bit = byte_index*8+7; bit >= byte_index*8; bit--){ //читаю биты одного байта из блока
                    if ((bitmap[index+byte_index]%2==1)||(bitmap[index+byte_index]%2==-1)) bits[bit] = 1;
                    bitmap[index+byte_index] = (byte) (bitmap[index+byte_index] >> 1);
                }
            }
            //обработка блока данных S и P блоками
            bits = BlockEncryptor(bits);

            //далее происходит запись в выходную карту байтов
            for (int i = index; i < index+data_block; i++){
                int pre_byte = 0;
                for (int local_bit = (i-index)*8; local_bit < (i-index)*8 + 8; local_bit++){
                    pre_byte += bits[local_bit]*Math.pow(2,(7 - (local_bit-((i-index)*8))));
                }
                output_bitmap[i] = (byte) pre_byte;
            }
        }
        return output_bitmap;

    }
    public static byte[] BlockEncryptor(byte[] block){
        byte[] output = block;
        int rounds = 5;
        for (int index = 0; index < rounds; index++){
            output = S_BlockOperation(output);
            output = P_BlockOperation(output, index+2);
        }
        return output;
    }
    public static byte[] S_BlockOperation(byte[] block){ //Блок ПОДСТАНОВКА S
        byte[] output = new byte[block.length];
        int block_size = 12;
        int count_of_S_blocks = block.length / block_size;
        for (int S_index = 0; S_index < count_of_S_blocks; S_index++){
            int sourcePosition = S_index*block_size;
            for (int i = 0; i < block_size; i++) output[sourcePosition+i] = (byte) (block[sourcePosition+i] == 1 ? 0 : 1);
            output[sourcePosition] = block[sourcePosition+11];
            output[sourcePosition+1] = block[sourcePosition+10];
            output[sourcePosition+2] = block[sourcePosition+9];
            output[sourcePosition+3] = block[sourcePosition+8];
            output[sourcePosition+4] = block[sourcePosition+7];
            output[sourcePosition+5] = block[sourcePosition+6];
            output[sourcePosition+6] = block[sourcePosition+5];
            output[sourcePosition+7] = block[sourcePosition+4];
            output[sourcePosition+8] = block[sourcePosition+3];
            output[sourcePosition+9] = block[sourcePosition+2];
            output[sourcePosition+10] = block[sourcePosition+1];
            output[sourcePosition+11] = block[sourcePosition];
        }
        return output;
    }
    public static byte[] P_BlockOperation(byte[] block, int shift) { //Блок ПЕРЕСТАНОВКА P
        if (block != null) {
            int length = block.length;
            byte[] out = new byte[length];
            System.arraycopy(block, shift, out, 0, length - shift);
            System.arraycopy(block, 0, out, length - shift, shift);
            return out;
        } else {
            return null;
        }
    }
}