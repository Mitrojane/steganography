import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.imageio.ImageIO;

//Реализовать внедрение информации в изображение в формате .bmp методом блочного скрытия. 
//в отчете отразить изменеие PSNR от длины сообщения
public class Main {
	public static void main(String... args) throws IOException {
		int key = 1;
		String file = "D:\\Study\\eclipse\\stego_lab2\\barbara.bmp";
		String out = "D:\\Study\\eclipse\\stego_lab2\\output.bmp";
		
		int blockSize = code(file, out, key);
		String result = decode(out,key, blockSize);
		
		System.out.println("Decoding text:\n"+ result);
		System.out.println("RSNR: " + PSNR(file, out));
	}

	public static int code(String filename, String output, int key) throws IOException {
		File f = new File(filename);
		BufferedImage image = ImageIO.read(f);
		// int hight = image.getHeight();
		// int width = image.getWidth();
		DataBuffer data = image.getData().getDataBuffer();
		int size = data.getSize();

		System.out.println("Enter text.");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String text = reader.readLine();
		//String text = readUsingBufferedReader("D:\\Study\\eclipse\\stego_lab2\\input.txt");

		StringBuilder tmp = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			String str = Integer.toBinaryString(text.charAt(i));
			if (str.length() != 8) {
				StringBuilder tm = new StringBuilder();
				for (int j = 0; j < 8 - str.length(); j++) {
					tm.append('0');
				}
				for (int j = 0; j < str.length(); j++) {
					tm.append(str.charAt(j));
				}
				str = tm.toString();
			}
			tmp.append(str);
		}
		String bin = tmp.toString();


		System.out.println(size);
		//System.out.println(bin.length());*/

		int blockSize = size / bin.length();
		//System.out.println(blockSize);

		//System.out.println(data.getDataType());
		int curBin = 0;
		int i = 0;
		while( curBin < bin.length()) {
			int cur = Integer.parseInt(String.valueOf(bin.charAt(curBin)));
				int xor = 0;
				for (int j = 0; j < blockSize; j++) {
					xor = (xor + (data.getElem(i * blockSize + j) % 2)) % 2;
				}
				int lastBit = data.getElem(i * blockSize + key) % 2;
				if (lastBit == cur) {
					curBin++;
					i++;

				} else {
					if (lastBit == 0 & cur == 1) {
						int tm = data.getElem(i * blockSize + key);
						data.setElem(i * blockSize + key, tm + 1);
					}
					if (lastBit == 1 & cur == 0) {
						int tm = data.getElem(i * blockSize + key);
						data.setElem(i * blockSize + key,  tm - 1);
					}

					int xorNew = 0;
					for (int j = 0; j < blockSize; j++) {
						xorNew = xorNew + (data.getElem(i * blockSize + j) % 2) % 2;
					}

					if (xor != xorNew) {
						int tm = data.getElem(i * blockSize + 0);
						data.setElem(i * blockSize + 0, tm - 1);
					}
					curBin++;
					i++;
				}

		}

		Raster t = image.getData();
		Raster rasterNew = t.createRaster(image.getData().getSampleModel(), data, new Point(0, 0));
		image.setData(rasterNew);
		File out = new File(output);
		ImageIO.write(image, "bmp", out);
		return blockSize;
	}

	public static String decode(String coded, int key, int blockSize) throws IOException {
		File f = new File(coded);
		BufferedImage image = ImageIO.read(f);
		DataBuffer data = image.getData().getDataBuffer();
		int size = data.getSize();

		StringBuilder out = new StringBuilder();

		for (int i = 0; i < size / blockSize; i++) {
			/*int tmp = data.getElem(i * blockSize + key);
			int tm = tmp % 2;
			System.out.println(tmp + " " + tm);*/
			int lastBit = data.getElem(i * blockSize + key) % 2;
			if (lastBit == 0)
				out.append("0");
			else
				out.append("1");
		}

		String output = out.toString();
		StringBuilder tmp = new StringBuilder();
		StringBuilder text = new StringBuilder();
		int i = 0;
		while (i < output.length()) {
			tmp = new StringBuilder();
			for (int j = 0; j < 8; j++) {
				tmp.append(output.charAt(i));
				i++;
				if (i == output.length())
					break;
			}
			text.append((char) Integer.parseInt(tmp.toString(), 2));
		}
		return text.toString();
	}
	
	
	public static double PSNR(String fileIn, String fileOut) throws IOException {
        byte[] a = Files.readAllBytes(Paths.get(fileIn));
        byte[] b = Files.readAllBytes(Paths.get(fileOut));
        if(a.length != b.length){

            System.out.format("PSNR needs equal\n");
            return 0;
        }

        double s = 0;

        for(int i = 0; i < a.length; i++){

            int ax = Byte.toUnsignedInt(a[i]);
            int bx = Byte.toUnsignedInt(b[i]);

            s += (ax-bx)*(ax-bx);
        }

        double k = 1.0*a.length*255*255/s;

        return 10 * Math.log10(k);
    }
	

}