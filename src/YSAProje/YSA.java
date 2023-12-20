package YSAProje;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.nnet.learning.MomentumBackpropagation;
import org.neuroph.util.TransferFunctionType;

public class YSA {
	private static final File egitimDosya = new File(YSA.class.getResource("Data.txt").getPath());
	private static final File testDosya = new File(YSA.class.getResource("DataTest.txt").getPath());

	private DataSet egitimVeriSeti;
	private DataSet testVeriSeti;

	private double[] minumumlar;
	private double[] maksimumlar;

	private int araKatmanNoron;
	private MomentumBackpropagation mbp;

	public YSA(int araKatmanNoron, double momentum, double ogrenmeKatsayisi, double maxHata, int epoch)
			throws FileNotFoundException {

		this.araKatmanNoron = araKatmanNoron;

		minumumlar = new double[3];
		maksimumlar = new double[3];
		for (int i = 0; i < 1; i++) {
			minumumlar[i] = Double.MAX_VALUE;
			maksimumlar[i] = Double.MIN_VALUE;
		}

		// E�itim ve test veri setlerindeki minimum ve maksimum de�erleri bul
		minimumVeMaksimumlarBul(egitimDosya);
		minimumVeMaksimumlarBul(testDosya);

		egitimVeriSeti = veriSetiOku(egitimDosya);
		testVeriSeti = veriSetiOku(testDosya);

		mbp = new MomentumBackpropagation();
		mbp.setLearningRate(ogrenmeKatsayisi);
		mbp.setMaxError(maxHata);
		mbp.setMaxIterations(epoch);
		mbp.setMomentum(momentum);
	}

	public void Egit() {
		MultiLayerPerceptron sinirselAg = new MultiLayerPerceptron(TransferFunctionType.SIGMOID, 2, araKatmanNoron, 1);
		System.out.println(egitimVeriSeti);
		sinirselAg.setLearningRule(mbp);

		sinirselAg.learn(egitimVeriSeti);

		System.out.println("Egitim Tamamlandi");
		sinirselAg.save("model.nnet");
	}

	// Test i�lemini ger�ekle�tir ve ortalama hata de�erini d�nd�r
	public double test() {
		// Modeli dosyadan y�kle
		NeuralNetwork sinirselAg = NeuralNetwork.createFromFile("model.nnet");

		// Toplam hata de�eri
		double toplamHata = 0;

		// Test veri setindeki her bir sat�r �zerinde i�lem yap
		for (var satir : testVeriSeti.getRows()) {
			// Modelin girdisini belirle
			sinirselAg.setInput(satir.getInput());

			// Modelin ��kt�s�n� hesapla
			sinirselAg.calculate();

			// Toplam hata de�erine bu sat�rdaki hata eklenir
			toplamHata += mse(satir.getDesiredOutput(), sinirselAg.getOutput());
		}

		// Ortalama hata de�erini d�nd�r
		return toplamHata / testVeriSeti.size();
	}

	// E�itim i�leminin ger�ekle�ti�i a�amada toplam hata de�erini d�nd�r
	public double egitimHata() {
		return mbp.getTotalNetworkError();
	}

	// Belirtilen giri� de�erleri �zerinde tek bir test i�lemi ger�ekle�tir ve
	// sonucu d�nd�r
	public String tekSatirTest(double[] inputs) {
		// Giri� de�erlerini normalle�tir
		double[] inputD = new double[inputs.length];
		for (int i = 0; i < inputs.length; i++) {
			inputD[i] = minMax(inputs[i], minumumlar[i], maksimumlar[i]);
		}

		// Modeli dosyadan y�kle
		NeuralNetwork sinirselAg = NeuralNetwork.createFromFile("model.nnet");

		// Modelin girdisini belirle
		sinirselAg.setInput(inputD);

		// Modelin ��kt�s�n� hesapla
		sinirselAg.calculate();

		// Ger�ek ��kt�y� d�nd�r
		return gercekCikti(sinirselAg.getOutput());
	}

	// Modelin ��kt�s�na g�re ger�ek ��kt�y� belirle
	private String gercekCikti(double[] output) {

		return String.valueOf((int)(output[0]*40))+" Fren/saniye";
//		double max = Double.MIN_VALUE;
//		int maxIndex = 0;
//
//		// ��k�� de�erleri aras�nda en b�y�k olan� bul
//		for (int i = 0; i < output.length; i++) {
//			if (output[i] > max) {
//				max = output[i];
//				maxIndex = i;
//			}
//		}
//
//		// En b�y�k olan de�ere g�re etiketi belirle ve d�nd�r
//		switch (maxIndex) {
//		case 0:
//			return "k�t�";
//		case 1:
//			return "normal";
//		case 2:
//			return "iyi";
//		default:
//			return "hata";
//		}
	}

	// Ortalama karesel hata (MSE) hesapla
	private double mse(double[] beklenen, double[] cikti) {
		double birSatirHata = 0;
		// Her bir ��k�� i�in hata hesapla
		for (int i = 0; i < beklenen.length; i++) {
			birSatirHata += Math.pow(beklenen[i] - cikti[i], 2);
		}
		// Toplam hatay� d�nd�r
		return birSatirHata / 3;
	}

	// Dosyadan veri setini oku
	private DataSet veriSetiOku(File dosya) throws FileNotFoundException {
		Scanner in = new Scanner(dosya);

		// DataSet nesnesi olu�tur, 2 giri� say�s�, 1 ��k�� say�s�
		DataSet ds = new DataSet(2, 1);

		// Dosya sonuna kadar her bir sat�r� oku
		while (in.hasNextLine()) {

			String[] values = in.nextLine().split("\\s+"); // Satirdaki de�erleri bo�luklara g�re ay�r
			double[] input = new double[2];// Giri� de�erlerini oku ve normalle�tir
			double[] output = new double[1];
			for (int i = 0; i < 3; i++) {
				double d = Double.parseDouble(values[i]);
				// Giri� de�erlerini normalle�tir
				if (i == 2) {
					output[0] = minMax(d, minumumlar[i], maksimumlar[i]);

				} else {
					input[i] = minMax(d, minumumlar[i], maksimumlar[i]);
				}
			}

			// ��k�� de�erlerini oku

			// Sat�r� DataSet'e ekle
			DataSetRow satir = new DataSetRow(input, output);
			ds.add(satir);
		}

		// DataSet'i d�nd�r
		return ds;
	}

	// De�erin min-max normalizasyonunu yap
	private double minMax(double d, double min, double max) {
		return (d - min) / (max - min);
	}

	// Dosyadaki minimum ve maksimum de�erleri bul
	private void minimumVeMaksimumlarBul(File dosya) throws FileNotFoundException {
		Scanner in = new Scanner(dosya);
		// Dosya sonuna kadar her bir sat�r� oku
		while (in.hasNextLine()) {

			String[] values = in.nextLine().split("\\s+"); // Satirdaki de�erleri bo�luklara g�re ay�r
			// Her bir giri� de�eri i�in minimum ve maksimumu g�ncelle
			for (int i = 0; i < 3; i++) {
				double d = Double.parseDouble(values[i]);
				if (d < minumumlar[i])
					minumumlar[i] = d;
				if (d > maksimumlar[i])
					maksimumlar[i] = d;
			}
			// ��k�� de�erlerini oku, ancak kullan�lmayacak

		}
	}
}
